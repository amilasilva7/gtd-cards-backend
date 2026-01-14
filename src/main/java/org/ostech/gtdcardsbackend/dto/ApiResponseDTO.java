package org.ostech.gtdcardsbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {

    private String status;

    private String message;

    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    private String requestId;

    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return (ApiResponseDTO<T>) ApiResponseDTO.builder()
            .status("SUCCESS")
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponseDTO<T> error(String message) {
        return (ApiResponseDTO<T>) ApiResponseDTO.builder()
            .status("ERROR")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
