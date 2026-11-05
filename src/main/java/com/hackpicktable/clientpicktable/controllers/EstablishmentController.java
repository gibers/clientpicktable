package com.hackpicktable.clientpicktable.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackpicktable.clientpicktable.dtos.EstablishmentDTO;
import com.hackpicktable.clientpicktable.impl.RefreshEstablishmentInDBImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/establishment")
@Slf4j
public class EstablishmentController {

	private final RefreshEstablishmentInDBImpl refreshTablesInDBImpl;

	@GetMapping(value = "/{establishmentName}")
	public EstablishmentDTO refreshEstablishmentInDB(@PathVariable String establishmentName) {
		return refreshTablesInDBImpl.refreshTablesInDB(establishmentName);
	}

}
