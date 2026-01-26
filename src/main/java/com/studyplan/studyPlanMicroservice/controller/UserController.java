package com.studyplan.studyPlanMicroservice.controller;

import com.studyplan.studyPlanMicroservice.data.ApiResponse;
import com.studyplan.studyPlanMicroservice.data.PageResponse;
import com.studyplan.studyPlanMicroservice.data.UserData;
import com.studyplan.studyPlanMicroservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserData>> createUser(
            @Valid @RequestBody UserData data) {
        UserData created = userService.createUser(data);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User created successfully"));
    }

    @GetMapping("/check-username/{username}")
    @Operation(summary = "Check if username exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(exists, exists ? "Username taken" : "Username available"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserData>> getUserById(@PathVariable Integer id) {
        UserData user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved"));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<ApiResponse<UserData>> getUserByEmail(@PathVariable String email) {
        UserData user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved"));
    }

    @GetMapping("/firebase/{uid}")
    @Operation(summary = "Get user by Firebase UID")
    public ResponseEntity<ApiResponse<UserData>> getUserByFirebaseUid(@PathVariable String uid) {
        UserData user = userService.getUserByFirebaseUid(uid);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved"));
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination and optional filters")
    public ResponseEntity<ApiResponse<PageResponse<UserData>>> getAllUsers(
            @Parameter(description = "Email (optional filter)")
            @RequestParam(required = false) String email,
            @Parameter(description = "Type: free or premium (optional filter)")
            @RequestParam(required = false) String type,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") Integer size) {

        PageResponse<UserData> result;

        if (email != null || type != null) {
            result = userService.searchUsers(email, type, page, size);
        } else {
            result = userService.getAllUsers(page, size);
        }

        return ResponseEntity.ok(ApiResponse.success(result, "Users retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserData>> updateUser(
            @PathVariable Integer id, @Valid @RequestBody UserData data) {
        UserData updated = userService.updateUser(id, data);
        return ResponseEntity.ok(ApiResponse.success(updated, "User updated"));
    }

    @PostMapping("/{id}/last-login")
    @Operation(summary = "Update last login timestamp")
    public ResponseEntity<ApiResponse<Void>> updateLastLogin(@PathVariable Integer id) {
        userService.updateLastLogin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Last login updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }
}