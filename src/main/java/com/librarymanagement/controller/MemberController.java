package com.librarymanagement.controller;

import com.librarymanagement.dto.ErrorResponse;
import com.librarymanagement.dto.MemberRequest;
import com.librarymanagement.dto.MemberResponse;
import com.librarymanagement.service.MemberService;
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
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "REST APIs for managing library member profiles and statuses")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Register a new member", description = "Creates a new library membership profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member registered successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Member with email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MemberResponse> registerMember(@Valid @RequestBody MemberRequest request) {
        log.info("REST request to register member: {}", request.getEmail());
        MemberResponse response = memberService.registerMember(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieves a member's profile using their unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MemberResponse> getMemberById(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long id) {
        log.info("REST request to get member by ID: {}", id);
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get member by email", description = "Retrieves a member's profile using their email address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MemberResponse> getMemberByEmail(
            @Parameter(description = "Email address", example = "alice.johnson@example.com") @PathVariable String email) {
        log.info("REST request to get member by email: {}", email);
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    @GetMapping
    @Operation(summary = "List all members", description = "Retrieves a paginated list of all library members")
    @ApiResponse(responseCode = "200", description = "Paginated list of members retrieved")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("REST request to get members page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search members", description = "Searches members by name or email")
    @ApiResponse(responseCode = "200", description = "Search results retrieved")
    public ResponseEntity<Page<MemberResponse>> searchMembers(
            @Parameter(description = "Keyword to search in name or email", example = "Alice") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("REST request to search members with query: '{}'", query);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(memberService.searchMembers(query, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member profile", description = "Updates member details such as name, phone, email, status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member successfully updated",
                    content = @Content(schema = @Schema(implementation = MemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already used by another member",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MemberResponse> updateMember(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        log.info("REST request to update member ID: {}", id);
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member", description = "Deletes member profile if they have no unreturned books")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete member with active borrowed books",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "Member ID", example = "1") @PathVariable Long id) {
        log.info("REST request to delete member ID: {}", id);
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
