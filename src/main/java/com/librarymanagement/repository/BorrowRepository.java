package com.librarymanagement.repository;

import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRepository extends JpaRepository<BorrowRecord, Long> {

    Page<BorrowRecord> findByMemberId(Long memberId, Pageable pageable);

    List<BorrowRecord> findByMemberId(Long memberId);

    Page<BorrowRecord> findByBookId(Long bookId, Pageable pageable);

    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);

    Optional<BorrowRecord> findByMemberIdAndBookIdAndStatus(Long memberId, Long bookId, BorrowStatus status);

    long countByMemberIdAndStatus(Long memberId, BorrowStatus status);

    long countByBookIdAndStatus(Long bookId, BorrowStatus status);

    long deleteByBookId(Long bookId);

    long deleteByMemberId(Long memberId);

    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'BORROWED' AND b.dueDate < :currentDate")
    List<BorrowRecord> findOverdueBorrowRecords(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT b FROM BorrowRecord b WHERE b.member.id = :memberId AND b.status = 'BORROWED'")
    List<BorrowRecord> findActiveBorrowingsByMemberId(@Param("memberId") Long memberId);
}
