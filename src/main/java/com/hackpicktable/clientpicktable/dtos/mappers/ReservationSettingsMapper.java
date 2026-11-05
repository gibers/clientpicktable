package com.hackpicktable.clientpicktable.dtos.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.ReservationSettingsDto;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.ReservationSettings;

@Component
public class ReservationSettingsMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public ReservationSettingsDto toDto(ReservationSettings reservationSettings) {
        ReservationSettingsDto dto = modelMapper.map(reservationSettings, ReservationSettingsDto.class);
        dto.setEstablishmentId(reservationSettings.getEstablishment().getId());
        return dto;
    }

    public ReservationSettings toEntity(ReservationSettingsDto dto) {
        ReservationSettings entity = modelMapper.map(dto, ReservationSettings.class);
        Establishment establishment = new Establishment();
        establishment.setId(dto.getEstablishmentId());
        entity.setEstablishment(establishment);
        return entity;
    }
}
