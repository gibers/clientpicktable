package com.hackpicktable.clientpicktable.entities;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackpicktable.clientpicktable.entities.jsonobjects.ParamReservationState;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "previous_reservation", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PreviousReservation {

    @Id
    @Column(name = "establishment_id", nullable = false)
    @JsonProperty("establishment_id")
    private UUID establishmentId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonIgnore
    private Establishment establishment;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "previous_http_status", nullable = false)
    @JsonProperty("previous_http_status")
    private Integer previousHttpStatus;

    @Column(name = "interval_with_previous", nullable = false)
    @JsonProperty("interval_with_previous")
    private Integer intervalWithPrevious;

    @Column(name = "success", nullable = false)
    @JsonProperty("success")
    private Boolean success;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "param_reservation_state", nullable = false, columnDefinition = "json")
    @JsonProperty("param_reservation_state")
    private ParamReservationState paramReservationState;

    @Column(name = "list_start_time", columnDefinition = "time[]")
    @JsonProperty("list_start_time")
    private List<LocalTime> listStartTime;

    @ElementCollection
    @CollectionTable(
            name = "previous_tisch",
            schema = "public",
            joinColumns = @JoinColumn(name = "establishment_id", referencedColumnName = "establishment_id"))
    @OrderColumn(name = "previous_tisch_order")
    @JsonIgnore
    @Builder.Default
    private List<PreviousTischEntry> previousTischEntries = new ArrayList<>();

}
