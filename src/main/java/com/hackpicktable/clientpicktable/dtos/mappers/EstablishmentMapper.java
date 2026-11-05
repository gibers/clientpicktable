package com.hackpicktable.clientpicktable.dtos.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.EstablishmentDTO;
import com.hackpicktable.clientpicktable.entities.Establishment;

@Component
public class EstablishmentMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public EstablishmentDTO toDto(Establishment establishment) {
        return modelMapper.map(establishment, EstablishmentDTO.class);
    }

    public Establishment toEntity(EstablishmentDTO establishmentDTO) {
        return modelMapper.map(establishmentDTO, Establishment.class);
    }
}
