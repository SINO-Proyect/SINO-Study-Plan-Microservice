package com.studyplan.studyPlanMicroservice.controller;

import com.studyplan.studyPlanMicroservice.data.ApiResponse;
import com.studyplan.studyPlanMicroservice.data.UniversityData;
import com.studyplan.studyPlanMicroservice.service.UniversityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
@Tag(name = "Universities", description = "University management")
@CrossOrigin(origins = "*")
public class UniversityController {

    private final UniversityService universityService;

    @PostMapping
    @Operation(summary = "Create a new university")
    public ResponseEntity<ApiResponse<UniversityData>> createUniversity(
            @Valid @RequestBody UniversityData data) {
        UniversityData created = universityService.createUniversity(data);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "University created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get university by ID")
    public ResponseEntity<ApiResponse<UniversityData>> getUniversityById(@PathVariable Integer id) {
        UniversityData university = universityService.getUniversityById(id);
        return ResponseEntity.ok(ApiResponse.success(university, "University retrieved"));
    }

    @GetMapping
    @Operation(summary = "Get all universities")
    public ResponseEntity<ApiResponse<List<UniversityData>>> getAllUniversities() {
        List<UniversityData> universities = universityService.getAllUniversities();
        return ResponseEntity.ok(ApiResponse.success(universities, "Universities retrieved"));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active universities")
    public ResponseEntity<ApiResponse<List<UniversityData>>> getActiveUniversities() {
        List<UniversityData> universities = universityService.getActiveUniversities();
        return ResponseEntity.ok(ApiResponse.success(universities, "Active universities retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update university")
    public ResponseEntity<ApiResponse<UniversityData>> updateUniversity(
            @PathVariable Integer id, @Valid @RequestBody UniversityData data) {
        UniversityData updated = universityService.updateUniversity(id, data);
        return ResponseEntity.ok(ApiResponse.success(updated, "University updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete university")
    public ResponseEntity<ApiResponse<Void>> deleteUniversity(@PathVariable Integer id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.ok(ApiResponse.success(null, "University deleted"));
    }
}

