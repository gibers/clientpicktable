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

import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
import com.hackpicktable.clientpicktable.dtos.ReservationResponseDto;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.LastPatronymicUsed;
import com.hackpicktable.clientpicktable.entities.Patronymic;
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

	private static final List<String> RESTAURANT_TO_EXCLUDE = List.of("jacques-restaurant");

	@Transactional
	public ReservationResponseDto bookAReservation(String establishmentSlug, BookATableRequestDto bookATableRequestDto) {
		// 1. Retrieve establishment from DB.
		Establishment establishmentFromDB = establishmentRepository.findBySlug(establishmentSlug).orElseThrow();
		// 2. get last patronymic used
		Optional<LastPatronymicUsed> byEstablishmentId = lastPatronymicUsedRepository.findById(establishmentFromDB.getId());
		// 3. get next patronymic to use
		long nextPatronymicToUse = byEstablishmentId
				.map(x -> x.getLastPatronymicUsed().getId() + 1).orElse(1L);
		Patronymic patronymicToUse = patronymicRepository.findById(nextPatronymicToUse).orElseThrow();
		// 4. get list of tables with capacity greater than or equal to the number of people
		List<Tisch> tablesWithCapacityGte = tischRepository.findAllByEstablishmentAndCapacityGreaterThanEqualAndBookableTrue(
						establishmentFromDB, bookATableRequestDto.getNumberOfPeople())
				.stream().sorted(Comparator.comparing(Tisch::getCapacity)).toList();
		// 5. book a table:
		return reservationParamRequestProvider.getObject().setParamAndLaunch(
				establishmentFromDB, patronymicToUse, bookATableRequestDto.getNumberOfPeople(), bookATableRequestDto.getStartTime(),
				bookATableRequestDto.getDateReservation(), tablesWithCapacityGte);
	}

	public List<ReservationResponseDto> bookAReservationForAllEstablishment() {
		List<ReservationResponseDto> reservationResponseDtoResult = new ArrayList<>();
		// 1. Retrieve establishments from DB that have at least one table.
		List<Establishment> establishmentsFromDB = tischRepository.findDistinctEstablishments();

		LocalDate reservationDate = LocalDate.now().plusDays(1);

		List<Integer> listCapacityOrder = List.of(4, 3, 5, 2, 6, 1, 7); // 7 items
		Map<Establishment, LinkedHashMap<Integer, List<Tisch>>> mapEstablishmentCapacities = getCapacity(establishmentsFromDB,
				listCapacityOrder);
		mapEstablishmentCapacities.forEach((establishment, mapCapacityTablesForThatEstablishment) -> {
//			if (!establishment.getSlug().equals("vally")) return;

			Patronymic patronymicToUse = getPatronymicToUse(establishment);

			List<LocalTime> listStartTimeSorted = getListStartTimeSorted(establishment, reservationDate);
			if (listStartTimeSorted == null) {
				return;
			}

			ParamReservationLoop paramReservationLoop = getFirstParameterLoop();
			List<Integer> capacities = new ArrayList<>(mapCapacityTablesForThatEstablishment.keySet());
			ReservationResponseDto reservationResponseDto = null;

			for (ParamReservationLoop reservationState = paramReservationLoop;
				 conditionStopFor(listStartTimeSorted, Objects.requireNonNull(reservationState), reservationResponseDto);
				 reservationState = conditionNextStep(mapCapacityTablesForThatEstablishment.size(), reservationState,
						 Objects.requireNonNull(reservationResponseDto))) {

				LocalTime localTimeReservation = listStartTimeSorted.get(reservationState.startTimeIndex);
				Integer capacity = capacities.get(reservationState.capacityIndex);

				reservationResponseDto = reservationParamRequestProvider.getObject().setParamAndLaunch(
						establishment, patronymicToUse, capacity, localTimeReservation,
						reservationDate, mapCapacityTablesForThatEstablishment.get(capacity));
			}
			reservationResponseDtoResult.add(reservationResponseDto);
		});

		return reservationResponseDtoResult;
	}

	private ParamReservationLoop getFirstParameterLoop() {
		return new ParamReservationLoop(0, 0);
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

	private ParamReservationLoop conditionNextStep(int numberOfDistinctCapacity,
			ParamReservationLoop initState, ReservationResponseDto reservationResponseDto) {
		// we return a fake ParamReservationLoop, in order to avoid the NPE on method:
		// {@link #conditionStopFor(List, ParamReservationLoop, ReservationResponseDto)}.
		if (reservationResponseDto.getSuccess()) return new ParamReservationLoop(1, 1);
		if (BLOCKING_MESSAGES_TIME.stream().anyMatch(reservationResponseDto.getErrorMessage()::contains)) {
			return new ParamReservationLoop(0, initState.startTimeIndex + 1);
		}
		if (initState.capacityIndex >= numberOfDistinctCapacity -1) {
			return new ParamReservationLoop(0, initState.startTimeIndex + 1);
		}
		return new ParamReservationLoop(initState.capacityIndex + 1, initState.startTimeIndex);
	}

	private boolean conditionStopFor(List<LocalTime> listStartTime, ParamReservationLoop reservationState,
			ReservationResponseDto reservationResponseDto) {
		if (reservationState.capacityIndex == 0 && reservationState.startTimeIndex == 0) {
			return true; // skip the first iteration
		}
		if (!reservationResponseDto.getSuccess() && BLOCKING_MESSAGES_TOOMANYRESERVATION.contains(reservationResponseDto.getErrorMessage())) {
			return false;
		}
		boolean sizeExceeded = reservationState.startTimeIndex >= listStartTime.size();
		return reservationResponseDto.getSuccess().equals(false) && !sizeExceeded;
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

	private Map<Establishment, LinkedHashMap<Integer, List<Tisch>>> getCapacity(List<Establishment> establishmentsFromDB,
			List<Integer> listCapacityOrder) {
		Map<Establishment, LinkedHashMap<Integer, List<Tisch>>> mapEstablishmentCapacities = new HashMap<>();
		establishmentsFromDB.stream().filter(establishment -> !RESTAURANT_TO_EXCLUDE.contains(establishment.getSlug()))
				.forEach(establishment -> mapEstablishmentCapacities.computeIfAbsent(establishment, e -> {
			Map<Integer, List<Tisch>> tablesByCapacity = getMapTableCapacity(tischRepository.findAllByEstablishment(e));
			LinkedHashMap<Integer, List<Tisch>> orderedTablesByCapacity = new LinkedHashMap<>();
			listCapacityOrder.forEach(capacity -> {
				if (tablesByCapacity.containsKey(capacity)) {
					orderedTablesByCapacity.put(capacity, tablesByCapacity.get(capacity));
				}
			});
			return orderedTablesByCapacity;
		}));
		return mapEstablishmentCapacities;
	}

	private Map<Integer, List<Tisch>> getMapTableCapacity(List<Tisch> tischList) {
		return tischList.stream().collect(Collectors.groupingBy(Tisch::getCapacity));
	}

	@Data
	private static class ParamReservationLoop {
		private int capacityIndex;
		private int startTimeIndex;

		public ParamReservationLoop(int i, int startTimeIndex) {
			this.capacityIndex = i;
			this.startTimeIndex = startTimeIndex;
		}
	}


}
