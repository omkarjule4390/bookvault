package com.librarymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for borrowing a book")
public class BorrowRequest {

    @NotNull(message = "Book ID is mandatory")
    @Schema(description = "ID of the book to borrow", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotNull(message = "Member ID is mandatory")
    @Schema(description = "ID of the member borrowing the book", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Min(value = 1, message = "Borrow duration must be at least 1 day")
    @Max(value = 90, message = "Borrow duration cannot exceed 90 days")
    @Schema(description = "Number of days for borrowing (default is 14 days)", example = "14")
    private Integer borrowDays;

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    @Schema(description = "Optional notes or remarks regarding the borrow transaction", example = "Requested for semester project")
    private String remarks;
}
