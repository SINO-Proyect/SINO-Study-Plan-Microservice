package com.studyplan.studyPlanMicroservice.service;

import com.studyplan.studyPlanMicroservice.data.PageResponse;
import com.studyplan.studyPlanMicroservice.data.UserData;
import com.studyplan.studyPlanMicroservice.domain.User;
import com.studyplan.studyPlanMicroservice.domain.UserPlan;
import com.studyplan.studyPlanMicroservice.jpa.*;

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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPlanRepository userPlanRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final FollowerRepository followerRepository;
    private final CourseRepository courseRepository;
    private final StudyPlanRepository studyPlanRepository;

    @Transactional
    public UserData createUser(UserData data) {
        if (userRepository.existsByEmail(data.getEmail())) {
            throw new RuntimeException("User already exists with email: " + data.getEmail());
        }
        if (data.getUsername() != null && userRepository.existsByUsername(data.getUsername())) {
             throw new RuntimeException("User already exists with username: " + data.getUsername());
        }

        User user = User.builder()
                .firebaseUid(data.getFirebaseUid())
                .username(data.getUsername())
                .fullName(data.getFullName())
                .email(data.getEmail())
                .dateRegister(data.getDateRegister())
                .datePurchase(data.getDatePurchase())
                .type(data.getType())
                .build();

        User saved = userRepository.save(user);
        return toData(saved);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public UserData getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return toData(user);
    }

    @Transactional(readOnly = true)
    public UserData getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return toData(user);
    }

    @Transactional(readOnly = true)
    public UserData getUserByFirebaseUid(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with firebaseUid: " + firebaseUid));
        return toData(user);
    }

    // Deprecated? kept for compatibility if needed
    @Transactional
    public User getOrCreateUserByEmail(String email) {
         return userRepository.findByEmail(email).orElseGet(() -> {
             User newUser = User.builder().email(email).dateRegister(java.time.LocalDateTime.now()).type("freemium").build();
             return userRepository.save(newUser);
         });
    }

    @Transactional
    public User getOrCreateUserByFirebaseUid(String firebaseUid, String email) {
        // This is mainly for legacy sync, better use createUser explicitly
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            existingUser.setFirebaseUid(firebaseUid);
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .firebaseUid(firebaseUid)
                                    .email(email)
                                    .dateRegister(java.time.LocalDateTime.now())
                                    .type("freemium")
                                    .build();
                            return userRepository.save(newUser);
                        }));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserData> getAllUsers(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "email"));
        Page<User> userPage = userRepository.findAll(pageable);
        return PageResponse.from(userPage, this::toData);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserData> searchUsers(String email, String type, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "email"));
        Page<User> userPage = userRepository.findAll(buildSpecification(email, type), pageable);
        return PageResponse.from(userPage, this::toData);
    }

    @Transactional
    public UserData updateUser(Integer id, UserData data) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (!user.getEmail().equals(data.getEmail()) && userRepository.existsByEmail(data.getEmail())) {
            throw new RuntimeException("Email already in use: " + data.getEmail());
        }
        if (data.getUsername() != null && !data.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(data.getUsername())) {
             throw new RuntimeException("Username already in use: " + data.getUsername());
        }

        user.setFirebaseUid(data.getFirebaseUid());
        user.setUsername(data.getUsername());
        user.setFullName(data.getFullName());
        user.setEmail(data.getEmail());
        // Only update dates if provided? Or keep logic as is
        if(data.getDateRegister() != null) user.setDateRegister(data.getDateRegister());
        user.setDatePurchase(data.getDatePurchase());
        user.setType(data.getType());

        User updated = userRepository.save(user);
        return toData(updated);
    }

    @Transactional
    public void updateLastLogin(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private Specification<User> buildSpecification(String email, String type) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("type")), type.toLowerCase()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserData toData(User user) {
        // Calculate progress and fetch extra info
        Double progress = 0.0;
        String degreeName = null;
        
        // 1. Get Active Plan
        List<UserPlan> userPlans = userPlanRepository.findByIdUser(user.getIdUser());
        UserPlan userPlan = userPlans.isEmpty() ? null : userPlans.get(0);
        
        if (userPlan != null) {
            studyPlanRepository.findById(userPlan.getIdStudyPlan()).ifPresent(plan -> {
                 // degreeName = plan.getDscName(); // Cannot set local variable
            });
            // Re-fetch because lambda limits
            var planOpt = studyPlanRepository.findById(userPlan.getIdStudyPlan());
            if(planOpt.isPresent()){
                degreeName = planOpt.get().getDscName();
                long totalCourses = courseRepository.countByIdStudyPlan(userPlan.getIdStudyPlan());
                if (totalCourses > 0) {
                     long approvedCourses = studentCourseRepository.countApprovedCourses(user.getIdUser(), userPlan.getIdStudyPlan());
                     progress = (double) approvedCourses / totalCourses;
                }
            }
        }

        // 2. Followers
        long followers = followerRepository.countByIdUser(user.getIdUser());
        long following = followerRepository.countByFollowerUserId(user.getIdUser());

        return UserData.builder()
                .idUser(user.getIdUser())
                .firebaseUid(user.getFirebaseUid())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateRegister(user.getDateRegister())
                .datePurchase(user.getDatePurchase())
                .type(user.getType())
                .lastLogin(user.getLastLogin())
                .progress(progress)
                .degreeName(degreeName)
                .followersCount(followers)
                .followingCount(following)
                .build();
    }
}