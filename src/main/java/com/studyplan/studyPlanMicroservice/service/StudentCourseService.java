package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.StudentCourseData;
import com.studyplan.studyPlanMicroservice.domain.*;
import com.studyplan.studyPlanMicroservice.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseService {

    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StatusRepository statusRepository;
    private final RequirementRepository requirementRepository;
    private final UserPlanRepository userPlanRepository;

    private static final int STATUS_AVAILABLE = 1;
    private static final int STATUS_IN_PROGRESS = 2;
    private static final int STATUS_LOCKED = 3;
    private static final int STATUS_PASSED = 4;

    @Transactional(readOnly = true)
    public List<StudentCourseData> getStudentCoursesByUser(Integer userId) {
        return studentCourseRepository.findByUser_IdUser(userId).stream()
                .map(this::toData)
                .collect(Collectors.toList());
    }

    @Transactional
    public void syncPlanCoursesForAllUsers(Integer planId) {
        List<UserPlan> userPlans = userPlanRepository.findByIdStudyPlan(planId);
        for (UserPlan up : userPlans) {
            initializeStudentPlan(up.getIdUser(), planId);
        }
    }

    @Transactional
    public void recalculateAllStatuses(Integer userId) {
        List<StudentCourse> userCourses = studentCourseRepository.findByUser_IdUser(userId);
        Status available = statusRepository.findById(STATUS_AVAILABLE).orElseThrow();
        Status locked = statusRepository.findById(STATUS_LOCKED).orElseThrow();

        for (StudentCourse sc : userCourses) {
            // Only update if it's currently LOCKED or AVAILABLE. 
            // PASSED and IN_PROGRESS are manual states.
            if (sc.getStatus().getIdStatus() == STATUS_LOCKED || sc.getStatus().getIdStatus() == STATUS_AVAILABLE) {
                if (areAllRequirementsMet(userId, sc.getCourse().getIdCourse())) {
                    if (sc.getStatus().getIdStatus() == STATUS_LOCKED) {
                        sc.setStatus(available);
                        studentCourseRepository.save(sc);
                    }
                } else {
                    if (sc.getStatus().getIdStatus() == STATUS_AVAILABLE) {
                        sc.setStatus(locked);
                        studentCourseRepository.save(sc);
                    }
                }
            }
        }
    }

    @Transactional
    public void initializeStudentPlan(Integer userId, Integer planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Course> courses = courseRepository.findByIdStudyPlan(planId);
        Status available = statusRepository.findById(STATUS_AVAILABLE).orElseThrow();
        Status locked = statusRepository.findById(STATUS_LOCKED).orElseThrow();

        for (Course course : courses) {
            // Check if already exists to avoid duplicates
            boolean exists = studentCourseRepository.findByUser_IdUser(userId).stream()
                    .anyMatch(sc -> sc.getCourse().getIdCourse().equals(course.getIdCourse()));
            
            if (!exists) {
                List<Requirement> reqs = requirementRepository.findByIdCourse(course.getIdCourse());
                // Only PREREQUISITES cause it to be LOCKED initially.
                boolean hasPrerequisites = reqs.stream().anyMatch(r -> "PREREQUISITE".equals(r.getTypeRequirement()));
                Status initialStatus = hasPrerequisites ? locked : available;

                StudentCourse sc = StudentCourse.builder()
                        .user(user)
                        .course(course)
                        .status(initialStatus)
                        .numTimesTaken(0)
                        .build();
                studentCourseRepository.save(sc);
            }
        }
    }

    @Transactional
    public StudentCourseData createOrUpdateStudentCourse(StudentCourseData data) {
        User user = userRepository.findById(data.getIdUser())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(data.getIdCourse())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        Status status = statusRepository.findById(data.getIdStatus())
                .orElseThrow(() -> new RuntimeException("Status not found"));

        StudentCourse existing = studentCourseRepository.findByUser_IdUser(data.getIdUser()).stream()
                .filter(sc -> sc.getCourse().getIdCourse().equals(data.getIdCourse()))
                .findFirst()
                .orElse(null);

        StudentCourse studentCourse;
        if (existing != null) {
            existing.setStatus(status);
            existing.setNumTimesTaken(data.getNumTimesTaken() != null ? data.getNumTimesTaken() : existing.getNumTimesTaken());
            studentCourse = studentCourseRepository.save(existing);
        } else {
            studentCourse = StudentCourse.builder()
                    .user(user)
                    .course(course)
                    .status(status)
                    .numTimesTaken(data.getNumTimesTaken() != null ? data.getNumTimesTaken() : 0)
                    .build();
            studentCourse = studentCourseRepository.save(studentCourse);
        }

        // If course is passed, check unlocks
        if (data.getIdStatus() == STATUS_PASSED) {
            checkAndUnlockCourses(data.getIdUser(), data.getIdCourse());
        }

        return toData(studentCourse);
    }

    private void checkAndUnlockCourses(Integer userId, Integer passedCourseId) {
        // Find courses that depend on this one
        List<Requirement> dependentReqs = requirementRepository.findByIdCourseRequirement(passedCourseId);
        Status available = statusRepository.findById(STATUS_AVAILABLE).orElseThrow();

        for (Requirement req : dependentReqs) {
            Integer dependentCourseId = req.getIdCourse();
            
            // Check if ALL requirements for this dependent course are passed
            if (areAllRequirementsMet(userId, dependentCourseId)) {
                // Update status to AVAILABLE if it was LOCKED
                StudentCourse dependentSc = studentCourseRepository.findByUser_IdUser(userId).stream()
                        .filter(sc -> sc.getCourse().getIdCourse().equals(dependentCourseId))
                        .findFirst()
                        .orElse(null);
                
                if (dependentSc != null && dependentSc.getStatus().getIdStatus() == STATUS_LOCKED) {
                    dependentSc.setStatus(available);
                    studentCourseRepository.save(dependentSc);
                }
            }
        }
    }

    private boolean areAllRequirementsMet(Integer userId, Integer courseId) {
        List<Requirement> requirements = requirementRepository.findByIdCourse(courseId);
        List<StudentCourse> userCourses = studentCourseRepository.findByUser_IdUser(userId);

        for (Requirement req : requirements) {
            if ("PREREQUISITE".equals(req.getTypeRequirement())) {
                boolean isPassed = userCourses.stream()
                        .anyMatch(sc -> sc.getCourse().getIdCourse().equals(req.getIdCourseRequirement()) 
                                     && sc.getStatus().getIdStatus() == STATUS_PASSED);
                if (!isPassed) return false;
            }
        }
        return true;
    }

    private StudentCourseData toData(StudentCourse entity) {
        return StudentCourseData.builder()
                .idStudentCourse(entity.getIdStudentCourse())
                .idStatus(entity.getStatus().getIdStatus())
                .statusName(entity.getStatus().getDscName())
                .idUser(entity.getUser().getIdUser())
                .idCourse(entity.getCourse().getIdCourse())
                .numTimesTaken(entity.getNumTimesTaken())
                .build();
    }
}