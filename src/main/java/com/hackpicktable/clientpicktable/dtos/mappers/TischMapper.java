package com.hackpicktable.clientpicktable.dtos.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.TischDto;
import com.hackpicktable.clientpicktable.entities.Tisch;

@Component
public class TischMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public TischDto toDto(Tisch tisch) {
        return modelMapper.map(tisch, TischDto.class);
    }

    public Tisch toDto(TischDto tischDto) {
        return modelMapper.map(tischDto, Tisch.class);
    }

}
