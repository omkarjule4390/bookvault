package com.librarymanagement.service.impl;

import com.librarymanagement.dto.BorrowRequest;
import com.librarymanagement.dto.BorrowResponse;
import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.entity.Member;
import com.librarymanagement.entity.MemberStatus;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.ResourceNotFoundException;
import com.librarymanagement.mapper.EntityMapper;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRepository;
import com.librarymanagement.repository.MemberRepository;
import com.librarymanagement.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BorrowServiceImpl implements BorrowService {

    public static final int MAX_BORROW_LIMIT = 5;
    public static final int DEFAULT_BORROW_DAYS = 14;
    public static final BigDecimal DAILY_FINE_RATE = BigDecimal.valueOf(1.00);

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final EntityMapper entityMapper;

    @Override
    public BorrowResponse borrowBook(BorrowRequest request) {
        log.info("Processing borrow request: memberId={}, bookId={}", request.getMemberId(), request.getBookId());

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", request.getMemberId()));

        // 1. Validate member status
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(String.format(
                    "Member '%s' has status '%s' and is not permitted to borrow books",
                    member.getName(), member.getStatus()));
        }

        // 2. Validate member borrowing limit
        long activeBorrows = borrowRepository.countByMemberIdAndStatus(member.getId(), BorrowStatus.BORROWED);
        if (activeBorrows >= MAX_BORROW_LIMIT) {
            throw new BusinessException(String.format(
                    "Member '%s' has already reached the maximum active borrowing limit of %d books",
                    member.getName(), MAX_BORROW_LIMIT));
        }

        // 3. Validate member doesn't already have an active loan of the same book
        if (borrowRepository.findByMemberIdAndBookIdAndStatus(member.getId(), book.getId(), BorrowStatus.BORROWED).isPresent()) {
            throw new BusinessException(String.format(
                    "Member '%s' already has an active borrowed copy of book '%s'",
                    member.getName(), book.getTitle()));
        }

        // 4. Validate book availability
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException(String.format(
                    "No copies of '%s' are currently available for borrowing", book.getTitle()));
        }

        // 5. Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 6. Create borrow record
        int duration = (request.getBorrowDays() != null && request.getBorrowDays() > 0)
                ? request.getBorrowDays()
                : DEFAULT_BORROW_DAYS;

        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowDate(today)
                .dueDate(today.plusDays(duration))
                .status(BorrowStatus.BORROWED)
                .fineAmount(BigDecimal.ZERO)
                .remarks(request.getRemarks())
                .build();

        BorrowRecord savedRecord = borrowRepository.save(record);
        log.info("Book successfully borrowed. BorrowRecord ID: {}", savedRecord.getId());

        return entityMapper.toBorrowResponse(savedRecord);
    }

    @Override
    public BorrowResponse returnBook(Long borrowRecordId) {
        log.info("Processing return for BorrowRecord ID: {}", borrowRecordId);

        BorrowRecord record = borrowRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord", "id", borrowRecordId));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BusinessException(String.format(
                    "Book '%s' was already returned on %s",
                    record.getBook().getTitle(), record.getReturnDate()));
        }

        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);
        record.setStatus(BorrowStatus.RETURNED);

        // Calculate fine if returned past due date
        if (returnDate.isAfter(record.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            BigDecimal fine = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(daysLate));
            record.setFineAmount(fine);

            String lateRemark = String.format("Returned %d day(s) overdue. Calculated fine: $%s", daysLate, fine);
            record.setRemarks(record.getRemarks() != null && !record.getRemarks().isBlank()
                    ? record.getRemarks() + " | " + lateRemark
                    : lateRemark);
            log.info("Book returned late by {} days. Fine imposed: ${}", daysLate, fine);
        }

        // Return copy back to book inventory
        Book book = record.getBook();
        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
        bookRepository.save(book);

        BorrowRecord updatedRecord = borrowRepository.save(record);
        log.info("Book return processed successfully for BorrowRecord ID: {}", updatedRecord.getId());

        return entityMapper.toBorrowResponse(updatedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowResponse getBorrowRecordById(Long id) {
        log.debug("Fetching borrow record with ID: {}", id);
        BorrowRecord record = borrowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord", "id", id));
        return entityMapper.toBorrowResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getAllBorrowRecords(Pageable pageable) {
        log.debug("Fetching all borrow records with pagination: {}", pageable);
        return borrowRepository.findAll(pageable)
                .map(entityMapper::toBorrowResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getBorrowRecordsByMember(Long memberId, Pageable pageable) {
        log.debug("Fetching borrow records for member ID: {}", memberId);
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", "id", memberId);
        }
        return borrowRepository.findByMemberId(memberId, pageable)
                .map(entityMapper::toBorrowResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowResponse> getBorrowRecordsByStatus(BorrowStatus status, Pageable pageable) {
        log.debug("Fetching borrow records with status: {}", status);
        return borrowRepository.findByStatus(status, pageable)
                .map(entityMapper::toBorrowResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponse> getOverdueBorrowRecords() {
        log.debug("Fetching all currently overdue borrow records");
        return borrowRepository.findOverdueBorrowRecords(LocalDate.now())
                .stream()
                .map(entityMapper::toBorrowResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponse> getActiveBorrowingsForMember(Long memberId) {
        log.debug("Fetching active borrow records for member ID: {}", memberId);
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", "id", memberId);
        }
        return borrowRepository.findActiveBorrowingsByMemberId(memberId)
                .stream()
                .map(entityMapper::toBorrowResponse)
                .toList();
    }
}
