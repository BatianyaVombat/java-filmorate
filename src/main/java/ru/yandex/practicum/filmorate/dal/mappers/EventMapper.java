package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.events.EventResponse;
import ru.yandex.practicum.filmorate.model.Event;

@UtilityClass
public class EventMapper {
    public EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .entityId(event.getEntityId())
                .build();
    }
}
