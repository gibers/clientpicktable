package com.hackpicktable.clientpicktable.impl;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.ReservationSettingsDto;
import com.hackpicktable.clientpicktable.dtos.mappers.ReservationSettingsMapper;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.ReservationSettings;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.ReservationSettingsRepository;
import com.hackpicktable.clientpicktable.restcall.ReservationSettingsFromSupabase;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshReservationSettingsInDBImpl {

	private final ReservationSettingsFromSupabase reservationSettingsFromSupabase;
	private final EstablishmentRepository establishmentRepository;
	private final ReservationSettingsRepository reservationSettingsRepository;
	private final ReservationSettingsMapper reservationSettingsMapper;
	private final Validator validator;

	@Transactional
	public ReservationSettingsDto refreshReservationSettingsImpl(String establishmentSlug) {
		// 1. Retrieve establishment from DB.
		Establishment establishmentFromDB = establishmentRepository.findBySlug(establishmentSlug).orElseThrow();
		// 2. Retrieve reservation settings from Supabase.
		ReservationSettingsDto reservationSettings = reservationSettingsFromSupabase.getReservationSettings(establishmentFromDB.getId());
		stopProcessIfSupabaseFailed(establishmentSlug, reservationSettings);
		// 3. empty table ReservationSettings only if supabase did not fail.
		reservationSettingsRepository.deleteByEstablishment(establishmentFromDB);
		// 4. save the supabase result in DB.
		ReservationSettings reservationSettingsEntity = reservationSettingsMapper.toEntity(reservationSettings);
		reservationSettingsRepository.save(reservationSettingsEntity);
		return reservationSettings;
	}

	private void stopProcessIfSupabaseFailed(String establishmentSlug, ReservationSettingsDto reservationSettingsDto) {
		// I don't think supabase will return a null, defensive code.
		if (reservationSettingsDto == null) {
			log.error("Establishment not found in Supabase for {}", establishmentSlug);
			throw new RuntimeException("ReservationSettings not found in Supabase for that establishment");
		}
		var violations = validator.validate(reservationSettingsDto);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}

}
