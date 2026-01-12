package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.UniversityData;
import com.studyplan.studyPlanMicroservice.domain.University;
import com.studyplan.studyPlanMicroservice.jpa.UniversityRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityRepository universityRepository;

    @Transactional
    public UniversityData createUniversity(UniversityData data) {
        if (universityRepository.existsByDscName(data.getDscName())) {
            throw new RuntimeException("University already exists: " + data.getDscName());
        }

        University university = University.builder()
                .dscName(data.getDscName())
                .dscCountry(data.getDscCountry())
                .status(data.getStatus())
                .build();

        University saved = universityRepository.save(university);
        return toData(saved);
    }

    @Transactional(readOnly = true)
    public UniversityData getUniversityById(Integer id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("University not found: " + id));
        return toData(university);
    }

    @Transactional(readOnly = true)
    public List<UniversityData> getAllUniversities() {
        return universityRepository.findAll().stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UniversityData> getActiveUniversities() {
        return universityRepository.findByStatus(true).stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional
    public UniversityData updateUniversity(Integer id, UniversityData data) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("University not found: " + id));

        university.setDscName(data.getDscName());
        university.setDscCountry(data.getDscCountry());
        university.setStatus(data.getStatus());

        University updated = universityRepository.save(university);
        return toData(updated);
    }

    @Transactional
    public void deleteUniversity(Integer id) {
        if (!universityRepository.existsById(id)) {
            throw new RuntimeException("University not found: " + id);
        }
        universityRepository.deleteById(id);
    }

    private UniversityData toData(University university) {
        return UniversityData.builder()
                .idUniversity(university.getIdUniversity())
                .dscName(university.getDscName())
                .dscCountry(university.getDscCountry())
                .status(university.getStatus())
                .build();
    }
}

