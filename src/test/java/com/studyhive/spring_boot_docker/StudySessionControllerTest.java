package com.studyhive.spring_boot_docker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        studySessionRepository = mock(StudySessionRepository.class);
        studyGroupRepository = mock(StudyGroupRepository.class);
        studySessionController = new StudySessionController(studySessionRepository, studyGroupRepository);
    }

    @Test
    void createSessionReturnsUnauthorizedWhenJwtMissing() {
        ResponseEntity<StudySessionResponse> response = studySessionController.createSession(request(42L), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createSessionReturnsNotFoundWhenGroupDoesNotExist() {
        when(studyGroupRepository.findById(42L)).thenReturn(Optional.empty());

        ResponseEntity<StudySessionResponse> response =
                studySessionController.createSession(request(42L), jwt("owner-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createSessionReturnsForbiddenWhenRequesterDoesNotOwnGroup() {
        StudyGroup group = groupOwnedBy("owner-1");
        when(studyGroupRepository.findById(12L)).thenReturn(Optional.of(group));

        ResponseEntity<StudySessionResponse> response =
                studySessionController.createSession(request(12L), jwt("other-user"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createSessionReturnsCreatedSessionResponse() {
        StudyGroup group = groupWithId(12L, "owner-1");
        when(studyGroupRepository.findById(12L)).thenReturn(Optional.of(group));
        when(studySessionRepository.save(any(StudySession.class))).thenAnswer(invocation -> {
            StudySession savedSession = invocation.getArgument(0);
            setId(savedSession, 99L);
            return savedSession;
        });

        ResponseEntity<StudySessionResponse> response =
                studySessionController.createSession(request(12L), jwt("owner-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().groupId()).isEqualTo(12L);
        assertThat(response.getBody().title()).isEqualTo("Midterm Review");
        assertThat(response.getBody().durationMinutes()).isEqualTo(90);
    }

    @Test
    void deleteSessionDeletesWhenRequesterOwnsParentGroup() {
        StudySession session = sessionForGroup(groupWithId(8L, "owner-1"), "Midterm Review");
        when(studySessionRepository.findById(7L)).thenReturn(Optional.of(session));

        ResponseEntity<Void> response = studySessionController.deleteSession(7L, jwt("owner-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(studySessionRepository).delete(session);
    }

    @Test
    void getSessionsByGroupReturnsRepositoryResults() {
        StudyGroup group = groupWithId(3L, "owner-1");
        StudySession first = sessionForGroup(group, "Midterm Review");
        first.setDurationMinutes(90);
        StudySession second = sessionForGroup(group, "Project Prep");
        second.setDurationMinutes(60);

        when(studySessionRepository.findByGroup_IdOrderByScheduledAtAsc(3L)).thenReturn(List.of(first, second));

        ResponseEntity<List<StudySessionResponse>> response = studySessionController.getSessionsByGroup(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).extracting(StudySessionResponse::title)
                .containsExactly("Midterm Review", "Project Prep");
        assertThat(response.getBody()).extracting(StudySessionResponse::durationMinutes)
                .containsExactly(90, 60);
    }

    @Test
    void updateSessionReturnsForbiddenWhenRequesterDoesNotOwnExistingSessionGroup() {
        StudySession existingSession = sessionForGroup(groupWithId(4L, "owner-1"), "Midterm Review");
        when(studySessionRepository.findById(20L)).thenReturn(Optional.of(existingSession));

        ResponseEntity<StudySessionResponse> response =
                studySessionController.updateSession(20L, request(4L), jwt("other-user"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static StudySessionRequest request(Long groupId) {
        return new StudySessionRequest(
                groupId,
                "Midterm Review",
                "Chapters 4-6",
                OffsetDateTime.of(2026, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC),
                "Library Room 2",
                "Bring practice problems",
                90
        );
    }

    private static StudyGroup groupOwnedBy(String creatorId) {
        StudyGroup group = new StudyGroup();
        group.setCreatorId(creatorId);
        return group;
    }

    private static StudyGroup groupWithId(Long id, String creatorId) {
        StudyGroup group = groupOwnedBy(creatorId);
        group.setId(id);
        return group;
    }

    private static StudySession sessionForGroup(StudyGroup group, String title) {
        StudySession session = new StudySession();
        session.setGroup(group);
        session.setTitle(title);
        session.setScheduledAt(OffsetDateTime.of(2026, 5, 10, 18, 0, 0, 0, ZoneOffset.UTC));
        session.setLocation("Library Room 2");
        session.setNotes("Bring practice problems");
        return session;
    }

    private static void setId(StudySession session, Long id) {
        try {
            var field = StudySession.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(session, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to set StudySession id for test", e);
        }
    }

    private static Jwt jwt(String subject) {
        Instant issuedAt = Instant.now();
        return new Jwt("token", issuedAt, issuedAt.plusSeconds(300), Map.of("alg", "none"), Map.of("sub", subject));
    }
}
