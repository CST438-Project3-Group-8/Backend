package com.studyhive.spring_boot_docker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserController userController;
    private UserRepository userRepository;
    private UserCourseRepository userCourseRepository;
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userCourseRepository = mock(UserCourseRepository.class);
        courseRepository = mock(CourseRepository.class);
        userController = new UserController(userRepository, userCourseRepository, courseRepository);
    }

    @Test
    void createUserUsesEmailFromJwtClaim() {
        User user = new User();
        user.setName("Taylor");
        user.setEmail("wrong@example.com");

        when(userRepository.findByUserId("user-123")).thenReturn(List.of());
        when(userRepository.findByEmail("jwt@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<User> response = userController.createUser(
                user,
                jwt(Map.of(
                        "sub", "user-123",
                        "email", "jwt@example.com",
                        "jti", "jwt-id",
                        "app_metadata", Map.of("provider", "google")
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("jwt@example.com");
        assertThat(response.getBody().getUserId()).isEqualTo("user-123");
        assertThat(response.getBody().getOauthProvider()).isEqualTo(OauthProvider.GOOGLE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserAllowsEmptyBodyForLoginBootstrap() {
        when(userRepository.findByUserId("user-123")).thenReturn(List.of());
        when(userRepository.findByEmail("bootstrap@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<User> response = userController.createUser(
                null,
                jwt(Map.of(
                        "sub", "user-123",
                        "email", "bootstrap@example.com",
                        "app_metadata", Map.of("provider", "google")
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo("user-123");
        assertThat(response.getBody().getEmail()).isEqualTo("bootstrap@example.com");
        assertThat(response.getBody().getName()).isEqualTo("bootstrap");
        assertThat(response.getBody().getOauthProvider()).isEqualTo(OauthProvider.GOOGLE);
    }

    @Test
    void getMyProfileAutoCreatesUserWhenMissing() {
        when(userRepository.findByUserId("user-123")).thenReturn(List.of());
        when(userRepository.findByEmail("new.user@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<User> response = userController.getMyProfile(
                jwt(Map.of(
                        "sub", "user-123",
                        "email", "new.user@example.com",
                        "app_metadata", Map.of("provider", "github")
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo("user-123");
        assertThat(response.getBody().getEmail()).isEqualTo("new.user@example.com");
        assertThat(response.getBody().getName()).isEqualTo("new.user");
        assertThat(response.getBody().getOauthProvider()).isEqualTo(OauthProvider.GITHUB);
    }

    @Test
    void createUserReusesExistingUserWithMatchingEmail() {
        User existingUser = new User();
        existingUser.setEmail("jusmartinez@csumb.edu");
        existingUser.setName("Justin");
        existingUser.setOauthProvider(OauthProvider.GOOGLE);

        when(userRepository.findByUserId("new-sub-123")).thenReturn(List.of());
        when(userRepository.findByEmail("jusmartinez@csumb.edu")).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<User> response = userController.createUser(
                null,
                jwt(Map.of(
                        "sub", "new-sub-123",
                        "email", "jusmartinez@csumb.edu",
                        "app_metadata", Map.of("provider", "google")
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo("new-sub-123");
        assertThat(response.getBody().getEmail()).isEqualTo("jusmartinez@csumb.edu");
        assertThat(response.getBody().getName()).isEqualTo("Justin");
        assertThat(response.getBody().getOauthProvider()).isEqualTo(OauthProvider.GOOGLE);
        verify(userRepository).save(existingUser);
    }

    @Test
    void createUserFallsBackToEmailProviderWhenSocialProviderIsAmbiguous() {
        when(userRepository.findByUserId("user-123")).thenReturn(List.of());
        when(userRepository.findByEmail("linked@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<User> response = userController.createUser(
                null,
                jwt(Map.of(
                        "sub", "user-123",
                        "email", "linked@example.com",
                        "app_metadata", Map.of(
                                "provider", "email",
                                "providers", List.of("email", "google", "github")
                        )
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOauthProvider()).isEqualTo(OauthProvider.EMAIL);
    }

    @Test
    void createUserPrefersSingleSocialProviderFromProvidersList() {
        when(userRepository.findByUserId("user-123")).thenReturn(List.of());
        when(userRepository.findByEmail("social@example.com")).thenReturn(java.util.Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<User> response = userController.createUser(
                null,
                jwt(Map.of(
                        "sub", "user-123",
                        "email", "social@example.com",
                        "app_metadata", Map.of(
                                "provider", "email",
                                "providers", List.of("email", "google")
                        )
                ))
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getOauthProvider()).isEqualTo(OauthProvider.GOOGLE);
    }

    @Test
    void deleteUserReturnsNotFoundWhenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseEntity<Void> response = userController.deleteUser(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private static Jwt jwt(Map<String, Object> claims) {
        Instant issuedAt = Instant.now();
        return new Jwt("token", issuedAt, issuedAt.plusSeconds(300), Map.of("alg", "none"), claims);
    }
}
