package com.hackpicktable.clientpicktable.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TischDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("capacity")
    private Integer capacity;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("establishment_id")
    private UUID establishmentId;

    @JsonProperty("is_bookable")
    private Boolean bookable;

    @JsonProperty("allows_children")
    private Boolean allowsChildren;

    @JsonProperty("allows_stroller")
    private Boolean allowsStroller;

    @JsonProperty("is_terrace")
    private Boolean terrace;

    @JsonProperty("min_capacity")
    private Integer minCapacity;

    @JsonProperty("assignment_priority")
    private Integer assignmentPriority;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("zone_id")
    private UUID zoneId;

    @JsonProperty("display_order")
    private Integer displayOrder;
}
