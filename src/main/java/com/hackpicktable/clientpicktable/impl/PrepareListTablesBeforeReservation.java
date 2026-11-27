package com.hackpicktable.clientpicktable.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.PreviousReservation;
import com.hackpicktable.clientpicktable.entities.PreviousTischEntry;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.entities.jsonobjects.ParamReservationState;
import com.hackpicktable.clientpicktable.impl.secondlevel.ListStartTimeSlotForThatDate;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.PreviousReservationRepository;
import com.hackpicktable.clientpicktable.repositories.TischRepository;

import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class PrepareListTablesBeforeReservation {

	private final TischRepository tischRepository;
	private final EstablishmentRepository establishmentRepository;
	private final PreviousReservationRepository previousReservationRepository;
	private final ListStartTimeSlotForThatDate listStartTimeSlotForThatDate;
	private final EntityManager entityManager;

	private final List<Integer> listCapacityOrder = List.of(4, 3, 5, 2, 6, 1, 7); // 7 items
	private List<String> distinctSlugs;
	private int previousInterval = 0;

	public PrepareListTablesBeforeReservation(TischRepository tischRepository, EstablishmentRepository establishmentRepository,
			PreviousReservationRepository previousReservationRepository, ListStartTimeSlotForThatDate listStartTimeSlotForThatDate,
			EntityManager entityManager) {
		this.tischRepository = tischRepository;
		this.establishmentRepository = establishmentRepository;
		this.previousReservationRepository = previousReservationRepository;
		this.listStartTimeSlotForThatDate = listStartTimeSlotForThatDate;
		this.entityManager = entityManager;
	}

	public PreviousReservation execute() {
		this. distinctSlugs = firstCheckControl();
		return getOrInsertPreviousReservation();
	}

	private List<String> firstCheckControl() {
		List<String> distinctSlugs = this.establishmentRepository.findDistinctSlugs()
				.stream().filter(s -> !ReservationAddImpl.RESTAURANT_TO_EXCLUDE.contains(s)).toList();
		if (distinctSlugs.isEmpty()) {
			throw new IllegalStateException("No establishment found in table establishment");
		}
		long numberOfRow = previousReservationRepository.findAll().size();
		if (numberOfRow > 1) {
			throw new IllegalStateException("table previous_reservation should have maximum one row");
		}
		return distinctSlugs;
	}

	private PreviousReservation getOrInsertPreviousReservation() {
		Optional<PreviousReservation> previousReservationFromDBOptional = previousReservationRepository.findAll().stream().findFirst();
		if (previousReservationFromDBOptional.isEmpty() || Objects.equals(previousReservationFromDBOptional.get().getPreviousHttpStatus(), 200)) {
			this.previousInterval = setPreviousInterval(previousReservationFromDBOptional);
			return insertNewEntryInPreviousReservation();
		} else if (previousReservationFromDBOptional.get().getPreviousHttpStatus() == null ||
				List.of(429, 403, 400).contains(previousReservationFromDBOptional.get().getPreviousHttpStatus())) {
			return previousReservationFromDBOptional.get();
		}
		throw new IllegalStateException("Unexpected HTTP status code: " + previousReservationFromDBOptional.get().getPreviousHttpStatus());
	}

	// all methods below are called when previousReservation.isEmpty() || previousReservation.getPreviousHttpStatus() == 200
	// we could put them in a separated class.

	// method3
	private Pair<Establishment, List<LocalTime>> lookUntilNextEstaHasSomeStartTime() {
		Establishment establishment;
		int indexSlug = 0;
		List<LocalTime> startTimeListSorted;
		do {
			establishment = nextSlugToBookOn(indexSlug++);
			startTimeListSorted = listStartTimeSorted(establishment);
		} while (startTimeListSorted.isEmpty());
		return Pair.of(establishment, startTimeListSorted);
	}

	// method7
	private void insertPreviousReservationAndPreviousTischInDB(
			Pair<Establishment, List<LocalTime>> establishmentToBookOn,
			PreviousReservation previousReservation) {
		List<Tisch> tablesPerCapacity = this.listTablesPerCapacityForThatEstablishment(establishmentToBookOn.getFirst());
		extendPreviousEstablishmentWithListTables(previousReservation, tablesPerCapacity, establishmentToBookOn.getSecond());
		this.previousReservationRepository.deleteAll();
		this.entityManager.persist(previousReservation);
	}

	// method4
	private Establishment nextSlugToBookOn(int indexSlug) {
		if (distinctSlugs.size() <= indexSlug) {
			throw new IllegalStateException(" indexSlug greater than distinctSlugs size");
		}
		long numberOfRow = previousReservationRepository.findAll().size();
		if (numberOfRow == 0) {
			return this.establishmentRepository.findBySlug(distinctSlugs.get(indexSlug)).orElseThrow();
		}
		PreviousReservation previousReservation = previousReservationRepository.findAll().stream().findFirst().orElseThrow();
		String previousSlug = previousReservation.getEstablishment().getSlug();
		int i = distinctSlugs.indexOf(previousSlug);
		String slug = distinctSlugs.get((i + 1 + indexSlug) % distinctSlugs.size());
		return this.establishmentRepository.findBySlug(slug).orElseThrow();
	}

	// method5
	private @NonNull List<LocalTime> listStartTimeSorted(Establishment establishmentToBookOn) {
		LocalDate reservationDate = LocalDate.now().plusDays(1);
		return listStartTimeSlotForThatDate.getListStartTimeSorted(establishmentToBookOn, reservationDate);
	}

	// method8
	private List<Tisch> listTablesPerCapacityForThatEstablishment (Establishment establishment) {
		List<Tisch> allByEstablishment = tischRepository.findAllByEstablishment(establishment);
		return listCapacityOrder.stream()
				.flatMap(capacity -> allByEstablishment.stream()
						.filter(table -> Objects.equals(table.getCapacity(), capacity)))
				.toList();
	}

	// methode1
	private int setPreviousInterval(Optional<PreviousReservation> previousReservationFromDBOptional) {
		if (previousReservationFromDBOptional.isEmpty())
			return 30;
		if (previousReservationFromDBOptional.get().getPreviousHttpStatus() == 200) {
			return previousReservationFromDBOptional.get().getIntervalWithPrevious();
		}
		throw new IllegalStateException("this method should be called just when previousReservation is empty or 200: " + previousReservationFromDBOptional.get());
	}

	// method2
	private PreviousReservation insertNewEntryInPreviousReservation() {
		Pair<Establishment, List<LocalTime>> estaWithStartTimeList = lookUntilNextEstaHasSomeStartTime();
		PreviousReservation previousReservation = buildEmptyPreviousEstablishment(estaWithStartTimeList.getFirst());
		insertPreviousReservationAndPreviousTischInDB(estaWithStartTimeList, previousReservation);
		return previousReservation;
	}

	// method9
	private void extendPreviousEstablishmentWithListTables(PreviousReservation previousReservation, List<Tisch> tablesPerCapacity,
			List<LocalTime> startTimeListSorted) {
		List<PreviousTischEntry> previousTischEntries = createPreviousTischEntries(tablesPerCapacity);
		previousReservation.setPreviousTischEntries(previousTischEntries);
		previousReservation.setListStartTime(startTimeListSorted);
	}

	// method10
	private @NonNull List<PreviousTischEntry> createPreviousTischEntries(List<Tisch> tablesPerCapacity) {
		List<PreviousTischEntry> previousTischEntries = new ArrayList<>();
		tablesPerCapacity.forEach(t -> previousTischEntries.add(PreviousTischEntry.builder()
				.capacity(t.getCapacity())
				.tableId(t.getId())
				.build()));
		return previousTischEntries;
	}

	// method6
	private PreviousReservation buildEmptyPreviousEstablishment(Establishment establishment) {
		return PreviousReservation.builder()
				.establishment(establishment)
				.intervalWithPrevious(this.previousInterval - 15)
				.paramReservationState(new ParamReservationState(0, 0))
				.build();
	}

}
