package com.studyhive.spring_boot_docker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record StudySessionRequest(
        @NotNull Long groupId,
        @NotBlank String title,
        String topic,
        @NotNull OffsetDateTime scheduledAt,
        String location,
        String notes,
        Integer durationMinutes
) {
}
