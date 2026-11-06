package com.hackpicktable.clientpicktable.impl;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
import com.hackpicktable.clientpicktable.dtos.ReservationResponseDto;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.LastPatronymicUsed;
import com.hackpicktable.clientpicktable.entities.Patronymic;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.LastPatronymicUsedRepository;
import com.hackpicktable.clientpicktable.repositories.PatronymicRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationAddImpl {

	private final EstablishmentRepository establishmentRepository;
	private final LastPatronymicUsedRepository lastPatronymicUsedRepository;
	private final PatronymicRepository patronymicRepository;
	private final ObjectProvider<ReservationParamRequest> reservationParamRequestProvider;

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
		// 4. book a table:
		return reservationParamRequestProvider.getObject().setParamAndLaunch(
				establishmentFromDB, patronymicToUse, bookATableRequestDto.getNumberOfPeople(), bookATableRequestDto.getStartTime(),
				bookATableRequestDto.getDateReservation());
	}

}
