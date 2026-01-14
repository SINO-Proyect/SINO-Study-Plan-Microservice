package com.studyplan.studyPlanMicroservice.controller;


import com.studyplan.studyPlanMicroservice.data.ApiResponse;
import com.studyplan.studyPlanMicroservice.data.CourseData;
import com.studyplan.studyPlanMicroservice.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseData>> createCourse(
            @Valid @RequestBody CourseData data) {
        CourseData created = courseService.createCourse(data);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Course created successfully"));
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple courses (for AI)")
    public ResponseEntity<ApiResponse<List<CourseData>>> createCourseBatch(
            @Valid @RequestBody List<CourseData> courses) {
        List<CourseData> created = courseService.createCourseBatch(courses);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Courses created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<ApiResponse<CourseData>> getCourseById(@PathVariable Integer id) {
        CourseData course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course, "Course retrieved"));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get course by code")
    public ResponseEntity<ApiResponse<CourseData>> getCourseByCode(@PathVariable String code) {
        CourseData course = courseService.getCourseByCode(code);
        return ResponseEntity.ok(ApiResponse.success(course, "Course retrieved"));
    }

    @GetMapping
    @Operation(summary = "Get all courses")
    public ResponseEntity<ApiResponse<List<CourseData>>> getAllCourses() {
        List<CourseData> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses, "Courses retrieved"));
    }

    @GetMapping("/study-plan/{studyPlanId}")
    @Operation(summary = "Get courses by study plan")
    public ResponseEntity<ApiResponse<List<CourseData>>> getCoursesByStudyPlan(
            @PathVariable Integer studyPlanId) {
        List<CourseData> courses = courseService.getCoursesByStudyPlan(studyPlanId);
        return ResponseEntity.ok(ApiResponse.success(courses, "Courses retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update course")
    public ResponseEntity<ApiResponse<CourseData>> updateCourse(
            @PathVariable Integer id, @Valid @RequestBody CourseData data) {
        CourseData updated = courseService.updateCourse(id, data);
        return ResponseEntity.ok(ApiResponse.success(updated, "Course updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Integer id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted"));
    }
}
