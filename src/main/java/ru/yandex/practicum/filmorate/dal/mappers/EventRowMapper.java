package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getLong("event_id"))
                // created_at хранится как TIMESTAMP, в JSON отдаём как unix-время в миллисекундах
                .timestamp(resultSet.getTimestamp("created_at").getTime())
                .userId(resultSet.getLong("user_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(EventOperation.valueOf(resultSet.getString("operation")))
                .entityId(resultSet.getLong("entity_id"))
                .build();
    }
}