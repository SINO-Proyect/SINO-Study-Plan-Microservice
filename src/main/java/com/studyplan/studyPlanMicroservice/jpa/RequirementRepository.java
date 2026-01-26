package com.studyplan.studyPlanMicroservice.jpa;

import com.studyplan.studyPlanMicroservice.domain.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Integer> {
    List<Requirement> findByIdCourse(Integer idCourse);
    List<Requirement> findByIdCourseRequirement(Integer idCourseRequirement);
}
