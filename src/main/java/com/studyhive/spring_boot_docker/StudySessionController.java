package com.studyhive.spring_boot_docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {
    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

//    @PostMapping
//    public ResponseEntity<StudySession> createSession(@RequestBody StudySession studySession, @AuthenticationPrincipal Jwt jwt) {
//        return studyGroupRepository.findById(studySession.getGroupId())
//                .map(group -> {
//                    if (!group.getCreatorId().equals(jwt.getSubject())) {
//                        return new ResponseEntity<StudySession>(HttpStatus.FORBIDDEN);
//                    }
//
//                    StudySession savedSession = studySessionRepository.save(studySession);
//                    return new ResponseEntity<>(savedSession, HttpStatus.CREATED);
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
@PostMapping
public ResponseEntity<StudySession> createSession(@Valid @RequestBody StudySession studySession) {
    return studyGroupRepository.findById(studySession.getGroupId())
            .map(group -> {
                StudySession savedSession = studySessionRepository.save(studySession);
                return new ResponseEntity<>(savedSession, HttpStatus.CREATED);
            })
            .orElse(ResponseEntity.notFound().build());
}

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<StudySession>> getSessionsByGroup(@PathVariable Long groupId) {
        List<StudySession> studySessions = studySessionRepository.findByGroupId(groupId);
        return new ResponseEntity<>(studySessions, HttpStatus.OK);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteSession(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
//        return studySessionRepository.findById(id)
//                .map(studySession -> studyGroupRepository.findById(studySession.getGroupId())
//                        .map(group -> {
//                            if (!group.getCreatorId().equals(jwt.getSubject())) {
//                                return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
//                            }
//
//                            studySessionRepository.delete(studySession);
//                            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
//                        })
//                        .orElse(ResponseEntity.notFound().build()))
//                .orElse(ResponseEntity.notFound().build());
//    }
    //for local testing
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
    if (!studySessionRepository.existsById(id)) {
        return ResponseEntity.notFound().build();
    }

    studySessionRepository.deleteById(id);
    return ResponseEntity.noContent().build();
}
//to update session
@PutMapping("/{id}")
public ResponseEntity<StudySession> updateSession(
        @PathVariable Long id,
        @RequestBody StudySession updatedSession
) {
    return studySessionRepository.findById(id)
            .map(session -> {
                session.setTitle(updatedSession.getTitle());
                session.setTopic(updatedSession.getTopic());
                session.setScheduledAt(updatedSession.getScheduledAt());
                session.setLocation(updatedSession.getLocation());
                session.setNotes(updatedSession.getNotes());
                session.setDurationMinutes(updatedSession.getDurationMinutes());

                StudySession savedSession = studySessionRepository.save(session);
                return ResponseEntity.ok(savedSession);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
}
}
