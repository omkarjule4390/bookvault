package com.librarymanagement.service;

import com.librarymanagement.dto.BorrowRequest;
import com.librarymanagement.dto.BorrowResponse;
import com.librarymanagement.entity.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(BorrowRequest request);

    BorrowResponse returnBook(Long borrowRecordId);

    BorrowResponse getBorrowRecordById(Long id);

    Page<BorrowResponse> getAllBorrowRecords(Pageable pageable);

    Page<BorrowResponse> getBorrowRecordsByMember(Long memberId, Pageable pageable);

    Page<BorrowResponse> getBorrowRecordsByStatus(BorrowStatus status, Pageable pageable);

    List<BorrowResponse> getOverdueBorrowRecords();

    List<BorrowResponse> getActiveBorrowingsForMember(Long memberId);
}
