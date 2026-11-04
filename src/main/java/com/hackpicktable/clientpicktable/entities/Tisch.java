package com.hackpicktable.clientpicktable.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tisch", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Tisch {

    @Id
    @Column(name = "id", nullable = false)
    @JsonProperty("id")
    private UUID id;

    @Column(name = "name", nullable = false, length = 70)
    @JsonProperty("name")
    private String name;

    @Column(name = "capacity", nullable = false)
    @JsonProperty("capacity")
    private Integer capacity;

    @Column(name = "is_active", nullable = false)
    @JsonProperty("is_active")
    private Boolean active;

    @Column(name = "notes", columnDefinition = "text")
    @JsonProperty("notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @Column(name = "is_bookable", nullable = false)
    @JsonProperty("is_bookable")
    private Boolean bookable;

    @Column(name = "allows_children", nullable = false)
    @JsonProperty("allows_children")
    private Boolean allowsChildren;

    @Column(name = "allows_stroller", nullable = false)
    @JsonProperty("allows_stroller")
    private Boolean allowsStroller;

    @Column(name = "is_terrace", nullable = false)
    @JsonProperty("is_terrace")
    private Boolean terrace;

    @Column(name = "min_capacity", nullable = false)
    @JsonProperty("min_capacity")
    private Integer minCapacity;

    @Column(name = "assignment_priority", nullable = false)
    @JsonProperty("assignment_priority")
    private Integer assignmentPriority;

    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "zone_id")
    @JsonProperty("zone_id")
    private UUID zoneId;

    @Column(name = "display_order", nullable = false)
    @JsonProperty("display_order")
    private Integer displayOrder;
}
