package com.hackpicktable.clientpicktable.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class PreviousTischEntry {

    @Column(name = "capacity", nullable = false)
    @JsonProperty("capacity")
    private Integer capacity;

    @Column(name = "table_id", nullable = false)
    @JsonProperty("table_id")
    private UUID tableId;

}
