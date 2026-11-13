package com.hackpicktable.clientpicktable.dtos.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.EstablishmentDto;
import com.hackpicktable.clientpicktable.entities.Establishment;

@Component
public class EstablishmentMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public EstablishmentDto toDto(Establishment establishment) {
        return modelMapper.map(establishment, EstablishmentDto.class);
    }

    public Establishment toEntity(EstablishmentDto establishmentDTO) {
        return modelMapper.map(establishmentDTO, Establishment.class);
    }
}
