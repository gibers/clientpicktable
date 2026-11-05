package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ReservationResponseDto {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("httpStatus")
    private Integer httpStatus;

    @JsonProperty("error")
    @Builder.Default
    private String errorMessage = "";

    @JsonProperty("reservation")
    private Reservation reservation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reservation {

        @JsonProperty("id")
        private UUID idReservation;

        @JsonProperty("date")
        private LocalDate date;

        @JsonProperty("startTime")
        private LocalTime startTime;

        @JsonProperty("numberOfPeople")
        private Integer numberOfPeople;
    }
}
