package com.librarymanagement.mapper;

import com.librarymanagement.dto.*;
import com.librarymanagement.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public Book toBookEntity(BookRequest request) {
        if (request == null) {
            return null;
        }
        return Book.builder()
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .isbn(request.getIsbn().trim())
                .category(request.getCategory() != null ? request.getCategory().trim() : null)
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .publicationYear(request.getPublicationYear())
                .build();
    }

    public void updateBookEntity(Book book, BookRequest request) {
        if (book == null || request == null) {
            return;
        }
        // Calculate difference in total copies and adjust available copies accordingly
        int copiesDifference = request.getTotalCopies() - book.getTotalCopies();
        int newAvailableCopies = book.getAvailableCopies() + copiesDifference;

        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor().trim());
        book.setIsbn(request.getIsbn().trim());
        book.setCategory(request.getCategory() != null ? request.getCategory().trim() : null);
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(Math.max(0, newAvailableCopies));
        book.setPublicationYear(request.getPublicationYear());
    }

    public BookResponse toBookResponse(Book book) {
        if (book == null) {
            return null;
        }
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .category(book.getCategory())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .publicationYear(book.getPublicationYear())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    public Member toMemberEntity(MemberRequest request) {
        if (request == null) {
            return null;
        }
        return Member.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .status(request.getStatus() != null ? request.getStatus() : MemberStatus.ACTIVE)
                .build();
    }

    public void updateMemberEntity(Member member, MemberRequest request) {
        if (member == null || request == null) {
            return;
        }
        member.setName(request.getName().trim());
        member.setEmail(request.getEmail().trim().toLowerCase());
        member.setPhone(request.getPhone().trim());
        if (request.getStatus() != null) {
            member.setStatus(request.getStatus());
        }
    }

    public MemberResponse toMemberResponse(Member member) {
        if (member == null) {
            return null;
        }
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .membershipDate(member.getMembershipDate())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    public BorrowResponse toBorrowResponse(BorrowRecord record) {
        if (record == null) {
            return null;
        }
        return BorrowResponse.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .bookIsbn(record.getBook().getIsbn())
                .memberId(record.getMember().getId())
                .memberName(record.getMember().getName())
                .memberEmail(record.getMember().getEmail())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .fineAmount(record.getFineAmount())
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
