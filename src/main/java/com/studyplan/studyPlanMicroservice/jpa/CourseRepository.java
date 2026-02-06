package com.studyplan.studyPlanMicroservice.jpa;

import com.studyplan.studyPlanMicroservice.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByIdStudyPlan(Integer studyPlanId);

    @Query("SELECT c FROM Course c WHERE TRIM(c.dscCode) = TRIM(:courseCode)")
    Optional<Course> findByDscCode(String courseCode);

    List<Course> findByDscLevel(String level);

    List<Course> findByTypeCourse(String typeCourse);

    @Query("SELECT c FROM Course c WHERE c.idStudyPlan = :studyPlanId AND c.dscPeriod = :period")
    List<Course> findByStudyPlanAndPeriod(Integer studyPlanId, String period);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE TRIM(c.dscCode) = TRIM(:courseCode)")
    boolean existsByDscCode(String courseCode);

    long countByIdStudyPlan(Integer idStudyPlan);
}