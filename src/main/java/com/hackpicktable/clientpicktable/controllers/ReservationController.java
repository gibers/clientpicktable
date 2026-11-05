package com.hackpicktable.clientpicktable.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
import com.hackpicktable.clientpicktable.dtos.ReservationResponseDto;
import com.hackpicktable.clientpicktable.impl.ReservationAddImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
@Slf4j
public class ReservationController {

	private final ReservationAddImpl reservationAddImpl;

	@PostMapping(value = "/book/{establishmentName}")
	public ReservationResponseDto addAReservation(@PathVariable String establishmentName, @RequestBody BookATableRequestDto request) {
		return reservationAddImpl.bookAReservation(establishmentName, request);
	}

}
