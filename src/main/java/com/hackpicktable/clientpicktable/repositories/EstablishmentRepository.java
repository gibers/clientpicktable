package com.hackpicktable.clientpicktable.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackpicktable.clientpicktable.entities.Establishment;

public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {
}
