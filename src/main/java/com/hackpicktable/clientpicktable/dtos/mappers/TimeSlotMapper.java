package com.hackpicktable.clientpicktable.dtos.mappers;

import org.springframework.stereotype.Component;

import com.hackpicktable.clientpicktable.dtos.TimeSlotDto;
import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.TimeSlot;
import com.hackpicktable.clientpicktable.entities.TimeSlotDay;

@Component
public class TimeSlotMapper {

    public TimeSlot toEntity(TimeSlotDto dto, Establishment establishment) {
        TimeSlot entity = new TimeSlot();
        entity.setId(dto.getId());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setActive(dto.getActive());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setAvailableDays(dto.getAvailableDays() == null ? null : dto.getAvailableDays().stream()
                .map(day -> TimeSlotDay.values()[day])
                .toArray(TimeSlotDay[]::new));
        entity.setEstablishment(establishment);
        entity.setDurationOverrideMinutes(dto.getDurationOverrideMinutes());
        return entity;
    }
}
