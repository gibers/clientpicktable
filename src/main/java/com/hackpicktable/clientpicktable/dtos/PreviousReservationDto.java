package com.hackpicktable.clientpicktable.dtos;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackpicktable.clientpicktable.entities.jsonobjects.ParamReservationState;

public record PreviousReservationDto(
		String slug,
        @JsonProperty("establishment_id") UUID establishmentId,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("previous_http_status") Integer previousHttpStatus,
        @JsonProperty("interval_with_previous") Integer intervalWithPrevious,
        @JsonProperty("success") Boolean success,
        @JsonProperty("param_reservation_state") ParamReservationState paramReservationState,
        @JsonProperty("list_start_time") List<LocalTime> listStartTime) {
}
