package com.hackpicktable.clientpicktable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.ReservationResponse;
import com.hackpicktable.clientpicktable.entities.Tisch;

@Repository
public interface ReservationResponseRepository extends JpaRepository<ReservationResponse, Long> {

	void deleteAllByEstablishmentAndTisch(Establishment establishment, Tisch tisch);

}
