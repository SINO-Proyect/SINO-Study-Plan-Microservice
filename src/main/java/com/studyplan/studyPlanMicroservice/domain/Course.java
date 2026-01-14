package com.studyplan.studyPlanMicroservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_course")
    private Integer idCourse;

    @Column(name = "id_study_plan", nullable = false)
    private Integer idStudyPlan;

    @Column(name = "dsc_code", nullable = false, length = 50, unique = true)
    private String dscCode;

    @Column(name = "dsc_name", nullable = false, length = 100)
    private String dscName;

    @Column(name = "dsc_level", nullable = false, length = 20)
    private String dscLevel;

    @Column(name = "dsc_period", nullable = false, length = 50)
    private String dscPeriod;

    @Column(name = "type_course", nullable = false, length = 50)
    private String typeCourse;

    @Column(name = "num_credits", nullable = false)
    private Integer numCredits;

    @Column(name = "requirement", nullable = false)
    private boolean requirement;

    @Column(name = "description", length = 500)
    private String description;
}
