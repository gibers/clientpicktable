package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

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
public class BookATableForSupabaseRequestDto {

    private String slug;
    private UUID establishmentId;
    private Customer customer;
    private Reservation reservation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Customer {
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private Boolean marketingConsent;
        private Boolean smsConsent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Reservation {

        private LocalDate date;
        private LocalTime startTime;
        private UUID tableId;
        private UUID combinedTableId;
        private Integer numberOfPeople;
        private Integer numberOfChildren;
        private Boolean hasStroller;
        private String terracePreference;
    }
}
