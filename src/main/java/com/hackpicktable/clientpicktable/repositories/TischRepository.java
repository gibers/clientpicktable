package com.hackpicktable.clientpicktable.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackpicktable.clientpicktable.entities.Establishment;
import com.hackpicktable.clientpicktable.entities.Tisch;

public interface TischRepository extends JpaRepository<Tisch, UUID> {

	List<Tisch> findAllByCapacityGreaterThanEqualAndBookableTrue(int capacity);

	void deleteAllByEstablishment(Establishment establishment);

}
