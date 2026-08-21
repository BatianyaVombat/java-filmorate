package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenresRowMapper implements RowMapper<GenreResponse> {
    @Override
    public GenreResponse mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        return new GenreResponse(id, name);
    }
}
