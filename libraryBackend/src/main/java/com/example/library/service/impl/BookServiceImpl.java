package com.example.library.service.impl;

import com.example.library.dto.BookDto;
import com.example.library.entity.impl.Book;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService;
import org.springframework.beans.BeanUtils; // Simple property copying
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional // Ensures all methods run within a transaction
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Basic DTO to Entity and Entity to DTO mappers
    // In a larger app, consider using a mapping library like MapStruct
    private BookDto convertToDto(Book book) {
        BookDto bookDto = new BookDto();
        BeanUtils.copyProperties(book, bookDto);
        return bookDto;
    }

    private Book convertToEntity(BookDto bookDto) {
        Book book = new Book();
        BeanUtils.copyProperties(bookDto, book);
        return book;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + bookDto.getIsbn() + " already exists.");
        }
        Book book = convertToEntity(bookDto);
        book.setId(null); // Ensure it's a new entity
        return convertToDto(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookDto> getBookById(Long id) {
        return bookRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookDto> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookDto> updateBook(Long id, BookDto bookDto) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    // Check if ISBN is being changed and if the new ISBN already exists for another book
                    if (bookDto.getIsbn() != null && !bookDto.getIsbn().equals(existingBook.getIsbn())) {
                        if (bookRepository.findByIsbn(bookDto.getIsbn()).filter(b -> !b.getId().equals(id)).isPresent()) {
                            throw new IllegalArgumentException("Another book with ISBN " + bookDto.getIsbn() + " already exists.");
                        }
                    }
                    BeanUtils.copyProperties(bookDto, existingBook, "id"); // Don't copy id
                    return convertToDto(bookRepository.save(existingBook));
                });
    }

    @Override
    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> searchBooks(String title, String author) {
        if (title != null && !title.isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        if (author != null && !author.isEmpty()) {
            return bookRepository.findByAuthorContainingIgnoreCase(author).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return getAllBooks(); // Or return empty list if no criteria provided
    }
}