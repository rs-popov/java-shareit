package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.Value;
import lombok.experimental.NonFinal;
import ru.practicum.shareit.booking.StatusType;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * // TODO .
 * id — уникальный идентификатор вещи;
 * name — краткое название;
 * description — развёрнутое описание;
 * available — статус о том, доступна или нет вещь для аренды;
 * owner — владелец вещи;
 * request — если вещь была создана по запросу другого пользователя, то в этом
 * поле будет храниться ссылка на соответствующий запрос.
 *
 */

@Value
public class Item {
    @NonFinal long id;
    String name;
    String description;
    boolean available;
    User owner;
    ItemRequest request;
}
