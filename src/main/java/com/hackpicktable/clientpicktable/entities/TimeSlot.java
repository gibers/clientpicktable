package com.hackpicktable.clientpicktable.entities;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "time_slot", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class TimeSlot {

    @Id
    @Column(name = "id", nullable = false)
    @JsonProperty("id")
    private UUID id;

    @Column(name = "start_time")
    @JsonProperty("start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    @JsonProperty("end_time")
    private LocalTime endTime;

    @Column(name = "is_active")
    @JsonProperty("is_active")
    private Boolean active;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "available_days", columnDefinition = "integer[]")
    @JsonProperty("available_days")
    private TimeSlotDay[] availableDays;

    @ManyToOne
    @JoinColumn(name = "establishment_id")
    private Establishment establishment;

    @Column(name = "duration_override_minutes")
    @JsonProperty("duration_override_minutes")
    private Integer durationOverrideMinutes;
}
