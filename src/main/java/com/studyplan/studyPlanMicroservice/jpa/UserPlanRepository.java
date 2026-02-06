package com.studyplan.studyPlanMicroservice.jpa;

import com.studyplan.studyPlanMicroservice.domain.UserPlan;
import com.studyplan.studyPlanMicroservice.domain.UserPlanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlanRepository extends JpaRepository<UserPlan, UserPlanId> {
    List<UserPlan> findByIdUser(Integer idUser); 
    List<UserPlan> findByIdStudyPlan(Integer idStudyPlan);
}
