package com.hackpicktable.clientpicktable.impl;

import static com.hackpicktable.clientpicktable.impl.ReservationParamRequest.BLOCKING_MESSAGES_TIME;
import static com.hackpicktable.clientpicktable.impl.ReservationParamRequest.BLOCKING_MESSAGES_TOOMANYRESERVATION;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.LastPatronymicUsed;
import com.hackpicktable.clientpicktable.entities.Patronymic;
import com.hackpicktable.clientpicktable.entities.PreviousReservation;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.entities.jsonobjects.ParamReservationState;
import com.hackpicktable.clientpicktable.impl.secondlevel.ListStartTimeSlotForThatDate;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.LastPatronymicUsedRepository;
import com.hackpicktable.clientpicktable.repositories.PatronymicRepository;
import com.hackpicktable.clientpicktable.repositories.PreviousReservationRepository;
import com.hackpicktable.clientpicktable.repositories.TischRepository;

import jakarta.transaction.Transactional;
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
	private final ListStartTimeSlotForThatDate listStartTimeSlotForThatDate;
	private final PrepareListTablesBeforeReservation prepareListTablesBeforeReservation;
	private final PreviousReservationRepository previousReservationRepository;

	private final ObjectProvider<ReservationParamRequest> reservationParamRequestProvider;

	public static final List<String> RESTAURANT_TO_EXCLUDE = List.of("jacques-restaurant");

	//	public static final List<String> RESTAURANT_TO_EXCLUDE = List.of("jacques-restaurant", "leterminus", "ponte-vecchio", "le-pont-de-brent",
//			"citios", "le-self", "the-barista-lab", "lasuitepayerne", "zao-cafe", "ismael", "hotel-cailler");

	@Transactional
	public ReservationResponse bookAReservationByScheduling() {
		Pair<Establishment, PreviousReservation> estaWithReservation = prepareListTablesBeforeReservation.execute();

		// 2. get next patronymic to use
		Patronymic patronymicToUse = getPatronymicToUse(estaWithReservation.getFirst());
		// 3. get list of tables with capacity greater than or equal to the number of people
		List<Tisch> tablesPerCapacity = estaWithReservation.getSecond().getPreviousTischEntries().stream()
				.map(x -> this.tischRepository.findById(x.getTableId()).orElseThrow())
				.toList();

		// 4. book a table:
		LocalDate reservationDate = LocalDate.now().plusDays(1);
		return this.loopOnTheSameEstablishmentWithParamReservation(estaWithReservation, tablesPerCapacity, patronymicToUse,
				reservationDate);
	}

	@Transactional
	public ReservationResponse bookAReservation(String establishmentSlug) {
		// 1. Retrieve establishment from DB.
		Establishment establishmentFromDB = establishmentRepository.findBySlug(establishmentSlug).orElseThrow();

		// 2. get next patronymic to use
		Patronymic patronymicToUse = getPatronymicToUse(establishmentFromDB);
		// 3. get list of tables with capacity greater than or equal to the number of people
		List<Tisch> tablesPerCapacity = this.listTablesPerCapacityForThatEstablishment(establishmentFromDB);

		LocalDate reservationDate = LocalDate.now().plusDays(1);
		List<LocalTime> listStartTimeSorted = listStartTimeSlotForThatDate.getListStartTimeSorted(establishmentFromDB, reservationDate);
		if (listStartTimeSorted.isEmpty()) {
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

		Map<Establishment, List<Tisch>> mapEstablishmentCapacities = getCapacity(establishmentsFromDB);
		mapEstablishmentCapacities.forEach((establishment, mapCapacityTablesForThatEstablishment) -> {
			//			if (!establishment.getSlug().equals("vally")) return;

			Patronymic patronymicToUse = getPatronymicToUse(establishment);
			List<LocalTime> listStartTimeSorted = listStartTimeSlotForThatDate.getListStartTimeSorted(establishment, reservationDate);
			if (listStartTimeSorted.isEmpty()) {
				return;
			}

			ReservationResponse reservationResponseEntity = loopOnTheSameEstablishment(establishment, mapCapacityTablesForThatEstablishment,
					listStartTimeSorted, patronymicToUse, reservationDate);

			reservationResponseDtoResult.add(reservationResponseEntity);
		});

		return reservationResponseDtoResult;
	}

	private ReservationResponse loopOnTheSameEstablishmentWithParamReservation(Pair<Establishment, PreviousReservation> estaWithPreviousReservation,
			List<Tisch> listTableForThatEstablishment, Patronymic patronymicToUse, LocalDate reservationDate) {
		ReservationResponse reservationResponseEntity = null;
		List<LocalTime> listStartTimeSorted = estaWithPreviousReservation.getSecond().getListStartTime();

		for (ParamReservationState reservationState = estaWithPreviousReservation.getSecond().getParamReservationState();
			 conditionStopForWithParamReservation(estaWithPreviousReservation.getSecond(), listStartTimeSorted,
					 Objects.requireNonNull(reservationState),
					 reservationResponseEntity);
			 reservationState = conditionNextStep(listTableForThatEstablishment.size(), reservationState,
					 Objects.requireNonNull(reservationResponseEntity))) {

			LocalTime localTimeReservation = listStartTimeSorted.get(reservationState.startTimeIndex());
			Tisch tischToBook = listTableForThatEstablishment.get(reservationState.capacityIndex());

			reservationResponseEntity = reservationParamRequestProvider.getObject().setParamAndLaunch(estaWithPreviousReservation.getFirst(),
					patronymicToUse, localTimeReservation, reservationDate, tischToBook);
		}
		return reservationResponseEntity;
	}

	private ReservationResponse loopOnTheSameEstablishment(Establishment establishment,
			List<Tisch> mapCapacityTablesForThatEstablishment, List<LocalTime> listStartTimeSorted,
			Patronymic patronymicToUse, LocalDate reservationDate) {
		ReservationResponse reservationResponseEntity = null;
		ParamReservationState paramReservationState = new ParamReservationState(0, 0);
		for (ParamReservationState reservationState = paramReservationState;
			 conditionStopFor(listStartTimeSorted, Objects.requireNonNull(reservationState),
					 reservationResponseEntity);
			 reservationState = conditionNextStep(mapCapacityTablesForThatEstablishment.size(), reservationState,
					 Objects.requireNonNull(reservationResponseEntity))) {

			LocalTime localTimeReservation = listStartTimeSorted.get(reservationState.startTimeIndex());
			Tisch tischToBook = mapCapacityTablesForThatEstablishment.get(reservationState.capacityIndex());

			reservationResponseEntity = reservationParamRequestProvider.getObject().setParamAndLaunch(establishment, patronymicToUse,
					localTimeReservation, reservationDate, tischToBook);
		}
		return reservationResponseEntity;
	}

	private ParamReservationState conditionNextStep(int numberOfTischForThatEstablishment,
			ParamReservationState initState, ReservationResponse reservationResponseEntity) {
		// we return a fake ParamReservationLoop, in order to avoid the NPE on method:
		// {@link #conditionStopFor(List, ParamReservationLoop, ReservationResponseDto)}.
		if (reservationResponseEntity.getSuccess()) return new ParamReservationState(1, 1);
		if (BLOCKING_MESSAGES_TIME.stream().anyMatch(reservationResponseEntity.getErrorMessage()::contains)) {
			return new ParamReservationState(0, initState.startTimeIndex() + 1);
		}
		// no increment in case of 429 or 403, because we need to try again.
		if (BLOCKING_MESSAGES_TOOMANYRESERVATION.stream().anyMatch(reservationResponseEntity.getErrorMessage()::contains)) {
			return new ParamReservationState(initState.capacityIndex(), initState.startTimeIndex());
		}
		if (initState.capacityIndex() >= numberOfTischForThatEstablishment -1) {
			log.debug("Capacity index is greater or equal to the number of tables for that establishment, next occurency: {}",
					initState.startTimeIndex() + 1);
			return new ParamReservationState(0, initState.startTimeIndex() + 1);
		}
		return new ParamReservationState(initState.capacityIndex() + 1, initState.startTimeIndex());
	}

	private boolean conditionStopForWithParamReservation(PreviousReservation previousReservation, List<LocalTime> listStartTime, ParamReservationState reservationState,
			ReservationResponse reservationResponseEntity) {
		if ((reservationState.capacityIndex() == 0 && reservationState.startTimeIndex() == 0) || reservationResponseEntity == null) {
			return true; // skip the first iteration
		}
		if (!reservationResponseEntity.getSuccess() && BLOCKING_MESSAGES_TOOMANYRESERVATION.contains(reservationResponseEntity.getErrorMessage())) {
			saveCurrentStateInDB(reservationResponseEntity, reservationState, previousReservation);
			return false;
		}
		boolean sizeExceeded = reservationState.startTimeIndex() >= listStartTime.size();
		boolean endLoop = reservationResponseEntity.getSuccess().equals(true) || sizeExceeded;
		if (endLoop) {
			saveCurrentStateInDB(reservationResponseEntity, reservationState, previousReservation);
			return false;
		}
		return true;
	}

	private boolean conditionStopFor(List<LocalTime> listStartTime, ParamReservationState reservationState,
			ReservationResponse reservationResponseEntity) {
		if (reservationState.capacityIndex() == 0 && reservationState.startTimeIndex() == 0) {
			return true; // skip the first iteration
		}
		if (!reservationResponseEntity.getSuccess() && BLOCKING_MESSAGES_TOOMANYRESERVATION.contains(reservationResponseEntity.getErrorMessage())) {
			return false;
		}
		boolean sizeExceeded = reservationState.startTimeIndex() >= listStartTime.size();
		boolean endLoop = reservationResponseEntity.getSuccess().equals(true) || sizeExceeded;
		return !endLoop;
	}

	private void saveCurrentStateInDB(ReservationResponse reservationResponseEntity, ParamReservationState reservationState,
			PreviousReservation previousReservation) {
		if (reservationResponseEntity.getSuccess()) {
			previousReservation.setIntervalWithPrevious(Math.max((previousReservation.getIntervalWithPrevious() - 15), 15));
		} else {
			previousReservation.setIntervalWithPrevious(Math.min((previousReservation.getIntervalWithPrevious() + 15), 300));
		}
		previousReservation.setSuccess(reservationResponseEntity.getSuccess());
		previousReservation.setParamReservationState(reservationState);
		previousReservation.setPreviousHttpStatus(reservationResponseEntity.getHttpStatus());
		this.previousReservationRepository.save(previousReservation);
	}

	private @NonNull Patronymic getPatronymicToUse(Establishment establishmentFromDB) {
		// 2. get last patronymic used
		Optional<LastPatronymicUsed> byEstablishmentId = lastPatronymicUsedRepository.findById(establishmentFromDB.getId());
		// 3. get next patronymic to use
		long nextPatronymicToUse = byEstablishmentId
				.map(x -> x.getLastPatronymicUsed().getId() + 1).orElse(1L);
		return patronymicRepository.findById(nextPatronymicToUse).orElseThrow();
	}

	private Map<Establishment, List<Tisch>> getCapacity(List<Establishment> establishmentsFromDB) {
		Map<Establishment, List<Tisch>> mapEstablishmentCapacities = new HashMap<>();
		establishmentsFromDB.stream().filter(establishment -> !RESTAURANT_TO_EXCLUDE.contains(establishment.getSlug()))
				.forEach(establishment -> mapEstablishmentCapacities.computeIfAbsent(establishment,
						this::listTablesPerCapacityForThatEstablishment));
		return mapEstablishmentCapacities;
	}

	private List<Tisch> listTablesPerCapacityForThatEstablishment (Establishment establishment) {
		List<Integer> listCapacityOrder = List.of(4, 3, 5, 2, 6, 1, 7); // 7 items
		List<Tisch> allByEstablishment = tischRepository.findAllByEstablishment(establishment);
		return listCapacityOrder.stream()
				.flatMap(capacity -> allByEstablishment.stream()
						.filter(table -> Objects.equals(table.getCapacity(), capacity)))
				.toList();
	}

}
