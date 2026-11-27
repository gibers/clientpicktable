package com.hackpicktable.clientpicktable.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.ReservationToSaveDto;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.Patronymic;
import com.hackpicktable.clientpicktable.entities.PreviousReservation;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.PatronymicRepository;
import com.hackpicktable.clientpicktable.repositories.PreviousReservationRepository;
import com.hackpicktable.clientpicktable.repositories.ReservationResponseRepository;
import com.hackpicktable.clientpicktable.repositories.TischRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationToSaveImpl {

	private final PatronymicRepository patronymicRepository;
	private final EstablishmentRepository establishmentRepository;
	private final TischRepository tischRepository;
	private final ReservationResponseRepository reservationResponseRepository;
	private final PreviousReservationRepository previousReservationRepository;

	@Transactional
	public void saveReservation(ReservationToSaveDto reservationToSaveDto) {
		// 1. save customer data:
		Patronymic patronymicSaved = selectOrInsertNewPatronymicInDb(reservationToSaveDto);
		// 2. save reservation data:
		insertNewReservationResponseInDb(reservationToSaveDto, patronymicSaved);
		// 3. update previous_reservation
		updatePreviousReservation(reservationToSaveDto);
	}

	private void updatePreviousReservation(ReservationToSaveDto reservationToSaveDto) {
		Optional<PreviousReservation> previousReservationOptional =
				previousReservationRepository.findById(reservationToSaveDto.getEstablishmentId());
		if (previousReservationOptional.isEmpty()) {
			log.error("Previous reservation does not match with the establishment_id from reservationToSaveDto: {}",
					reservationToSaveDto.getEstablishmentId());
			throw new IllegalArgumentException("Previous reservation does not match with the establishment_id from reservationToSaveDto");
		}
		PreviousReservation previousReservation = previousReservationOptional.get();
		previousReservation.setPreviousHttpStatus(reservationToSaveDto.getHttpStatus());
		previousReservation.setSuccess(reservationToSaveDto.getSuccess());
		previousReservationRepository.save(previousReservation);
	}

	private void insertNewReservationResponseInDb(ReservationToSaveDto reservationToSaveDto, Patronymic patronymicSaved) {
		Establishment establishment = this.establishmentRepository.findById(reservationToSaveDto.getEstablishmentId()).orElseThrow();
		ReservationResponse build = ReservationResponse.builder()
				.reservationId(reservationToSaveDto.getReservationId())
				.establishment(establishment)
				.patronymic(patronymicSaved)
				.tischId(reservationToSaveDto.getTischId())
				.date(reservationToSaveDto.getDateReservation())
				.startTime(reservationToSaveDto.getStartTime())
				.numberOfPeople(reservationToSaveDto.getNumberOfGuests())
				.success(reservationToSaveDto.getSuccess())
				.httpStatus(reservationToSaveDto.getHttpStatus())
				.publicIp(reservationToSaveDto.getPublicIP())
				.errorMessage(reservationToSaveDto.getErrorMessage())
				.build();
		this.reservationResponseRepository.save(build);
	}

	private Patronymic selectOrInsertNewPatronymicInDb(ReservationToSaveDto reservationToSaveDto) {
		return this.patronymicRepository.findByFirstnameAndNameAndEmail(reservationToSaveDto.getFirstName(), reservationToSaveDto.getLastName(),
				reservationToSaveDto.getEmail()).stream().findFirst().orElseGet (() -> {
			Patronymic patronymic = Patronymic.builder()
					.firstname(reservationToSaveDto.getFirstName())
					.name(reservationToSaveDto.getLastName())
					.email(reservationToSaveDto.getEmail())
					.build();
			return patronymicRepository.save(patronymic);
		});
	}

}
