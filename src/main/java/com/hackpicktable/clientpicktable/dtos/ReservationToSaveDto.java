package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Data
public class ReservationToSaveDto {

	private String firstName;
	private String lastName;
	private String email;
	private String publicIP;

	private String errorMessage;
	private UUID reservationId;
	private Integer httpStatus;
	private Boolean success;
	private UUID establishmentId;
	private LocalDate dateReservation;
	private LocalTime startTime;
	private UUID tischId;
	private int numberOfGuests;

}
