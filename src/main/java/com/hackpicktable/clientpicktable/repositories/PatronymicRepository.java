package com.hackpicktable.clientpicktable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackpicktable.clientpicktable.entities.Patronymic;

@Repository
public interface PatronymicRepository extends JpaRepository<Patronymic, Long> {
}
