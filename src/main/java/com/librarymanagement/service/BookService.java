package com.librarymanagement.service;

import com.librarymanagement.dto.BookRequest;
import com.librarymanagement.dto.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponse createBook(BookRequest request);

    BookResponse getBookById(Long id);

    BookResponse getBookByIsbn(String isbn);

    Page<BookResponse> getAllBooks(Pageable pageable);

    Page<BookResponse> searchBooks(String query, Pageable pageable);

    Page<BookResponse> getBooksByCategory(String category, Pageable pageable);

    BookResponse updateBook(Long id, BookRequest request);

    void deleteBook(Long id);
}
