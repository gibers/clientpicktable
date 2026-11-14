package com.hackpicktable.clientpicktable.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackpicktable.clientpicktable.dtos.ReservationSettingsDto;
import com.hackpicktable.clientpicktable.impl.RefreshReservationSettingsInDBImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
@Slf4j
public class ReservationSettingsController {

	private final RefreshReservationSettingsInDBImpl refreshReservationSettingsImpl;

	@GetMapping(value = "/{establishmentSlug}")
	public ReservationSettingsDto refreshReservationSettings(@PathVariable String establishmentSlug) {
		return refreshReservationSettingsImpl.refreshReservationSettingsImpl(establishmentSlug);
	}

	@GetMapping(value = "/all")
	public List<UUID> refreshAllReservationSettings() {
		return refreshReservationSettingsImpl.refreshAllReservationSettingsImpl();
	}

}
