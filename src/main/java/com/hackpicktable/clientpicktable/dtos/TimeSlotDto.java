package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TimeSlotDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("start_time")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("available_days")
    private List<Integer> availableDays;

    @JsonProperty("establishment_id")
    private UUID establishmentId;

    @JsonProperty("duration_override_minutes")
    private Integer durationOverrideMinutes;
}
