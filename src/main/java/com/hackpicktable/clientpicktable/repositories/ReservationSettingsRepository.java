package com.hackpicktable.clientpicktable.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackpicktable.clientpicktable.entities.ReservationSettings;

public interface ReservationSettingsRepository extends JpaRepository<ReservationSettings, UUID> {
}
