package ru.practicum.shareit.exceptions;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String s) {
        super(s);
    }
}