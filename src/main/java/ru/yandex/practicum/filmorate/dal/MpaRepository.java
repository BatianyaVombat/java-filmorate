package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;

import java.util.*;

@Repository
public class MpaRepository extends BaseRepository<MpaResponse> {

    public MpaRepository(JdbcTemplate jdbc, RowMapper<MpaResponse> mapper) {
        super(jdbc, mapper);
    }

    public List<MpaResponse> findAll() {
        String sqlFindAll = """
                        SELECT *
                        FROM MPA_Ratings
                        ORDER BY id
                """;

        return findMany(sqlFindAll);
    }

    public Optional<MpaResponse> findById(Long id) {
        String sqlFindMpa = """
                        SELECT *
                        FROM MPA_Ratings
                        WHERE id = ?
                """;

        return findOne(sqlFindMpa, id);
    }
}
