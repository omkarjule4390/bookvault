package com.librarymanagement.controller;

import com.librarymanagement.dto.BorrowRequest;
import com.librarymanagement.dto.BorrowResponse;
import com.librarymanagement.dto.ErrorResponse;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/borrow")
@RequiredArgsConstructor
@Tag(name = "Borrowing & Returns", description = "REST APIs for borrowing books, returning books, tracking loans, and calculating overdue fines")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    @Operation(summary = "Borrow a book", description = "Issues a book to a member, decrements inventory, sets a 14-day due date")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book borrowed successfully",
                    content = @Content(schema = @Schema(implementation = BorrowResponse.class))),
            @ApiResponse(responseCode = "400", description = "Business rule violation (e.g. no copies available, limit reached, inactive member)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book or Member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        log.info("REST request to borrow book: memberId={}, bookId={}", request.getMemberId(), request.getBookId());
        BorrowResponse response = borrowService.borrowBook(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book", description = "Processes return of a borrowed book, increments inventory, and calculates overdue fine if late")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book returned successfully",
                    content = @Content(schema = @Schema(implementation = BorrowResponse.class))),
            @ApiResponse(responseCode = "400", description = "Book was already returned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Borrow record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BorrowResponse> returnBook(
            @Parameter(description = "Borrow record ID", example = "1") @PathVariable Long id) {
        log.info("REST request to return book for borrow record ID: {}", id);
        return ResponseEntity.ok(borrowService.returnBook(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get borrow record by ID", description = "Retrieves loan details for a specific borrow record ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrow record found",
                    content = @Content(schema = @Schema(implementation = BorrowResponse.class))),
            @ApiResponse(responseCode = "404", description = "Borrow record not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BorrowResponse> getBorrowRecordById(
            @Parameter(description = "Borrow record ID", example = "1") @PathVariable Long id) {
        log.info("REST request to get borrow record ID: {}", id);
        return ResponseEntity.ok(borrowService.getBorrowRecordById(id));
    }

    @GetMapping
    @Operation(summary = "List all borrow records", description = "Retrieves a paginated list of all borrow transactions")
    @ApiResponse(responseCode = "200", description = "Paginated borrow records retrieved")
    public ResponseEntity<Page<BorrowResponse>> getAllBorrowRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("REST request to get borrow records page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(borrowService.getAllBorrowRecords(pageable));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get borrow records for a member", description = "Retrieves borrowing history for a specific member")
    @ApiResponse(responseCode = "200", description = "Member borrow records retrieved")
    public ResponseEntity<Page<BorrowResponse>> getBorrowRecordsByMember(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get borrow records for member ID: {}", memberId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(borrowService.getBorrowRecordsByMember(memberId, pageable));
    }

    @GetMapping("/member/{memberId}/active")
    @Operation(summary = "Get active borrowings for a member", description = "Retrieves all currently unreturned books borrowed by a member")
    @ApiResponse(responseCode = "200", description = "Active borrowings retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BorrowResponse.class))))
    public ResponseEntity<List<BorrowResponse>> getActiveBorrowingsForMember(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long memberId) {
        log.info("REST request to get active borrowings for member ID: {}", memberId);
        return ResponseEntity.ok(borrowService.getActiveBorrowingsForMember(memberId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter borrow records by status", description = "Retrieves borrow records filtered by status (BORROWED, RETURNED, OVERDUE)")
    @ApiResponse(responseCode = "200", description = "Filtered borrow records retrieved")
    public ResponseEntity<Page<BorrowResponse>> getBorrowRecordsByStatus(
            @Parameter(description = "Borrow status", example = "BORROWED") @PathVariable BorrowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to get borrow records with status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(borrowService.getBorrowRecordsByStatus(status, pageable));
    }

    @GetMapping("/overdue")
    @Operation(summary = "List all overdue borrow records", description = "Retrieves all active loans where current date is past due date")
    @ApiResponse(responseCode = "200", description = "List of overdue borrow records",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BorrowResponse.class))))
    public ResponseEntity<List<BorrowResponse>> getOverdueBorrowRecords() {
        log.info("REST request to get overdue borrow records");
        return ResponseEntity.ok(borrowService.getOverdueBorrowRecords());
    }
}
