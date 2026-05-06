package com.studyhive.spring_boot_docker;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {
    private final StudySessionRepository studySessionRepository;
    private final StudyGroupRepository studyGroupRepository;

    public StudySessionController(
            StudySessionRepository studySessionRepository,
            StudyGroupRepository studyGroupRepository
    ) {
        this.studySessionRepository = studySessionRepository;
        this.studyGroupRepository = studyGroupRepository;
    }

    @PostMapping
    public ResponseEntity<StudySessionResponse> createSession(
            @Valid @RequestBody StudySessionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return studyGroupRepository.findById(request.groupId())
                .map(group -> {
                    if (!group.getCreatorId().equals(jwt.getSubject())) {
                        return new ResponseEntity<StudySessionResponse>(HttpStatus.FORBIDDEN);
                    }

                    StudySession studySession = buildSession(request, group);
                    StudySession savedSession = studySessionRepository.save(studySession);
                    return new ResponseEntity<>(StudySessionResponse.fromEntity(savedSession), HttpStatus.CREATED);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySessionResponse> getSessionById(@PathVariable Long id) {
        return studySessionRepository.findById(id)
                .map(StudySessionResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<StudySessionResponse>> getSessionsByGroup(@PathVariable Long groupId) {
        List<StudySessionResponse> studySessions = studySessionRepository
                .findByGroup_IdOrderByScheduledAtAsc(groupId)
                .stream()
                .map(StudySessionResponse::fromEntity)
                .toList();

        return new ResponseEntity<>(studySessions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudySessionResponse> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody StudySessionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return studySessionRepository.findById(id)
                .map(existingSession -> {
                    if (!existingSession.getGroup().getCreatorId().equals(jwt.getSubject())) {
                        return new ResponseEntity<StudySessionResponse>(HttpStatus.FORBIDDEN);
                    }

                    return studyGroupRepository.findById(request.groupId())
                            .map(group -> {
                                if (!group.getCreatorId().equals(jwt.getSubject())) {
                                    return new ResponseEntity<StudySessionResponse>(HttpStatus.FORBIDDEN);
                                }

                                existingSession.setGroup(group);
                                existingSession.setTitle(request.title());
                                existingSession.setTopic(request.topic());
                                existingSession.setScheduledAt(request.scheduledAt());
                                existingSession.setLocation(request.location());
                                existingSession.setNotes(request.notes());

                                StudySession savedSession = studySessionRepository.save(existingSession);
                                return new ResponseEntity<>(StudySessionResponse.fromEntity(savedSession), HttpStatus.OK);
                            })
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return studySessionRepository.findById(id)
                .map(studySession -> {
                    if (!studySession.getGroup().getCreatorId().equals(jwt.getSubject())) {
                        return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
                    }

                    studySessionRepository.delete(studySession);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private StudySession buildSession(StudySessionRequest request, StudyGroup group) {
        StudySession studySession = new StudySession();
        studySession.setGroup(group);
        studySession.setTitle(request.title());
        studySession.setTopic(request.topic());
        studySession.setScheduledAt(request.scheduledAt());
        studySession.setLocation(request.location());
        studySession.setNotes(request.notes());
        return studySession;
    }
}
