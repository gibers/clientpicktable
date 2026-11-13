package com.hackpicktable.clientpicktable.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackpicktable.clientpicktable.dtos.TimeSlotDto;
import com.hackpicktable.clientpicktable.impl.RefreshTimeSlotInDBImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/timeSlot")
public class TimeSlotController {

	private final RefreshTimeSlotInDBImpl refreshTablesInDBImpl;

	@PutMapping(value = "/all")
	public List<TimeSlotDto> refreshAllTimeSlotInDB() {
		return refreshTablesInDBImpl.refreshAllTimeSlotInDB();
	}


}
