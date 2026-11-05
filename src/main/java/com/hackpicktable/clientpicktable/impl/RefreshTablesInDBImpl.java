package com.hackpicktable.clientpicktable.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.TischDto;
import com.hackpicktable.clientpicktable.dtos.mappers.TischMapper;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.TischRepository;
import com.hackpicktable.clientpicktable.restcall.AllTablesFromSupabase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTablesInDBImpl {

	private final AllTablesFromSupabase allTablesFromSupabase;
	private final TischMapper tischMapper;
	private final TischRepository tischRepository;
	private final EstablishmentRepository establishmentRepository;

	@Transactional
	public List<TischDto> refreshTables(String establishmentSlug) {
		// 1. get the establishment from DB:
		Establishment establishmentZaoCafe = establishmentRepository.findBySlug(establishmentSlug).orElseThrow();
		// 2. call the remote supabase Endpoint from that establishmentId:
		List<TischDto> allTischDto = allTablesFromSupabase.getAllTables(establishmentZaoCafe.getId());
		stopProcessIfSupabaseFailed(establishmentSlug, allTischDto);
		// 3. empty table Tisch in DB, before to insert new data:
		tischRepository.deleteAllByEstablishment(establishmentZaoCafe);
		// 4. save all TischDto in DB:
		saveAllTischDto(allTischDto);
		return allTischDto;
	}

	private void stopProcessIfSupabaseFailed(String establishmentName, List<TischDto> listTischDto) {
		if (listTischDto == null || listTischDto.isEmpty()) {
			log.error("list Tisch not found in Supabase for {}", establishmentName);
			throw new RuntimeException("Establishment not found in Supabase");
		}
	}

	private void saveAllTischDto(List<TischDto> allTischDto) {
		List<Tisch> listTisch = allTischDto.stream()
				.map(tischMapper::toDto)
				.toList();
		tischRepository.saveAll(listTisch);
	}

}
