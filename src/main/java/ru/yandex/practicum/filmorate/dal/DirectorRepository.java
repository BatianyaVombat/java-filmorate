package ru.yandex.practicum.filmorate.dal;

import org.apache.logging.log4j.util.InternalException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

@Repository
public class DirectorRepository extends BaseRepository<Director> {

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public List<Director> findAll() {
        String sqlFindDirectors = """
                            SELECT *
                            FROM Directors
                """;
        return findMany(sqlFindDirectors);
    }

    public Optional<Director> findById(Long id) {
        String sqlFindOne = """
                        SELECT *
                        FROM Directors
                        WHERE id = ?
                """;
        return findOne(sqlFindOne, id);
    }

    public Optional<Director> findByName(String name) {
        String findByName = """
                        SELECT *
                        FROM Directors
                        WHERE name = ?
                """;
        return findOne(findByName, name);
    }

    public Director createDirector(Director director) {
        String sqlDirector = """
                    INSERT INTO Directors (name)
                    VALUES (?)
                """;
        jdbc.update(sqlDirector, director.getName());

        Long newId = jdbc.queryForObject("SELECT id FROM Directors WHERE name = ?",
                Long.class, director.getName());

        return findById(newId).orElseThrow(
                () -> new InternalException("Не удалось найти режиссёра после сохранения")
        );
    }

    public void updateDirector(Director director) {
        String sqlUpd = """
                    UPDATE Directors SET name = ?
                    WHERE id = ?
                """;
        update(sqlUpd, director.getName(), director.getId());
    }

    public void deleteDirector(Long id) {
        String sqlDel = """
                    DELETE FROM Directors
                    WHERE id = ?
                """;
        update(sqlDel, id);
    }
}
