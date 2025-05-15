package com.example.library.service;

import com.example.library.dto.BookDto;
import java.util.List;
import java.util.Optional;

public interface BookService {
    BookDto createBook(BookDto bookDto);
    Optional<BookDto> getBookById(Long id);
    Optional<BookDto> getBookByIsbn(String isbn);
    List<BookDto> getAllBooks();
    Optional<BookDto> updateBook(Long id, BookDto bookDto);
    boolean deleteBook(Long id);
    List<BookDto> searchBooks(String title, String author);
}