package com.hackpicktable.clientpicktable.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hackpicktable.clientpicktable.entities.Establishment;

public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {

	Optional<Establishment> findBySlug(String establishmentSlug);

	void deleteByName(String establishmentName);

	@Query("select distinct e.slug from Establishment e order by e.slug")
	List<String> findDistinctSlugs();

}
