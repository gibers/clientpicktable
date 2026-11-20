package com.hackpicktable.clientpicktable.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackpicktable.clientpicktable.entities.PreviousReservation;

public interface PreviousReservationRepository extends JpaRepository<PreviousReservation, UUID> {
}
