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
    public ResponseEntity<User> createUser(
            @RequestBody(required = false) User user,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User incomingUser = user != null ? user : new User();
        User persistedUser = userRepository.findByUserId(jwt.getSubject())
                .stream()
                .findFirst()
                .orElseGet(User::new);

        persistedUser.setUserId(jwt.getSubject());
        persistedUser.setEmail(jwt.getClaimAsString("email"));
        persistedUser.setName(hasText(incomingUser.getName()) ? incomingUser.getName() : coalesce(persistedUser.getName(), defaultNameFor(jwt)));
        persistedUser.setBio(coalesce(incomingUser.getBio(), persistedUser.getBio()));
        persistedUser.setMajor(coalesce(incomingUser.getMajor(), persistedUser.getMajor()));
        persistedUser.setOauthProvider(resolveOauthProvider(incomingUser.getOauthProvider(), persistedUser.getOauthProvider(), jwt));

        return ResponseEntity.ok(userRepository.save(persistedUser));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(Map.of(
                "userId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "role", jwt.getClaimAsString("role")
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(findOrCreateCurrentUser(jwt));
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = findOrCreateCurrentUser(jwt);
        user.setName(request.name());
        user.setBio(request.bio());
        user.setMajor(request.major());

        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/me/courses")
    public ResponseEntity<List<Course>> getMyCourses(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        findOrCreateCurrentUser(jwt);
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

        findOrCreateCurrentUser(jwt);

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

        findOrCreateCurrentUser(jwt);
        userCourseRepository.deleteByUserIdAndCourse_Id(jwt.getSubject(), courseId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<User> found = userRepository.findByUserId(jwt.getSubject());
        if (found.isEmpty()) return ResponseEntity.noContent().build();

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

    private User findOrCreateCurrentUser(Jwt jwt) {
        return userRepository.findByUserId(jwt.getSubject())
                .stream()
                .findFirst()
                .orElseGet(() -> userRepository.save(newUserFromJwt(jwt)));
    }

    private User newUserFromJwt(Jwt jwt) {
        User user = new User();
        user.setUserId(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email"));
        user.setName(defaultNameFor(jwt));
        user.setOauthProvider(resolveOauthProvider(null, null, jwt));
        return user;
    }

    private OauthProvider resolveOauthProvider(OauthProvider requestedProvider, OauthProvider existingProvider, Jwt jwt) {
        if (requestedProvider != null) {
            return requestedProvider;
        }

        if (existingProvider != null) {
            return existingProvider;
        }

        Object appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata instanceof Map<?, ?> metadata) {
            OauthProvider provider = providerFromValue(metadata.get("provider"));
            if (provider != null) {
                return provider;
            }
        }

        OauthProvider provider = providerFromValue(jwt.getClaim("provider"));
        if (provider != null) {
            return provider;
        }

        throw new IllegalArgumentException("Unable to determine oauth provider from JWT claims");
    }

    private OauthProvider providerFromValue(Object value) {
        if (!(value instanceof String providerName) || !hasText(providerName)) {
            return null;
        }

        return switch (providerName.trim().toLowerCase()) {
            case "google" -> OauthProvider.GOOGLE;
            case "github" -> OauthProvider.GITHUB;
            default -> null;
        };
    }

    private String defaultNameFor(Jwt jwt) {
        return firstNonBlank(
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("preferred_username"),
                emailPrefix(jwt.getClaimAsString("email")),
                "User"
        );
    }

    private String emailPrefix(String email) {
        if (!hasText(email)) {
            return null;
        }

        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String coalesce(String preferredValue, String fallbackValue) {
        return preferredValue != null ? preferredValue : fallbackValue;
    }
}
