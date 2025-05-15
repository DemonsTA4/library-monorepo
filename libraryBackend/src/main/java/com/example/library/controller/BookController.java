package com.example.library.controller;

import com.example.library.dto.BookDto;
import com.example.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid; // javax.validation for Spring Boot 2.x

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Book Management", description = "APIs for managing books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(summary = "Get all books", description = "Retrieve a list of all books, or search by title or author.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks(
            @Parameter(description = "Search by book title (case-insensitive, partial match)") @RequestParam(required = false) String title,
            @Parameter(description = "Search by book author (case-insensitive, partial match)") @RequestParam(required = false) String author) {
        if ((title != null && !title.isEmpty()) || (author != null && !author.isEmpty())) {
            return ResponseEntity.ok(bookService.searchBooks(title, author));
        }
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(summary = "Get a book by ID", description = "Retrieve a specific book by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@Parameter(description = "ID of the book to be retrieved") @PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with id: " + id));
    }

    @Operation(summary = "Get a book by ISBN", description = "Retrieve a specific book by its ISBN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookDto> getBookByIsbn(@Parameter(description = "ISBN of the book") @PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with ISBN: " + isbn));
    }


    @Operation(summary = "Create a new book", description = "Add a new book to the library.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or ISBN already exists")
    })
    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
        try {
            BookDto createdBook = bookService.createBook(bookDto);
            return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Update an existing book", description = "Update details of an existing book by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or ISBN conflict")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@Parameter(description = "ID of the book to be updated") @PathVariable Long id,
                                              @Valid @RequestBody BookDto bookDto) {
        try {
            return bookService.updateBook(id, bookDto)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with id: " + id));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a book", description = "Delete a book from the library by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@Parameter(description = "ID of the book to be deleted") @PathVariable Long id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with id: " + id);
        }
    }
}