package com.hackpicktable.clientpicktable.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservation_response", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ReservationResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "reservation_id", unique = true)
    private UUID reservationId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patronymic_id", nullable = false)
    private Patronymic patronymic;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tisch_id", nullable = false)
    private Tisch tisch;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(name = "error_message")
    private String errorMessage = "";

    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;

}
