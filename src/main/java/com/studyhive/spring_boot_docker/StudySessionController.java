package com.studyhive.spring_boot_docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {
    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @PostMapping
    public ResponseEntity<StudySession> createSession(@RequestBody StudySession studySession, @AuthenticationPrincipal Jwt jwt) {
        return studyGroupRepository.findById(studySession.getGroupId())
                .map(group -> {
                    if (!group.getCreatorId().equals(jwt.getSubject())) {
                        return new ResponseEntity<StudySession>(HttpStatus.FORBIDDEN);
                    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return studySessionRepository.findById(id)
                .map(studySession -> studyGroupRepository.findById(studySession.getGroupId())
                        .map(group -> {
                            if (!group.getCreatorId().equals(jwt.getSubject())) {
                                return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
                            }

                            studySessionRepository.delete(studySession);
                            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                        })
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.notFound().build());
    }
}
