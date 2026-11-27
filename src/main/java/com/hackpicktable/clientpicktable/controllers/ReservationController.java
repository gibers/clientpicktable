package com.hackpicktable.clientpicktable.controllers;

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackpicktable.clientpicktable.dtos.BookATableRequestDto;
import com.hackpicktable.clientpicktable.dtos.PreviousReservationDto;
import com.hackpicktable.clientpicktable.dtos.ReservationToSaveDto;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;
import com.hackpicktable.clientpicktable.impl.ReservationAddImpl;
import com.hackpicktable.clientpicktable.impl.ReservationToSaveImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
@Slf4j
public class ReservationController {

	private final ReservationAddImpl reservationAddImpl;
	private final ReservationToSaveImpl reservationToSaveImpl;

	@PostMapping(value = "/book/{establishmentName}")
	public ReservationResponse addAReservation(@PathVariable String establishmentName, @RequestBody BookATableRequestDto request) {
		return reservationAddImpl.bookAReservation(establishmentName);
	}

	@PostMapping(value = "/book-all")
	public List<ReservationResponse> bookOneReservationInAllEstablishment() {
		return reservationAddImpl.bookAReservationForAllEstablishment();
	}

	@GetMapping("/nextEstablishment")
	public PreviousReservationDto getNextEstablishmentToBookOn() {
		return reservationAddImpl.getPreviousReservationImpl();
	}

	@PostMapping("/saveResa")
	public void saveResa(@RequestBody ReservationToSaveDto request) {
		reservationToSaveImpl.saveReservation(request);
	}

	@PostMapping(value = "/book-test")
	public List<ReservationResponse> bookTestForPreviousReservation() {
		return Collections.singletonList(reservationAddImpl.bookAReservationByScheduling());
	}

}
