package ru.practicum.shareit.item.comment.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Value
@Builder
public class CommentDto {
    Long id;

    @Size(min = 1)
    String text;

    String authorName;

    LocalDateTime created;
}
