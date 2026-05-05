package com.studyhive.spring_boot_docker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudySessionControllerTest {

    private StudySessionController studySessionController;
    private StudySessionRepository studySessionRepository;
    private StudyGroupRepository studyGroupRepository;

    @BeforeEach
    void setUp() {
        studySessionController = new StudySessionController();
        studySessionRepository = mock(StudySessionRepository.class);
        studyGroupRepository = mock(StudyGroupRepository.class);

        ReflectionTestUtils.setField(studySessionController, "studySessionRepository", studySessionRepository);
        ReflectionTestUtils.setField(studySessionController, "studyGroupRepository", studyGroupRepository);
    }

    @Test
    void createSessionReturnsNotFoundWhenGroupDoesNotExist() {
        StudySession session = new StudySession();
        session.setGroupId(42L);
        when(studyGroupRepository.findById(42L)).thenReturn(Optional.empty());

        ResponseEntity<StudySession> response = studySessionController.createSession(session, jwt("owner-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSessionReturnsForbiddenWhenRequesterDoesNotOwnGroup() {
        StudySession session = new StudySession();
        session.setGroupId(12L);

        StudyGroup group = new StudyGroup();
        group.setCreatorId("owner-1");
        when(studyGroupRepository.findById(12L)).thenReturn(Optional.of(group));

        ResponseEntity<StudySession> response = studySessionController.createSession(session, jwt("other-user"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteSessionDeletesWhenRequesterOwnsParentGroup() {
        StudySession session = new StudySession();
        session.setGroupId(8L);

        StudyGroup group = new StudyGroup();
        group.setCreatorId("owner-1");

        when(studySessionRepository.findById(7L)).thenReturn(Optional.of(session));
        when(studyGroupRepository.findById(8L)).thenReturn(Optional.of(group));

        ResponseEntity<Void> response = studySessionController.deleteSession(7L, jwt("owner-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(studySessionRepository).delete(session);
    }

    @Test
    void getSessionsByGroupReturnsRepositoryResults() {
        StudySession first = new StudySession();
        first.setTitle("Midterm Review");
        StudySession second = new StudySession();
        second.setTitle("Project Prep");
        when(studySessionRepository.findByGroupId(3L)).thenReturn(List.of(first, second));

        ResponseEntity<List<StudySession>> response = studySessionController.getSessionsByGroup(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).extracting(StudySession::getTitle)
                .containsExactly("Midterm Review", "Project Prep");
    }

    private static Jwt jwt(String subject) {
        Instant issuedAt = Instant.now();
        return new Jwt("token", issuedAt, issuedAt.plusSeconds(300), Map.of("alg", "none"), Map.of("sub", subject));
    }
}
