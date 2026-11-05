package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookATableRequestDto {

	private int numberOfPeople;
	private LocalTime startTime;
	private LocalDate dateReservation;

}

