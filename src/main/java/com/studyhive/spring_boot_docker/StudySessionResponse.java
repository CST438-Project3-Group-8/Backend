package com.studyhive.spring_boot_docker;

import java.time.OffsetDateTime;

public record StudySessionResponse(
        Long id,
        Long groupId,
        String title,
        String topic,
        OffsetDateTime scheduledAt,
        String location,
        String notes
) {
    public static StudySessionResponse fromEntity(StudySession studySession) {
        return new StudySessionResponse(
                studySession.getId(),
                studySession.getGroup().getId(),
                studySession.getTitle(),
                studySession.getTopic(),
                studySession.getScheduledAt(),
                studySession.getLocation(),
                studySession.getNotes()
        );
    }
}
