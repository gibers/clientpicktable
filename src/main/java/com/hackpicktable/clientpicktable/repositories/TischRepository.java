package com.hackpicktable.clientpicktable.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.Tisch;

public interface TischRepository extends JpaRepository<Tisch, UUID> {

	List<Tisch> findAllByEstablishmentAndCapacityGreaterThanEqualAndBookableTrue(Establishment establishment,int capacity);
	List<Tisch> findAllByEstablishment(Establishment establishment);

	@Query("select distinct t.establishment from Tisch t where t.active = true and t.bookable = true")
	List<Establishment> findDistinctEstablishments();

	void deleteAllByEstablishment(Establishment establishment);

}
