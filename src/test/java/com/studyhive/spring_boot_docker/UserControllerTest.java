package com.studyhive.spring_boot_docker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
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
        user.setOauthProvider(OauthProvider.GOOGLE);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userController.createUser(
                user,
                jwt(Map.of("email", "jwt@example.com", "jti", "jwt-id"))
        );

        assertThat(savedUser.getEmail()).isEqualTo("jwt@example.com");
        assertThat(savedUser.getUserId()).isEqualTo("jwt-id");
        verify(userRepository).save(user);
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
