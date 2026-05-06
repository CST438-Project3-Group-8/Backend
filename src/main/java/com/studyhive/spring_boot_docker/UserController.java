package com.studyhive.spring_boot_docker;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseRepository courseRepository;

    public UserController(UserRepository userRepository,
                          UserCourseRepository userCourseRepository,
                          CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.userCourseRepository = userCourseRepository;
        this.courseRepository = courseRepository;
    }


    @PostMapping
    public @Valid User createUser(@Valid @RequestBody User user, @AuthenticationPrincipal Jwt jwt) {
        user.setUserId(jwt.getId());
        user.setEmail(jwt.getClaimAsString("email"));
        return userRepository.save(user);
    }


    @GetMapping
    public Map<String, Object> getUser(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "userId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "role", jwt.getClaimAsString("role")
        );
    }


    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<User> found = userRepository.findByUserId(jwt.getSubject());
        if (found.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(found.get(0));
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<User> found = userRepository.findByUserId(jwt.getSubject());
        if (found.isEmpty()) return ResponseEntity.notFound().build();

        User user = found.get(0);
        user.setName(request.name());
        user.setBio(request.bio());
        user.setMajor(request.major());

        return ResponseEntity.ok(userRepository.save(user));
    }


    @GetMapping("/me/courses")
    public ResponseEntity<List<Course>> getMyCourses(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Course> courses = userCourseRepository.findByUserId(jwt.getSubject())
                .stream()
                .map(UserCourse::getCourse)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/me/courses/{courseId}")
    public ResponseEntity<Void> addMyCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Idempotent – already enrolled is fine
        if (userCourseRepository.existsByUserIdAndCourse_Id(jwt.getSubject(), courseId)) {
            return ResponseEntity.ok().build();
        }

        return courseRepository.findById(courseId)
                .map(course -> {
                    userCourseRepository.save(new UserCourse(jwt.getSubject(), course));
                    return ResponseEntity.status(HttpStatus.CREATED).<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/me/courses/{courseId}")
    @Transactional
    public ResponseEntity<Void> removeMyCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userCourseRepository.deleteByUserIdAndCourse_Id(jwt.getSubject(), courseId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<User> found = userRepository.findByUserId(jwt.getSubject());
        if (found.isEmpty()) return ResponseEntity.notFound().build();

        // Remove course enrolments first (FK constraint)
        userCourseRepository.deleteAllByUserId(jwt.getSubject());
        userRepository.deleteById(found.get(0).getId().longValue());

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
