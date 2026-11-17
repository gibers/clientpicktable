package com.hackpicktable.clientpicktable.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hackpicktable.clientpicktable.entities.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

	@Query(value = "SELECT * FROM public.time_slot WHERE :day = ANY(available_days) and establishment_id = :establishmentId and is_active = true", nativeQuery = true)
	List<TimeSlot> findAllByAvailableDay(@Param("day") Integer day, @Param("establishmentId") UUID establishmentId);

	void deleteAllByEstablishmentId(UUID establishmentId);

}
