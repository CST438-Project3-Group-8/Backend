package com.studyhive.spring_boot_docker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank String name,
        @Size(max = 500) String bio
) {}