package org.ostech.gtdcardsbackend.exception;

import org.ostech.gtdcardsbackend.dto.ErrorResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleCardAlreadyExists(
        CardAlreadyExistsException ex,
        WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.CONFLICT.value())
            .error("CARD_ALREADY_EXISTS")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCardNotFound(
        CardNotFoundException ex,
        WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("CARD_NOT_FOUND")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CardServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleCardServiceException(
        CardServiceException ex,
        WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("CARD_SERVICE_ERROR")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.CONFLICT.value())
            .error("DATA_INTEGRITY_VIOLATION")
            .message("Database constraint violation: Resource already exists")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request) {
        List<ErrorResponseDTO.ValidationError> validationErrors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            validationErrors.add(new ErrorResponseDTO.ValidationError(
                error.getField(),
                error.getDefaultMessage()
            ))
        );

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_FAILED")
            .message("Invalid input data")
            .path(request.getDescription(false).replace("uri=", ""))
            .validationErrors(validationErrors)
            .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
        Exception ex,
        WebRequest request) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
