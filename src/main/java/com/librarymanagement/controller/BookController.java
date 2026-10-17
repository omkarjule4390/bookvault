package com.librarymanagement.controller;

import com.librarymanagement.dto.BookRequest;
import com.librarymanagement.dto.BookResponse;
import com.librarymanagement.dto.ErrorResponse;
import com.librarymanagement.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "REST APIs for managing book catalog and inventory")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Add a new book", description = "Creates a new book entry in the library catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book successfully created",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Book with ISBN already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        log.info("REST request to create book: {}", request.getTitle());
        BookResponse response = bookService.createBook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves book details for the given database ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book details found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookResponse> getBookById(
            @Parameter(description = "Book ID", example = "1") @PathVariable Long id) {
        log.info("REST request to get book ID: {}", id);
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Retrieves a book by its unique ISBN number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book details found",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found with specified ISBN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookResponse> getBookByIsbn(
            @Parameter(description = "10 or 13 digit ISBN", example = "9780132350884") @PathVariable String isbn) {
        log.info("REST request to get book by ISBN: {}", isbn);
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @GetMapping
    @Operation(summary = "List all books", description = "Retrieves a paginated list of all books in catalog")
    @ApiResponse(responseCode = "200", description = "Paginated list of books retrieved")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("REST request to get books page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bookService.getAllBooks(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Searches books matching title or author keyword")
    @ApiResponse(responseCode = "200", description = "Search results retrieved")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @Parameter(description = "Keyword to search in title or author", example = "Clean") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("REST request to search books: query='{}'", query);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bookService.searchBooks(query, pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter books by category", description = "Retrieves books belonging to a specific category")
    @ApiResponse(responseCode = "200", description = "Category books retrieved")
    public ResponseEntity<Page<BookResponse>> getBooksByCategory(
            @Parameter(description = "Category name", example = "Software Engineering") @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get books in category: '{}'", category);
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return ResponseEntity.ok(bookService.getBooksByCategory(category, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing book", description = "Updates book details such as title, author, inventory count")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book successfully updated",
                    content = @Content(schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or copy count violation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ISBN conflict with another book",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        log.info("REST request to update book ID: {}", id);
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes a book if it has no active borrow records")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete book with active loans",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID", example = "1") @PathVariable Long id) {
        log.info("REST request to delete book ID: {}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
