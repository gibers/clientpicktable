package com.hackpicktable.clientpicktable.dtos.mappers;

import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.PreviousReservationDto;
import com.hackpicktable.clientpicktable.entities.PreviousReservation;

@Component
public class PreviousReservationMapper {

    public PreviousReservationDto toDto(PreviousReservation previousReservation, String slug) {
        return new PreviousReservationDto(
                slug,
                previousReservation.getEstablishmentId(),
                previousReservation.getUpdatedAt(),
                previousReservation.getPreviousHttpStatus(),
                previousReservation.getIntervalWithPrevious(),
                previousReservation.getSuccess(),
                previousReservation.getParamReservationState(),
                previousReservation.getListStartTime());
    }
}
