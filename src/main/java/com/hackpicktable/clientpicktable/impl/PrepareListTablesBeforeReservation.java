package com.hackpicktable.clientpicktable.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	public PrepareListTablesBeforeReservation(TischRepository tischRepository, EstablishmentRepository establishmentRepository,
			PreviousReservationRepository previousReservationRepository, ListStartTimeSlotForThatDate listStartTimeSlotForThatDate,
			EntityManager entityManager) {
		this.tischRepository = tischRepository;
		this.establishmentRepository = establishmentRepository;
		this.previousReservationRepository = previousReservationRepository;
		this.listStartTimeSlotForThatDate = listStartTimeSlotForThatDate;
		this.entityManager = entityManager;
	}

	public Pair<Establishment, PreviousReservation> execute() {
		Pair<Establishment, List<LocalTime>> estaWithStartTimeList = seekTheNextEstablishmentWithStartTime();
		PreviousReservation previousReservation = getOrInsertPreviousReservation(estaWithStartTimeList);
		return Pair.of(estaWithStartTimeList.getFirst(), previousReservation);
	}

	private Pair<Establishment, List<LocalTime>> seekTheNextEstablishmentWithStartTime() {
		Establishment establishment;
		int indexSlug = 0;
		List<LocalTime> startTimeListSorted;
		do {
			establishment = nextSlugToBookOn(indexSlug++);
			startTimeListSorted = listStartTimeSorted(establishment);
		} while (startTimeListSorted.isEmpty());
		return Pair.of(establishment, startTimeListSorted);
	}

	private void firstInsertionInPreviousReservationAndPreviousTisch(
			Pair<Establishment, List<LocalTime>> establishmentToBookOn,
			PreviousReservation previousReservation) {
		if (previousReservationPresent()) {
			throw new AssertionError();
		}
		List<Tisch> tablesPerCapacity = this.listTablesPerCapacityForThatEstablishment(establishmentToBookOn.getFirst());
		extendPreviousEstablishmentWithListTables(previousReservation, tablesPerCapacity, establishmentToBookOn.getSecond());
	}

	private Establishment nextSlugToBookOn(int indexSlug) {
		List<String> distinctSlugs = this.establishmentRepository.findDistinctSlugs()
				.stream().filter(s -> !ReservationAddImpl.RESTAURANT_TO_EXCLUDE.contains(s)).toList();
		if (distinctSlugs.isEmpty()) {
			throw new IllegalStateException("No establishment found in table establishment");
		}
		if (distinctSlugs.size() <= indexSlug) {
			throw new IllegalStateException(" indexSlug greater than distinctSlugs size");
		}
		long numberOfRow = previousReservationRepository.findAll().size();
		if (numberOfRow > 1) {
			throw new IllegalStateException("table previous_reservation should have maximum one row");
		} else if (numberOfRow == 0) {
			return this.establishmentRepository.findBySlug(distinctSlugs.get(indexSlug)).orElseThrow();
		} else {
			PreviousReservation previousReservation = previousReservationRepository.findAll().stream().findFirst().orElseThrow();
			String previousSlug = previousReservation.getEstablishment().getSlug();
			int i = distinctSlugs.indexOf(previousSlug);
			String slug = distinctSlugs.get((i + 1 + indexSlug) % distinctSlugs.size());
			return this.establishmentRepository.findBySlug(slug).orElseThrow();
		}
	}

	private @NonNull List<LocalTime> listStartTimeSorted(Establishment establishmentToBookOn) {
		LocalDate reservationDate = LocalDate.now().plusDays(1);
		return listStartTimeSlotForThatDate.getListStartTimeSorted(establishmentToBookOn, reservationDate);
	}

	private List<Tisch> listTablesPerCapacityForThatEstablishment (Establishment establishment) {
		List<Tisch> allByEstablishment = tischRepository.findAllByEstablishment(establishment);
		return listCapacityOrder.stream()
				.flatMap(capacity -> allByEstablishment.stream()
						.filter(table -> Objects.equals(table.getCapacity(), capacity)))
				.toList();
	}

	private boolean previousReservationPresent() {
		long numberOfRow = previousReservationRepository.findAll().size();
		return numberOfRow != 0;
	}

	private PreviousReservation getOrInsertPreviousReservation(Pair<Establishment, List<LocalTime>> estaWithStartTimeList) {
		if (previousReservationPresent()) {
			PreviousReservation previousReservation = previousReservationRepository.findAll().stream().findFirst().orElseThrow();
			assert previousReservation.getEstablishment() == estaWithStartTimeList.getFirst();
			return previousReservation;
		}
		PreviousReservation previousReservation = buildPreviousEstablishment(estaWithStartTimeList.getFirst());
		firstInsertionInPreviousReservationAndPreviousTisch(estaWithStartTimeList, previousReservation);
		return previousReservation;
	}

	private void extendPreviousEstablishmentWithListTables(PreviousReservation previousReservation, List<Tisch> tablesPerCapacity,
			List<LocalTime> startTimeListSorted) {
		List<PreviousTischEntry> previousTischEntries = createPreviousTischEntries(tablesPerCapacity);
		previousReservation.setPreviousTischEntries(previousTischEntries);
		previousReservation.setListStartTime(startTimeListSorted);
		this.entityManager.persist(previousReservation);
	}

	private @NonNull List<PreviousTischEntry> createPreviousTischEntries(List<Tisch> tablesPerCapacity) {
		List<PreviousTischEntry> previousTischEntries = new ArrayList<>();
		tablesPerCapacity.forEach(t -> previousTischEntries.add(PreviousTischEntry.builder()
				.capacity(t.getCapacity())
				.tableId(t.getId())
				.build()));
		return previousTischEntries;
	}

	private PreviousReservation buildPreviousEstablishment(Establishment establishment) {
		return PreviousReservation.builder()
				.establishment(establishment)
				.previousHttpStatus(200)
				.intervalWithPrevious(15)
				.success(true)
				.paramReservationState(new ParamReservationState(0, 0))
				.build();
	}

}
