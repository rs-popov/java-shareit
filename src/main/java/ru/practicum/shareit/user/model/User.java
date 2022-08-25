package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

/**
 * id — уникальный идентификатор пользователя;
 * name — имя или логин пользователя;
 * email — адрес электронной почты (учтите, что два пользователя не могут
 * иметь одинаковый адрес электронной почты).
 */

@Data
@Builder
public class User {
    private long id;
    private String name;
    @Email
    private String email;
}
