package com.hackpicktable.clientpicktable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackpicktable.clientpicktable.entities.ReservationResponse;

@Repository
public interface ReservationResponseRepository extends JpaRepository<ReservationResponse, Long> {
}
