package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenresRowMapper;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;

import java.util.*;

@Repository
public class GenreRepository extends BaseRepository<GenreResponse> {

    public GenreRepository(JdbcTemplate jdbc, RowMapper<GenreResponse> mapper) {
        super(jdbc, mapper);
    }

    public List<GenreResponse> findAll() {
        String sqlFindGenres = "SELECT * FROM Genres ORDER BY id";

        return findMany(sqlFindGenres);
    }

    public Optional<GenreResponse> findById(Long id) {
        String sqlFindOne = "SELECT * FROM Genres WHERE id = ?";

        return findOne(sqlFindOne, id);
    }

    public List<GenreResponse> findAllById(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM Genres WHERE id IN (" + placeholders + ")";

        return jdbc.query(sql, new GenresRowMapper(), ids.toArray());
    }
}
