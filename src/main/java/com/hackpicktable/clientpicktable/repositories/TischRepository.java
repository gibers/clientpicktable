package com.hackpicktable.clientpicktable.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackpicktable.clientpicktable.entities.Tisch;

public interface TischRepository extends JpaRepository<Tisch, UUID> {

	void deleteAllByEstablishmentId(UUID establishmentId);

}
