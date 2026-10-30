package com.librarymanagement.service.impl;

import com.librarymanagement.dto.BookRequest;
import com.librarymanagement.dto.BookResponse;
import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowStatus;
import com.librarymanagement.exception.BusinessException;
import com.librarymanagement.exception.DuplicateResourceException;
import com.librarymanagement.exception.ResourceNotFoundException;
import com.librarymanagement.mapper.EntityMapper;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRepository;
import com.librarymanagement.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final EntityMapper entityMapper;

    @Override
    public BookResponse createBook(BookRequest request) {
        log.info("Creating new book with ISBN: {}", request.getIsbn());

        if (bookRepository.existsByIsbn(request.getIsbn().trim())) {
            throw new DuplicateResourceException("Book", "ISBN", request.getIsbn());
        }

        Book book = entityMapper.toBookEntity(request);
        Book savedBook = bookRepository.save(book);
        log.info("Book successfully created with ID: {}", savedBook.getId());

        return entityMapper.toBookResponse(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        log.debug("Fetching book with ID: {}", id);
        Book book = findBookOrThrow(id);
        return entityMapper.toBookResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        log.debug("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "ISBN", isbn));
        return entityMapper.toBookResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        log.debug("Fetching all books with pagination: {}", pageable);
        return bookRepository.findAll(pageable)
                .map(entityMapper::toBookResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String query, Pageable pageable) {
        log.debug("Searching books matching query: '{}'", query);
        String trimmed = query != null ? query.trim() : "";
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(trimmed, trimmed, pageable)
                .map(entityMapper::toBookResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksByCategory(String category, Pageable pageable) {
        log.debug("Fetching books for category: '{}'", category);
        return bookRepository.findByCategoryIgnoreCase(category.trim(), pageable)
                .map(entityMapper::toBookResponse);
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        log.info("Updating book with ID: {}", id);
        Book book = findBookOrThrow(id);

        String trimmedIsbn = request.getIsbn().trim();
        if (bookRepository.existsByIsbnAndIdNot(trimmedIsbn, id)) {
            throw new DuplicateResourceException("Book", "ISBN", trimmedIsbn);
        }

        int currentlyBorrowed = book.getTotalCopies() - book.getAvailableCopies();
        if (request.getTotalCopies() < currentlyBorrowed) {
            throw new BusinessException(String.format(
                    "Cannot set total copies to %d because %d copies are currently borrowed",
                    request.getTotalCopies(), currentlyBorrowed));
        }

        entityMapper.updateBookEntity(book, request);
        Book updatedBook = bookRepository.save(book);
        log.info("Book successfully updated with ID: {}", updatedBook.getId());

        return entityMapper.toBookResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        log.info("Attempting to delete book with ID: {}", id);
        Book book = findBookOrThrow(id);

        long activeBorrows = borrowRepository.countByBookIdAndStatus(id, BorrowStatus.BORROWED);
        if (activeBorrows > 0) {
            throw new BusinessException(String.format(
                    "Cannot delete book '%s' (ID: %d) because it currently has %d active borrow record(s)",
                    book.getTitle(), id, activeBorrows));
        }

        // Only returned (history) records can remain at this point. They hold a NOT NULL foreign key to the book,
        // so they are removed in the same transaction; if anything fails, everything rolls back.
        long removedHistory = borrowRepository.deleteByBookId(id);
        log.info("Removed {} returned borrow record(s) of book ID: {}", removedHistory, id);
        bookRepository.delete(book);
        log.info("Book with ID: {} successfully deleted", id);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }
}
