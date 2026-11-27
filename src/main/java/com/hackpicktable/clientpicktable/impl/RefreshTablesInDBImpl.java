package com.hackpicktable.clientpicktable.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.TischDto;
import com.hackpicktable.clientpicktable.dtos.mappers.TischMapper;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.repositories.EstablishmentRepository;
import com.hackpicktable.clientpicktable.repositories.ReservationResponseRepository;
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
	private final ReservationResponseRepository reservationResponseRepository;

	@Transactional
	public List<TischDto> refreshTablesForThisEstablishment(String establishmentSlug) {
		// 1. get the establishment from DB:
		Establishment establishment = establishmentRepository.findBySlug(establishmentSlug).orElseThrow();
		// 2. call the remote supabase Endpoint from that establishmentId:
		List<TischDto> allTischDto = allTablesFromSupabase.getAllTables(establishment.getId());
		stopProcessIfSupabaseFailed(establishmentSlug, allTischDto);
		// 3. empty table Tisch in DB, before to insert new data:
		tischRepository.deleteAllByEstablishment(establishment);
		// 4. save all TischDto in DB:
		saveAllTischDto(allTischDto);
		return allTischDto;
	}

	@Transactional
	public List<UUID> refreshAllTables() {
		// 1. retrieve all establishments from DB
		List<Establishment> establishmentsFromDB = establishmentRepository.findAll();
		// 2. retrieve all TischDto from Supabase for all establishments:
		Map<Establishment, List<TischDto>> establishmentTischDtoMap = new HashMap<>();
		establishmentsFromDB.forEach(establishment ->
				establishmentTischDtoMap.computeIfAbsent(establishment, e -> allTablesFromSupabase.getAllTables(e.getId())));
		// 3. delete and save all non null or empty TischDto in DB:
		deleteAndSaveInTisch(establishmentTischDtoMap);
		// 4. return all establishments with empty TischDto, we should have 0 establishments, otherwise we can't book tables for that
		// establishment
		return establishmentTischDtoMap.entrySet().stream()
				.filter(entry -> entry.getValue() == null || entry.getValue().isEmpty())
				.map(x -> x.getKey().getId())
				.peek(x -> log.warn("list Tisch not found or empty in Supabase for establishment {}", x))
				.toList();
	}

	private void deleteAndSaveInTisch(Map<Establishment, List<TischDto>> establishmentTischDtoMap) {
		establishmentTischDtoMap.entrySet().stream()
				.filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
				.forEach((establishment) -> {
					gapBetweenTischInDBAndTischFromSupabase(establishment.getKey(), establishment.getValue());
				});
	}

	private void gapBetweenTischInDBAndTischFromSupabase(Establishment establishment, List<TischDto> listTischDto) {
		List<Tisch> allTableForThatEstablishment =
				this.tischRepository.findAllByEstablishment(establishment);
		// 1. insert new Tisch.
		listTischDto.stream()
				.filter(x -> allTableForThatEstablishment.stream().noneMatch(y -> y.getId().equals(x.getId())))
				.peek(x -> log.debug("Inserting new Tisch: {} in establishment: {}", x, establishment.getName()))
				.forEach(x -> this.tischRepository.save(tischMapper.toDto(x)));
		// 2. delete old Tisch in reservation_response and tisch.
		allTableForThatEstablishment.stream()
				.filter(x -> listTischDto.stream().noneMatch(y -> y.getId().equals(x.getId())))
				.forEach(x -> {
					log.debug("Deleting old Tisch: {} of establishment: {} in reservation_response and tisch", x, establishment.getName());
					this.tischRepository.delete(x);
				});
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
