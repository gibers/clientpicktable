package com.hackpicktable.clientpicktable.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hackpicktable.clientpicktable.dtos.TischDto;
import com.hackpicktable.clientpicktable.dtos.mappers.TischMapper;
import com.hackpicktable.clientpicktable.entities.Tisch;
import com.hackpicktable.clientpicktable.repositories.TischRepository;
import com.hackpicktable.clientpicktable.restcall.AllTablesFromSupabase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTablesInDBImpl {

	private final AllTablesFromSupabase allTablesFromSupabase;
	private final TischMapper tischMapper;
	private final TischRepository tischRepository;

	public List<TischDto> refreshTables(String establishmentName) {

		// todo: 1. récupérer l'UUID depuis la table establishment au lieu de ce hardcode:
		UUID uuidFromZaoCafe = UUID.fromString("35e5c927-7eac-4338-b315-67dc7dc23bf7");

		List<TischDto> allTables = allTablesFromSupabase.getAllTables(uuidFromZaoCafe);
		if (allTables == null || allTables.isEmpty()) {
			log.error("No tables found for this establishment in Supabase");
			return null;
		}
		// 1. empty table Tisch in DB:
		tischRepository.deleteAll();

		List<Tisch> listTisch = allTables.stream().map(tischMapper::toDto).toList();

		tischRepository.saveAll(listTisch);
		return allTables;
	}

}
