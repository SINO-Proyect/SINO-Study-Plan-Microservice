package com.studyplan.studyPlanMicroservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_study_plan")
    private Integer idStudyPlan;

    @Column(name = "id_university", nullable = false)
    private Integer idUniversity;

    @Column(name = "dsc_name", nullable = false, length = 200)
    private String dscName;

    @Column(name = "dsc_career", nullable = false, length = 200)
    private String dscCareer;

    @Column(name = "type_period", nullable = false, length = 50)
    private String typePeriod;

    @Column(name = "year_level", nullable = false, length = 20)
    private String yearLevel;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "id_creator")
    private Integer idCreator;
}
