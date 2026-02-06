package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.CourseData;
import com.studyplan.studyPlanMicroservice.domain.Course;
import com.studyplan.studyPlanMicroservice.domain.Requirement;
import com.studyplan.studyPlanMicroservice.domain.StudyPlan;
import com.studyplan.studyPlanMicroservice.jpa.CourseRepository;
import com.studyplan.studyPlanMicroservice.jpa.RequirementRepository;
import com.studyplan.studyPlanMicroservice.jpa.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final RequirementRepository requirementRepository;

    @Transactional
    public CourseData createCourse(CourseData data) {
        // Verify study plan exists
        if (!studyPlanRepository.existsById(data.getIdStudyPlan())) {
            throw new RuntimeException("Study plan not found: " + data.getIdStudyPlan());
        }

        // Normalize course code (trim spaces)
        String normalizedCode = data.getDscCode().trim();

        // Check for duplicate course code
        if (courseRepository.existsByDscCode(normalizedCode)) {
            throw new RuntimeException("Course code already exists: " + normalizedCode);
        }

        Course course = Course.builder()
                .idStudyPlan(data.getIdStudyPlan())
                .dscCode(normalizedCode)  // ← Guarda sin espacios
                .dscName(data.getDscName())
                .dscLevel(data.getDscLevel())
                .dscPeriod(data.getDscPeriod())
                .typeCourse(data.getTypeCourse())
                .numCredits(data.getNumCredits())
                .description(data.getDescription())
                .build();

        Course saved = courseRepository.save(course);

        // Sync with student_courses
        studentCourseService.syncPlanCoursesForAllUsers(data.getIdStudyPlan());

        return toData(saved);
    }

    //create a list of courses in a single transaction
    @Transactional
    public List<CourseData> createCourseBatch(List<CourseData> coursesData) {
        // 1. Save all courses first
        List<Course> savedCourses = coursesData.stream().map(data -> {
             Course course = Course.builder()
                .idStudyPlan(data.getIdStudyPlan())
                .dscCode(data.getDscCode())
                .dscName(data.getDscName())
                .dscLevel(data.getDscLevel())
                .dscPeriod(data.getDscPeriod())
                .typeCourse(data.getTypeCourse())
                .numCredits(data.getNumCredits())
                .description(data.getDescription())
                .build();
             return courseRepository.save(course);
        }).collect(Collectors.toList());

        // 2. Map Code -> ID
        Map<String, Integer> codeToId = savedCourses.stream()
                .collect(Collectors.toMap(Course::getDscCode, Course::getIdCourse));

        // 3. Save Requirements and Corequisites
        for (CourseData data : coursesData) {
            Integer courseId = codeToId.get(data.getDscCode());
            if (courseId == null) continue;

            // Save Prerequisites
            if (data.getPrerequisites() != null) {
                for (String reqCode : data.getPrerequisites()) {
                    Integer reqId = codeToId.get(reqCode);
                    if (reqId != null) {
                        Requirement req = Requirement.builder()
                                .idCourse(courseId)
                                .idCourseRequirement(reqId)
                                .typeRequirement("PREREQUISITE")
                                .build();
                        requirementRepository.save(req);
                    }
                }
            }

            // Save Corequisites
            if (data.getCorequisites() != null) {
                for (String coreqCode : data.getCorequisites()) {
                    Integer coreqId = codeToId.get(coreqCode);
                    if (coreqId != null) {
                        Requirement req = Requirement.builder()
                                .idCourse(courseId)
                                .idCourseRequirement(coreqId)
                                .typeRequirement("COREQUISITE")
                                .build();
                        requirementRepository.save(req);
                    }
                }
            }
        }

        return savedCourses.stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseData getCourseById(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
        return toData(course);
    }

    @Transactional(readOnly = true)
    public CourseData getCourseByCode(String code) {
        Course course = courseRepository.findByDscCode(code.trim()).orElse(null);
        if (course == null) {
            return null;
        }
        return toData(course);
    }

    @Transactional(readOnly = true)
    public List<CourseData> getCoursesByStudyPlan(Integer studyPlanId) {
        return courseRepository.findByIdStudyPlan(studyPlanId).stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseData> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseData updateCourse(Integer id, CourseData data) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));

        course.setIdStudyPlan(data.getIdStudyPlan());
        course.setDscCode(data.getDscCode());
        course.setDscName(data.getDscName());
        course.setDscLevel(data.getDscLevel());
        course.setDscPeriod(data.getDscPeriod());
        course.setTypeCourse(data.getTypeCourse());
        course.setNumCredits(data.getNumCredits());
        course.setDescription(data.getDescription());

        Course updated = courseRepository.save(course);
        return toData(updated);
    }

    @Transactional
    public void deleteCourse(Integer id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found: " + id);
        }
        courseRepository.deleteById(id);
    }

    //structure for the json file
    private CourseData toData(Course course) {
        return CourseData.builder()
                .idCourse(course.getIdCourse())
                .idStudyPlan(course.getIdStudyPlan())
                .dscCode(course.getDscCode())
                .dscName(course.getDscName())
                .dscLevel(course.getDscLevel())
                .dscPeriod(course.getDscPeriod())
                .typeCourse(course.getTypeCourse())
                .numCredits(course.getNumCredits())
                .description(course.getDescription())
                .build();
    }
}

