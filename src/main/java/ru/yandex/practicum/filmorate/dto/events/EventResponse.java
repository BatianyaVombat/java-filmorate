package ru.yandex.practicum.filmorate.dto.events;

import lombok.Builder;
import lombok.Getter;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;

@Getter
@Builder
public class EventResponse {
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private EventOperation operation;
    private Long eventId;
    private Long entityId;
}
