package ru.practicum.shareit.exceptions;

public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String s) {
        super(s);
    }
}