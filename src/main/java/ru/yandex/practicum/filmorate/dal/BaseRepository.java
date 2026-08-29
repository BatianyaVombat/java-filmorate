package ru.yandex.practicum.filmorate.dal;

import com.sun.jdi.InternalException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected boolean delete(String query, Object... params) {
        int rowsDelete = jdbc.update(query, params);
        return rowsDelete > 0;
    }

    protected Long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    query,
                    Statement.RETURN_GENERATED_KEYS
            );
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);


        Long id = keyHolder.getKeyAs(Long.class);
        if (id == null) {
            throw new InternalException("Не удалось сохранить данные");
        }

        return id;
    }

    protected void update(String query, Object... params) {
        int rowsUpdate = jdbc.update(query, params);
        if (rowsUpdate == 0) {
            throw new InternalException("Не удалось обновить данные");
        }
    }

    protected Long insertWithParams(String query, MapSqlParameterSource params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(query, params, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            throw new InternalException("Не удалось сохранить данные");
        }

        return id;
    }

    //для связующих таблиц (Friends, Film_Likes, Film_Genres)
    protected void execute(String query, Object... params) {
        jdbc.update(query, params);
    }
}
