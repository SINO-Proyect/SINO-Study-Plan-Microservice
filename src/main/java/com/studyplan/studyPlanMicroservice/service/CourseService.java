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
    private final StudentCourseService studentCourseService;

    @Transactional
    public CourseData createCourse(CourseData data) {
        // Verify study plan exists
        if (!studyPlanRepository.existsById(data.getIdStudyPlan())) {
            throw new RuntimeException("Study plan not found: " + data.getIdStudyPlan());
        }

        // Check for duplicate course code
        if (courseRepository.existsByDscCode(data.getDscCode())) {
            throw new RuntimeException("Course code already exists: " + data.getDscCode());
        }

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

        Course saved = courseRepository.save(course);
        
        // Sync with student_courses
        studentCourseService.syncPlanCoursesForAllUsers(data.getIdStudyPlan());
        
        return toData(saved);
    }

    //create a list of courses in a single transaction
    @Transactional
    public List<CourseData> createCourseBatch(List<CourseData> coursesData) {
        if (coursesData.isEmpty()) return List.of();
        
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

        // 4. Sync with student_courses for the plan
        Integer planId = coursesData.get(0).getIdStudyPlan();
        studentCourseService.syncPlanCoursesForAllUsers(planId);

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
        Course course = courseRepository.findByDscCode(code)
                .orElseThrow(() -> new RuntimeException("Course not found: " + code));
        return toData(course);
    }

    @Transactional(readOnly = true)
    public List<CourseData> getCoursesByStudyPlan(Integer studyPlanId) {
        List<Course> courses = courseRepository.findByIdStudyPlan(studyPlanId);
        // Pre-fetch all requirements for the plan to avoid N+1 queries
        List<Requirement> allReqs = requirementRepository.findAll(); // Optimization: could filter by course ids
        Map<Integer, String> idToCode = courses.stream().collect(Collectors.toMap(Course::getIdCourse, Course::getDscCode));

        return courses.stream()
                .map(c -> toData(c, allReqs, idToCode))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseData> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<Requirement> allReqs = requirementRepository.findAll();
        Map<Integer, String> idToCode = courses.stream().collect(Collectors.toMap(Course::getIdCourse, Course::getDscCode));
        
        return courses.stream()
                .map(c -> toData(c, allReqs, idToCode))
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseData updateCourse(Integer id, CourseData data) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));

        // Check for duplicate course code if it changed
        if (!course.getDscCode().equals(data.getDscCode()) && courseRepository.existsByDscCode(data.getDscCode())) {
            throw new RuntimeException("Course code already exists: " + data.getDscCode());
        }

        course.setDscCode(data.getDscCode());
        course.setDscName(data.getDscName());
        course.setDscLevel(data.getDscLevel());
        course.setDscPeriod(data.getDscPeriod());
        course.setTypeCourse(data.getTypeCourse());
        course.setNumCredits(data.getNumCredits());
        course.setDescription(data.getDescription());
        course.setIdStudyPlan(data.getIdStudyPlan());

        Course saved = courseRepository.save(course);
        
        // Note: For simplicity, we are not updating requirements here as they involve complex logic 
        // with other courses. Requirements are usually handled via batch upload or specific endpoints.
        
        return toData(saved);
    }

    @Transactional
    public void deleteCourse(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
        
        // Delete related requirements
        requirementRepository.deleteByIdCourse(id);
        requirementRepository.deleteByIdCourseRequirement(id);
        
        courseRepository.delete(course);
        
        // Sync with student_courses
        studentCourseService.syncPlanCoursesForAllUsers(course.getIdStudyPlan());
    }

    private CourseData toData(Course course) {
        return toData(course, List.of(), Map.of());
    }

    private CourseData toData(Course course, List<Requirement> allReqs, Map<Integer, String> idToCode) {
        List<String> prereqs = allReqs.stream()
                .filter(r -> r.getIdCourse().equals(course.getIdCourse()) && "PREREQUISITE".equals(r.getTypeRequirement()))
                .map(r -> idToCode.getOrDefault(r.getIdCourseRequirement(), "REQ-" + r.getIdCourseRequirement()))
                .collect(Collectors.toList());

        List<String> coreqs = allReqs.stream()
                .filter(r -> r.getIdCourse().equals(course.getIdCourse()) && "COREQUISITE".equals(r.getTypeRequirement()))
                .map(r -> idToCode.getOrDefault(r.getIdCourseRequirement(), "REQ-" + r.getIdCourseRequirement()))
                .collect(Collectors.toList());

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
                .prerequisites(prereqs)
                .corequisites(coreqs)
                .build();
    }
}

