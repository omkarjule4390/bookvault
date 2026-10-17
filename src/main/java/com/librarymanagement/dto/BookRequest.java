package com.librarymanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating or updating a book")
public class BookRequest {

    @NotBlank(message = "Title is mandatory")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Schema(description = "Book title", example = "Clean Code: A Handbook of Agile Software Craftsmanship", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Author is mandatory")
    @Size(max = 150, message = "Author name cannot exceed 150 characters")
    @Schema(description = "Author name", example = "Robert C. Martin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @NotBlank(message = "ISBN is mandatory")
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "Invalid ISBN format. Must be a valid 10 or 13 digit ISBN")
    @Schema(description = "10 or 13 digit ISBN", example = "9780132350884", requiredMode = Schema.RequiredMode.REQUIRED)
    private String isbn;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Schema(description = "Category or genre of the book", example = "Software Engineering")
    private String category;

    @NotNull(message = "Total copies is mandatory")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Schema(description = "Total number of physical copies in inventory", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCopies;

    @Min(value = 1450, message = "Publication year must be valid")
    @Max(value = 2100, message = "Publication year cannot be in the distant future")
    @Schema(description = "Year the book was published", example = "2008")
    private Integer publicationYear;
}
