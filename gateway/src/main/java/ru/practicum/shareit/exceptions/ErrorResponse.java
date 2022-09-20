package ru.practicum.shareit.exceptions;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ErrorResponse {
    @NonNull
    private String error;
    private String description;
}