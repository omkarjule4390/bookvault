package com.librarymanagement.dto;

import com.librarymanagement.entity.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for registering or updating a library member")
public class MemberRequest {

    @NotBlank(message = "Member name is mandatory")
    @Size(max = 120, message = "Name cannot exceed 120 characters")
    @Schema(description = "Full name of the member", example = "Alice Johnson", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    @Schema(description = "Email address (must be unique)", example = "alice.johnson@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^[+0-9\\-\\s()]{7,25}$", message = "Phone number must be a valid format")
    @Schema(description = "Contact phone number", example = "+1-555-0199", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Membership status (ACTIVE, SUSPENDED, INACTIVE)", example = "ACTIVE")
    private MemberStatus status;
}
