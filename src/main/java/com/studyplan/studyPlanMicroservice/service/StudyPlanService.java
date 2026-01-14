package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.StudyPlanData;
import com.studyplan.studyPlanMicroservice.domain.StudyPlan;
import com.studyplan.studyPlanMicroservice.jpa.StudyPlanRepository;
import com.studyplan.studyPlanMicroservice.jpa.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final UniversityRepository universityRepository;

    @Transactional
    public StudyPlanData createStudyPlan(StudyPlanData data) {
        // verify university exists
        if (!universityRepository.existsById(data.getIdUniversity())) {
            throw new RuntimeException("University not found: " + data.getIdUniversity());
        }

        // Check for duplicates
        if (studyPlanRepository.existsByDscCareerAndIdUniversity(
                data.getDscCareer(), data.getIdUniversity())) {
            throw new RuntimeException("Study plan already exists for this career");
        }

        StudyPlan studyPlan = StudyPlan.builder()
                .idUniversity(data.getIdUniversity())
                .dscName(data.getDscName())
                .dscCareer(data.getDscCareer())
                .typePeriod(data.getTypePeriod())
                .yearLevel(data.getYearLevel())
                .status(data.getStatus())
                .build();

        StudyPlan saved = studyPlanRepository.save(studyPlan);
        return toData(saved);
    }

    @Transactional(readOnly = true)
    public StudyPlanData getStudyPlanById(Integer id) {
        StudyPlan studyPlan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + id));
        return toData(studyPlan);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanData> getAllStudyPlans() {
        return studyPlanRepository.findAll().stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudyPlanData> getStudyPlansByUniversity(Integer universityId) {
        return studyPlanRepository.findByIdUniversity(universityId).stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudyPlanData updateStudyPlan(Integer id, StudyPlanData data) {
        StudyPlan studyPlan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + id));

        studyPlan.setDscName(data.getDscName());
        studyPlan.setDscCareer(data.getDscCareer());
        studyPlan.setTypePeriod(data.getTypePeriod());
        studyPlan.setYearLevel(data.getYearLevel());
        studyPlan.setStatus(data.getStatus());

        StudyPlan updated = studyPlanRepository.save(studyPlan);
        return toData(updated);
    }

    @Transactional
    public void deleteStudyPlan(Integer id) {
        if (!studyPlanRepository.existsById(id)) {
            throw new RuntimeException("Study plan not found: " + id);
        }
        studyPlanRepository.deleteById(id);
    }

    private StudyPlanData toData(StudyPlan studyPlan) {
        return StudyPlanData.builder()
                .idStudyPlan(studyPlan.getIdStudyPlan())
                .idUniversity(studyPlan.getIdUniversity())
                .dscName(studyPlan.getDscName())
                .dscCareer(studyPlan.getDscCareer())
                .typePeriod(studyPlan.getTypePeriod())
                .yearLevel(studyPlan.getYearLevel())
                .status(studyPlan.getStatus())
                .build();
    }
}