package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class EventRepository extends BaseRepository<Event>{
    private static final String INSERT_EVENT_QUERY = """
            INSERT INTO Events (created_at, user_id, event_type, operation, entity_id)
                VALUES (?, ?, ?, ?, ?)
            """;

    private static final String FIND_FEED_QUERY = """
                SELECT * FROM Events
                WHERE user_id = ?
                ORDER BY created_at ASC
            """;

    public EventRepository(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper);
    }

    public void addEvent(Event event) {
        insert(INSERT_EVENT_QUERY,
                new Timestamp(event.getTimestamp()),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
    }

    public List<Event> getFeed(Long userId) {
        return findMany(FIND_FEED_QUERY, userId);
    }
}
