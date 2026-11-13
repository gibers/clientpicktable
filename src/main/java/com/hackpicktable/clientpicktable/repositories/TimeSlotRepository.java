package com.hackpicktable.clientpicktable.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackpicktable.clientpicktable.entities.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

	void deleteAllByEstablishmentId(UUID establishmentId);

}
