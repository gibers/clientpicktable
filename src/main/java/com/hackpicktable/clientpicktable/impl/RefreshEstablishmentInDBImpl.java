package com.hackpicktable.clientpicktable.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.EstablishmentDto;
import com.hackpicktable.clientpicktable.dtos.mappers.EstablishmentMapper;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.restcall.EstablishmentFromSupabase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshEstablishmentInDBImpl {

	private final EstablishmentFromSupabase establishmentFromSupabase;
	private final EstablishmentRepository establishmentRepository;
	private final EstablishmentMapper establishmentMapper;

	public EstablishmentDto refreshEstablishmentInDB(String establishmentName) {
		// 1. call remote Supabase.
		EstablishmentDto establishmentDto = establishmentFromSupabase.getEstablishmentBySlug(establishmentName);
		stopProcessIfSupabaseFailed(establishmentName, establishmentDto);
		// 2. empty table Establishment only if supabase did not fail.
		establishmentRepository.deleteByName(establishmentName);
		// 3. save the supabase result in DB.
		Establishment entity = establishmentMapper.toEntity(establishmentDto);
		establishmentRepository.save(entity);
		return establishmentDto;
	}

	void refreshEstablishmentInDBForMissings(List<UUID> establishmentIds) {
		establishmentIds.stream()
				.map(establishmentFromSupabase::getEstablishmentById)
				.filter(Objects::nonNull)
				.forEach(establishmentDto -> {
					Establishment entity = establishmentMapper.toEntity(establishmentDto);
					establishmentRepository.save(entity);
				});
	}

	private void stopProcessIfSupabaseFailed(String establishmentName, EstablishmentDto establishmentDto) {
		// I don't think supabase will return a null, defensive code.
		if (establishmentDto == null) {
			log.error("Establishment not found in Supabase for {}", establishmentName);
			throw new RuntimeException("Establishment not found in Supabase");
		}
	}

}
