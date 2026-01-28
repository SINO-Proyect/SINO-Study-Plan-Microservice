package com.studyplan.studyPlanMicroservice.controller;

import com.studyplan.studyPlanMicroservice.data.ApiResponse;
import com.studyplan.studyPlanMicroservice.data.StudentCourseData;
import com.studyplan.studyPlanMicroservice.service.StudentCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-courses")
@RequiredArgsConstructor
@Tag(name = "Student Courses", description = "Student progress management")
@CrossOrigin(origins = "*")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all student courses for a user")
    public ResponseEntity<ApiResponse<List<StudentCourseData>>> getStudentCoursesByUser(@PathVariable Integer userId) {
        List<StudentCourseData> courses = studentCourseService.getStudentCoursesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(courses, "Student courses retrieved"));
    }

    @PostMapping
    @Operation(summary = "Create or update student course status")
    public ResponseEntity<ApiResponse<StudentCourseData>> createOrUpdateStudentCourse(@RequestBody StudentCourseData data) {
        StudentCourseData saved = studentCourseService.createOrUpdateStudentCourse(data);
        return ResponseEntity.ok(ApiResponse.success(saved, "Student course saved"));
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize student plan with default statuses")
    public ResponseEntity<ApiResponse<Void>> initializeStudentPlan(
            @RequestParam Integer userId, 
            @RequestParam Integer planId) {
        studentCourseService.initializeStudentPlan(userId, planId);
        return ResponseEntity.ok(ApiResponse.success(null, "Plan initialized successfully"));
    }

    @PostMapping("/recalculate/{userId}")
    @Operation(summary = "Recalculate all course statuses for a user based on prerequisites")
    public ResponseEntity<ApiResponse<Void>> recalculateAllStatuses(@PathVariable Integer userId) {
        studentCourseService.recalculateAllStatuses(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Statuses recalculated successfully"));
    }
}
