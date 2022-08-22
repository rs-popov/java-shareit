package ru.practicum.shareit.user;

import lombok.Data;

import javax.validation.constraints.Email;

/**
 * // TODO .
 *
 * id — уникальный идентификатор пользователя;
 * name — имя или логин пользователя;
 * email — адрес электронной почты (учтите, что два пользователя не могут
 * иметь одинаковый адрес электронной почты).
 */

@Data
public class User {

    long id;

    String name;

    @Email
    String email;
}
