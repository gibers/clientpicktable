package com.hackpicktable.clientpicktable.dtos.mappers;

import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.ReservationResponseDto;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;

@Component
public class ReservationResponseMapper {

    public ReservationResponse toEntity(ReservationResponseDto dto) {
        if (dto == null) {
            return null;
        }

        ReservationResponse entity = new ReservationResponse();
        entity.setSuccess(dto.getSuccess());
        entity.setHttpStatus(dto.getHttpStatus());
        entity.setErrorMessage(dto.getErrorMessage());

        ReservationResponseDto.Reservation reservation = dto.getReservation();
        if (reservation != null) {
            entity.setReservationId(reservation.getIdReservation());
            entity.setDate(reservation.getDate());
            entity.setStartTime(reservation.getStartTime());
            entity.setNumberOfPeople(reservation.getNumberOfPeople());
        }

        return entity;
    }

    public ReservationResponseDto toDto(ReservationResponse entity) {
        if (entity == null) {
            return null;
        }

        ReservationResponseDto.Reservation reservation = ReservationResponseDto.Reservation.builder()
                .idReservation(entity.getReservationId())
                .date(entity.getDate())
                .startTime(entity.getStartTime())
                .numberOfPeople(entity.getNumberOfPeople())
                .build();

        return ReservationResponseDto.builder()
                .success(entity.getSuccess())
                .httpStatus(entity.getHttpStatus())
                .errorMessage(entity.getErrorMessage())
                .reservation(reservation)
                .build();
    }
}
