package com.studyplan.studyPlanMicroservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "university")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_university")
    private Integer idUniversity;

    @Column(name = "dsc_name", nullable = false, length = 200)
    private String dscName;

    @Column(name = "dsc_country", nullable = false, length = 100)
    private String dscCountry;

    @Column(name = "status", nullable = false)
    private Boolean status;
}

