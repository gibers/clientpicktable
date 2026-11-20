package com.hackpicktable.clientpicktable.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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

	@Transactional
	public List<UUID> refreshAllReservationSettingsImpl() {
		// 1. retrieve all establishments from DB
		List<Establishment> establishmentsFromDB = establishmentRepository.findAll();
		// 2. for each establishment, retrieve all reservation_settings from Supabase
		Map<Establishment, ReservationSettingsDto> reservationSettingsDtoMap = new HashMap<>();
		establishmentsFromDB.forEach(establishment ->
				reservationSettingsDtoMap.computeIfAbsent(establishment,
						(e) -> reservationSettingsFromSupabase.getReservationSettings(e.getId())));
		// 3.save each reservation_settings in DB
		Stream<Map.Entry<Establishment, ReservationSettingsDto>> entryStream =
				reservationSettingsDtoMap.entrySet().stream().filter(entry -> entry.getValue() != null);
		saveEachInDB(entryStream);
		// 4. return list of establishments not found in Supabase (should be empty, otherwise, we can't book a table for those establishments)
		return reservationSettingsDtoMap.entrySet().stream().filter(entry -> entry.getValue() == null)
				.map(x -> x.getKey().getId())
				.peek(x -> log.warn("Establishment not found in Supabase for {}", x))
				.toList();
	}

	private void saveEachInDB(Stream<Map.Entry<Establishment, ReservationSettingsDto>> entryStream) {
		entryStream.forEach(entry -> {
			var violations = validator.validate(entry.getValue());
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
			log.debug("try to delete in reservation_settings for establishment {} saved", entry.getKey().getId());
			reservationSettingsRepository.deleteByEstablishment(entry.getKey());
			ReservationSettings reservationSettingsEntity = reservationSettingsMapper.toEntity(entry.getValue());
			log.debug("Reservation settings for establishment {} saved", entry.getKey().getId());
			reservationSettingsRepository.save(reservationSettingsEntity);
		});
	}

}
