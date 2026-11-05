package com.hackpicktable.clientpicktable.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patronymic", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Patronymic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "firstname", nullable = false, length = 70)
    private String firstname;

    @Column(name = "name", nullable = false, length = 70)
    private String name;

    @Column(name = "email", nullable = false, length = 70)
    private String email;
}
