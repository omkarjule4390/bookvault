package com.librarymanagement.dto;

import com.librarymanagement.entity.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing member details")
public class MemberResponse {

    @Schema(description = "Unique identifier of the member", example = "1")
    private Long id;

    @Schema(description = "Full name of the member", example = "Alice Johnson")
    private String name;

    @Schema(description = "Email address", example = "alice.johnson@example.com")
    private String email;

    @Schema(description = "Contact phone number", example = "+1-555-0199")
    private String phone;

    @Schema(description = "Date when the membership was registered", example = "2026-01-15")
    private LocalDate membershipDate;

    @Schema(description = "Current membership status", example = "ACTIVE")
    private MemberStatus status;

    @Schema(description = "Timestamp when record was created", example = "2026-09-13T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when record was last updated", example = "2026-09-13T10:15:30")
    private LocalDateTime updatedAt;
}
