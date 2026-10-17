package com.librarymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing book details")
public class BookResponse {

    @Schema(description = "Unique identifier of the book", example = "1")
    private Long id;

    @Schema(description = "Book title", example = "Clean Code: A Handbook of Agile Software Craftsmanship")
    private String title;

    @Schema(description = "Author name", example = "Robert C. Martin")
    private String author;

    @Schema(description = "10 or 13 digit ISBN", example = "9780132350884")
    private String isbn;

    @Schema(description = "Category or genre", example = "Software Engineering")
    private String category;

    @Schema(description = "Total number of copies owned by library", example = "5")
    private Integer totalCopies;

    @Schema(description = "Number of copies currently available for borrowing", example = "4")
    private Integer availableCopies;

    @Schema(description = "Publication year", example = "2008")
    private Integer publicationYear;

    @Schema(description = "Timestamp when record was created", example = "2026-09-13T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when record was last updated", example = "2026-09-13T10:15:30")
    private LocalDateTime updatedAt;
}
