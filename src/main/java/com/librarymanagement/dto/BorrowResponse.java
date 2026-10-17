package com.librarymanagement.dto;

import com.librarymanagement.entity.BorrowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing borrow transaction details")
public class BorrowResponse {

    @Schema(description = "Unique identifier of the borrow record", example = "1")
    private Long id;

    @Schema(description = "ID of the borrowed book", example = "1")
    private Long bookId;

    @Schema(description = "Title of the borrowed book", example = "Clean Code: A Handbook of Agile Software Craftsmanship")
    private String bookTitle;

    @Schema(description = "ISBN of the borrowed book", example = "9780132350884")
    private String bookIsbn;

    @Schema(description = "ID of the borrower member", example = "1")
    private Long memberId;

    @Schema(description = "Name of the borrower member", example = "Alice Johnson")
    private String memberName;

    @Schema(description = "Email of the borrower member", example = "alice.johnson@example.com")
    private String memberEmail;

    @Schema(description = "Date the book was borrowed", example = "2026-09-13")
    private LocalDate borrowDate;

    @Schema(description = "Date the book is due for return", example = "2026-09-27")
    private LocalDate dueDate;

    @Schema(description = "Date the book was actually returned (null if not yet returned)", example = "2026-09-25")
    private LocalDate returnDate;

    @Schema(description = "Status of the borrowing (BORROWED, RETURNED, OVERDUE)", example = "BORROWED")
    private BorrowStatus status;

    @Schema(description = "Overdue fine amount if applicable", example = "0.00")
    private BigDecimal fineAmount;

    @Schema(description = "Transaction remarks or notes", example = "Requested for semester project")
    private String remarks;

    @Schema(description = "Record creation timestamp", example = "2026-09-13T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Record update timestamp", example = "2026-09-13T10:15:30")
    private LocalDateTime updatedAt;
}
