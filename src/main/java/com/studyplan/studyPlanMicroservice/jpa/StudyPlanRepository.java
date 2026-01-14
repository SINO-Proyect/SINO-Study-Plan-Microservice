package com.studyplan.studyPlanMicroservice.jpa;

import com.studyplan.studyPlanMicroservice.domain.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Integer> {

    List<StudyPlan> findByIdUniversity(Integer universityId);

    List<StudyPlan> findByStatus(Boolean status);

    Optional<StudyPlan> findByDscCareerAndIdUniversity(String career, Integer universityId);

    @Query("SELECT sp FROM StudyPlan sp WHERE sp.idUniversity = :universityId AND sp.status = true")
    List<StudyPlan> findActiveByUniversity(Integer universityId);

    boolean existsByDscCareerAndIdUniversity(String career, Integer universityId);
}
