package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.PageResponse;
import com.studyplan.studyPlanMicroservice.data.StudyPlanData;
import com.studyplan.studyPlanMicroservice.domain.StudyPlan;
import com.studyplan.studyPlanMicroservice.jpa.StudyPlanRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final UserService userService;
    private final StudentCourseService studentCourseService;

    @Transactional
    public StudyPlanData createStudyPlan(StudyPlanData data, String userEmail) {
        // Find or create user
        var user = userService.getOrCreateUserByEmail(userEmail);

        if (studyPlanRepository.existsByDscCareerAndIdUniversity(data.getDscCareer(), data.getIdUniversity())) {
            // If it exists globally, we might just want to link it if not already linked
            StudyPlan existing = studyPlanRepository.findAll(buildSpecification(null, data.getDscCareer(), data.getYearLevel(), data.getIdUniversity(), null), PageRequest.of(0, 1)).getContent().stream().findFirst().orElse(null);
            if (existing != null) {
                if (studyPlanRepository.existsRelationship(user.getIdUser(), existing.getIdStudyPlan()) == 0) {
                    studyPlanRepository.linkUserToPlan(user.getIdUser(), existing.getIdStudyPlan());
                    studentCourseService.initializeStudentPlan(user.getIdUser(), existing.getIdStudyPlan());
                }
                return toData(existing);
            }
        }

        StudyPlan studyPlan = StudyPlan.builder()
                .idUniversity(data.getIdUniversity())
                .dscName(data.getDscName())
                .dscCareer(data.getDscCareer())
                .typePeriod(data.getTypePeriod())
                .yearLevel(data.getYearLevel())
                .status(data.getStatus())
                .idCreator(user.getIdUser())
                .build();

        StudyPlan saved = studyPlanRepository.save(studyPlan);
        
        // Link to user
        studyPlanRepository.linkUserToPlan(user.getIdUser(), saved.getIdStudyPlan());
        studentCourseService.initializeStudentPlan(user.getIdUser(), saved.getIdStudyPlan());
        
        return toData(saved);
    }

    @Transactional
    public List<StudyPlanData> getStudyPlansByUser(String email) {
        // Asegurar que el usuario existe en MySQL al consultar
        userService.getOrCreateUserByEmail(email);
        
        return studyPlanRepository.findByUserEmail(email)
                .stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyPlanData getStudyPlanById(Integer id) {
        StudyPlan studyPlan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + id));
        return toData(studyPlan);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudyPlanData> getAllStudyPlans(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "dscName")
        );

        Page<StudyPlan> studyPlanPage = studyPlanRepository.findAll(pageable);

        return PageResponse.from(studyPlanPage, this::toData);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudyPlanData> searchStudyPlans(
            String name,
            String career,
            String yearLevel,
            Integer universityId,
            Boolean status,
            Integer page,
            Integer size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "dscName")
        );
        Page<StudyPlan> studyPlanPage = studyPlanRepository.findAll(
                buildSpecification(name, career, yearLevel, universityId, status),
                pageable
        );
        return PageResponse.from(studyPlanPage, this::toData);
    }

    @Transactional(readOnly = true)
    public List<StudyPlanData> getStudyPlansByUniversity(Integer universityId) {
        return studyPlanRepository.findByIdUniversity(universityId)
                .stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudyPlanData updateStudyPlan(Integer id, StudyPlanData data) {
        StudyPlan studyPlan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + id));

        studyPlan.setIdUniversity(data.getIdUniversity());
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

    private Specification<StudyPlan> buildSpecification(String name, String career, String yearLevel, Integer universityId, Boolean status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("dscName")),
                                "%" + name.toLowerCase() + "%"
                        )
                );
            }
            if (career != null && !career.trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("dscCareer")),
                                "%" + career.toLowerCase() + "%"
                        )
                );
            }
            if (yearLevel != null) {

                predicates.add(
                        criteriaBuilder.equal(root.get("yearLevel"), yearLevel)
                );
                // Asumiendo que el atributo en tu entidad StudyPlan se llama "yearLevel"
            }

            if (universityId != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("idUniversity"), universityId)
                );
            }
            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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