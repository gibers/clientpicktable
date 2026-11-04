package com.hackpicktable.clientpicktable.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackpicktable.clientpicktable.dtos.TischDto;
import com.hackpicktable.clientpicktable.impl.RefreshTablesInDBImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tables")
@Slf4j
public class TableListController {

	private final RefreshTablesInDBImpl refreshTablesInDBImpl;

	@GetMapping(value = "/{establishmentName}")
	public List<TischDto> getAllTables(@PathVariable String establishmentName) {
		return refreshTablesInDBImpl.refreshTables(establishmentName);
	}

}
