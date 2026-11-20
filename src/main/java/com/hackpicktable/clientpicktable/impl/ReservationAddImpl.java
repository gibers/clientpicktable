package com.hackpicktable.clientpicktable.impl;

import static com.hackpicktable.clientpicktable.impl.ReservationParamRequest.BLOCKING_MESSAGES_TIME;
import static com.hackpicktable.clientpicktable.impl.ReservationParamRequest.BLOCKING_MESSAGES_TOOMANYRESERVATION;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.LastPatronymicUsed;
import com.hackpicktable.clientpicktable.entities.Patronymic;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;
import com.hackpicktable.clientpicktable.entities.ReservationSettings;
import com.hackpicktable.clientpicktable.entities.TimeSlot;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.LastPatronymicUsedRepository;
import com.hackpicktable.clientpicktable.repositories.PatronymicRepository;
import com.hackpicktable.clientpicktable.repositories.ReservationSettingsRepository;
import com.hackpicktable.clientpicktable.repositories.TimeSlotRepository;
import com.hackpicktable.clientpicktable.repositories.TischRepository;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationAddImpl {

	private final EstablishmentRepository establishmentRepository;
	private final TischRepository tischRepository;
	private final LastPatronymicUsedRepository lastPatronymicUsedRepository;
	private final PatronymicRepository patronymicRepository;
	private final TimeSlotRepository timeSlotRepository;
	private final ReservationSettingsRepository reservationSettingsRepository;

	private final ObjectProvider<ReservationParamRequest> reservationParamRequestProvider;

	public static final List<String> RESTAURANT_TO_EXCLUDE = List.of("jacques-restaurant", "leterminus", "ponte-vecchio", "le-pont-de-brent",
			"citios", "le-self", "the-barista-lab", "lasuitepayerne", "zao-cafe", "ismael", "hotel-cailler");

	@Transactional
	public ReservationResponse bookAReservation(String establishmentSlug) {
		// 1. Retrieve establishment from DB.
		Establishment establishmentFromDB = establishmentRepository.findBySlug(establishmentSlug).orElseThrow();
		// 2. get next patronymic to use
		Patronymic patronymicToUse = getPatronymicToUse(establishmentFromDB);
		// 3. get list of tables with capacity greater than or equal to the number of people
		LinkedHashMap<Integer, List<Tisch>> tablesPerCapacity = this.listTablesPerCapacityForThatEstablishment(establishmentFromDB);

		LocalDate reservationDate = LocalDate.now().plusDays(1);
		List<LocalTime> listStartTimeSorted = getListStartTimeSorted(establishmentFromDB, reservationDate);
		if (listStartTimeSorted == null) {
			return null;
		}
		// 4. book a table:
		return this.loopOnTheSameEstablishment(establishmentFromDB, tablesPerCapacity, listStartTimeSorted, patronymicToUse, reservationDate);
	}

	public List<ReservationResponse> bookAReservationForAllEstablishment() {
		List<ReservationResponse> reservationResponseDtoResult = new ArrayList<>();
		// 1. Retrieve establishments from DB that have at least one table.
		List<Establishment> establishmentsFromDB = tischRepository.findDistinctEstablishments();

		LocalDate reservationDate = LocalDate.now().plusDays(1);

		Map<Establishment, LinkedHashMap<Integer, List<Tisch>>> mapEstablishmentCapacities = getCapacity(establishmentsFromDB);
		mapEstablishmentCapacities.forEach((establishment, mapCapacityTablesForThatEstablishment) -> {
			//			if (!establishment.getSlug().equals("vally")) return;

			Patronymic patronymicToUse = getPatronymicToUse(establishment);
			List<LocalTime> listStartTimeSorted = getListStartTimeSorted(establishment, reservationDate);
			if (listStartTimeSorted == null) {
				return;
			}

			ReservationResponse reservationResponseEntity = loopOnTheSameEstablishment(establishment, mapCapacityTablesForThatEstablishment,
					listStartTimeSorted, patronymicToUse, reservationDate);

			reservationResponseDtoResult.add(reservationResponseEntity);
		});

		return reservationResponseDtoResult;
	}

	private ReservationResponse loopOnTheSameEstablishment(Establishment establishment,
			LinkedHashMap<Integer, List<Tisch>> mapCapacityTablesForThatEstablishment, List<LocalTime> listStartTimeSorted,
			Patronymic patronymicToUse, LocalDate reservationDate) {
		List<Integer> capacities = new ArrayList<>(mapCapacityTablesForThatEstablishment.keySet());
		ReservationResponse reservationResponseEntity = null;
		ParamReservationState paramReservationState = new ParamReservationState(0, 0);
		for (ParamReservationState reservationState = paramReservationState;
			 conditionStopFor(mapCapacityTablesForThatEstablishment, listStartTimeSorted, Objects.requireNonNull(reservationState),
					 reservationResponseEntity);
			 reservationState = conditionNextStep(mapCapacityTablesForThatEstablishment.size(), reservationState,
					 Objects.requireNonNull(reservationResponseEntity))) {

			LocalTime localTimeReservation = listStartTimeSorted.get(reservationState.startTimeIndex);
			Integer capacity = capacities.get(reservationState.capacityIndex);

			reservationResponseEntity = reservationParamRequestProvider.getObject().setParamAndLaunch(
					establishment, patronymicToUse, capacity, localTimeReservation,
					reservationDate, mapCapacityTablesForThatEstablishment.get(capacity));
		}
		return reservationResponseEntity;
	}

	private @Nullable List<LocalTime> getListStartTimeSorted(Establishment establishment, LocalDate reservationDate) {
		List<TimeSlot> timeSlotsForThatDay = getTimeSlotsForThatDay(reservationDate, establishment);
		ReservationSettings reservationSettings = reservationSettingsRepository.findByEstablishment(establishment);
		List<LocalTime> listStartTime = getListStartTime(timeSlotsForThatDay, reservationSettings);
		if (listStartTime.isEmpty())
			return null;
		return sortedListStartTime(listStartTime);
	}

	private List<LocalTime> sortedListStartTime(List<LocalTime> listStartTime) {
		return listStartTime.stream()
				.sorted(Comparator.comparingLong(time -> Math.abs(time.toNanoOfDay() - LocalTime.NOON.toNanoOfDay())))
				.toList();
	}

	private ParamReservationState conditionNextStep(int numberOfDistinctCapacity,
			ParamReservationState initState, ReservationResponse reservationResponseEntity) {
		// we return a fake ParamReservationLoop, in order to avoid the NPE on method:
		// {@link #conditionStopFor(List, ParamReservationLoop, ReservationResponseDto)}.
		if (reservationResponseEntity.getSuccess()) return new ParamReservationState(1, 1);
		if (BLOCKING_MESSAGES_TIME.stream().anyMatch(reservationResponseEntity.getErrorMessage()::contains)) {
			return new ParamReservationState(0, initState.startTimeIndex + 1);
		}
		if (initState.capacityIndex >= numberOfDistinctCapacity -1) {
			return new ParamReservationState(0, initState.startTimeIndex + 1);
		}
		return new ParamReservationState(initState.capacityIndex + 1, initState.startTimeIndex);
	}

	private boolean conditionStopFor(LinkedHashMap<Integer, List<Tisch>> mapCapacityTablesForThatEstablishment, List<LocalTime> listStartTime, ParamReservationState reservationState,
			ReservationResponse reservationResponseEntity) {
		if (reservationState.capacityIndex == 0 && reservationState.startTimeIndex == 0) {
			return true; // skip the first iteration
		}
		if (!reservationResponseEntity.getSuccess() && BLOCKING_MESSAGES_TOOMANYRESERVATION.contains(reservationResponseEntity.getErrorMessage())) {
			// todo: save current state in DB: success -> false | error_type (429 or 200) -> 429 | paramReservation: startTimeIndex,
			//  capacityIndex | listStartTime | capacities
			return false;
		}
		boolean sizeExceeded = reservationState.startTimeIndex >= listStartTime.size();
		boolean endLoop = reservationResponseEntity.getSuccess().equals(true) || sizeExceeded;
		if (endLoop) {
			// todo: save current state in DB: success -> true | error_type (429 or 200) -> 200 | paramReservation: startTimeIndex,
			//  capacityIndex | listStartTime | capacities
			return false;
		}
		return true;
	}

	private @NonNull Patronymic getPatronymicToUse(Establishment establishmentFromDB) {
		// 2. get last patronymic used
		Optional<LastPatronymicUsed> byEstablishmentId = lastPatronymicUsedRepository.findById(establishmentFromDB.getId());
		// 3. get next patronymic to use
		long nextPatronymicToUse = byEstablishmentId
				.map(x -> x.getLastPatronymicUsed().getId() + 1).orElse(1L);
		return patronymicRepository.findById(nextPatronymicToUse).orElseThrow();
	}

	private List<TimeSlot> getTimeSlotsForThatDay(LocalDate ld, Establishment establishmentsFromDB) {
		DayOfWeek dayOfWeek = ld.getDayOfWeek();
		int indexOfDayFrom0To6 = (dayOfWeek.getValue() == 7)? 0 : dayOfWeek.getValue();
		return timeSlotRepository.findAllByAvailableDay(indexOfDayFrom0To6,
				establishmentsFromDB.getId());
	}

	private List<LocalTime> getListStartTime(List<TimeSlot> timeSlotsForThatDay, ReservationSettings reservationSettings) {
		Integer bookingIntervalMinutes = reservationSettings.getBookingIntervalMinutes(); // ex: 30
		Integer reservationDurationMinutes = reservationSettings.getReservationDurationMinutes(); // ex: 120
		List<LocalTime> timeIntervals = new ArrayList<>();
		timeSlotsForThatDay.forEach(timeSlot -> {
			LocalTime startTime = timeSlot.getStartTime();
			LocalTime endTime = timeSlot.getEndTime().minusMinutes(reservationDurationMinutes);
			timeIntervals.addAll(computeTimeIntervals(startTime, endTime, bookingIntervalMinutes));
		});
		return timeIntervals.stream()
				.sorted(Comparator.naturalOrder())
				.toList();
	}

	private List<LocalTime> computeTimeIntervals(LocalTime startTime, LocalTime endTime, Integer bookingIntervalMinutes) {
		LocalTime currentTime = startTime;
		List<LocalTime> timeIntervals = new ArrayList<>();
		while (currentTime.isBefore(endTime) || currentTime.equals(endTime)) {
			LocalTime intervalEndTime = currentTime.plusMinutes(bookingIntervalMinutes);
			timeIntervals.add(currentTime);
			currentTime = intervalEndTime;
		}
		return timeIntervals;
	}

	private Map<Establishment, LinkedHashMap<Integer, List<Tisch>>> getCapacity(List<Establishment> establishmentsFromDB) {
		Map<Establishment, LinkedHashMap<Integer, List<Tisch>>> mapEstablishmentCapacities = new HashMap<>();
		establishmentsFromDB.stream().filter(establishment -> !RESTAURANT_TO_EXCLUDE.contains(establishment.getSlug()))
				.forEach(establishment -> mapEstablishmentCapacities.computeIfAbsent(establishment,
						this::listTablesPerCapacityForThatEstablishment));
		return mapEstablishmentCapacities;
	}

	private LinkedHashMap<Integer, List<Tisch>> listTablesPerCapacityForThatEstablishment (Establishment establishment) {
		List<Integer> listCapacityOrder = List.of(4, 3, 5, 2, 6, 1, 7); // 7 items
		Map<Integer, List<Tisch>> tablesByCapacity = getMapTableCapacity(tischRepository.findAllByEstablishment(establishment));
		LinkedHashMap<Integer, List<Tisch>> orderedTablesByCapacity = new LinkedHashMap<>();
		listCapacityOrder.forEach(capacity -> {
			if (tablesByCapacity.containsKey(capacity)) {
				orderedTablesByCapacity.put(capacity, tablesByCapacity.get(capacity));
			}
		});
		return orderedTablesByCapacity;
	}

	private Map<Integer, List<Tisch>> getMapTableCapacity(List<Tisch> tischList) {
		return tischList.stream().collect(Collectors.groupingBy(Tisch::getCapacity));
	}

	@Data
	private static class ParamReservationState {
		private int capacityIndex;
		private int startTimeIndex;

		public ParamReservationState(int i, int startTimeIndex) {
			this.capacityIndex = i;
			this.startTimeIndex = startTimeIndex;
		}
	}


}
