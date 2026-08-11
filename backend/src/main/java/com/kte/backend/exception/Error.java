package com.kte.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Error {
    private String error;
    private String path;           // ex: /api/v1/product/5
    private int status;            // ex: 404
    private LocalDateTime timestamp; // ex: 2026-05-02T14:32:10
}
