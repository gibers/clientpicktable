package com.hackpicktable.clientpicktable.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patronymic_used", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class PatronymicUsed {

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    @JsonIgnore
    private Establishment establishment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
