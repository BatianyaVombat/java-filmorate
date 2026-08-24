package ru.yandex.practicum.filmorate.dal;

import org.apache.logging.log4j.util.InternalException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public List<Film> findAll() {
        String sqlSelect = """
                        SELECT *
                        FROM Films
                """;
        List<Film> rawFilms = findMany(sqlSelect);
        List<Film> films = new ArrayList<>();

        rawFilms.forEach(oldFilm -> {
            String sqlGenres = """
                            SELECT genre_id
                            FROM Film_Genres
                            WHERE film_id = ?
                    """;
            List<Long> genresIds = jdbc.queryForList(sqlGenres, Long.class, oldFilm.getId());

            Film reassFilm = Film.builder()
                    .id(oldFilm.getId())
                    .name(oldFilm.getName())
                    .description(oldFilm.getDescription())
                    .releaseDate(oldFilm.getReleaseDate())
                    .duration(oldFilm.getDuration())
                    .genresIds(new HashSet<>(genresIds))
                    .mpaId(oldFilm.getMpaId())
                    .build();

            films.add(reassFilm);
        });

        return films;
    }

    public Optional<Film> findById(Long id) {
        //фильм без жанров
        Film film = findOne("SELECT * FROM Films WHERE id = ?", id).orElse(null);

        if (film == null) {
            return Optional.empty();
        }

        //берём все id-шники жанров
        String sqlGenres = """
                        SELECT genre_id
                        FROM Film_Genres
                        WHERE film_id = ?
                """;
        List<Long> genresIds = jdbc.queryForList(sqlGenres, Long.class, id);

        Film foundFilm = Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .genresIds(new HashSet<>(genresIds))
                .mpaId(film.getMpaId())
                .build();

        return Optional.of(foundFilm);
    }

    public Film saveFilm(Film film) {
        String sqlFilm = """
                       INSERT INTO Films (name, description, releaseDate, duration, rating_id)
                       VALUES (?, ?, ?, ?, ?)
                """;
        jdbc.update(sqlFilm, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaId());

        Long newId = jdbc.queryForObject("SELECT MAX(id) FROM Films", Long.class);

        if (newId == null) {
            throw new InternalException("Не удалось сохранить фильм");
        }

        updateGenres(newId, film.getGenresIds());

        return findById(newId).orElseThrow(
                () -> new InternalException("Не удалось найти фильм после сохранения")
        );
    }

    public void updateFilm(Film film) {
        String sqlUpd = """
                    UPDATE Films SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ?
                    WHERE id = ?
                """;
        update(sqlUpd, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpaId(), film.getId());

        updateGenres(film.getId(), film.getGenresIds());
    }

    public void addLike(Long filmId, Long userId) {
        String sqlLike = """
                      INSERT INTO Film_Likes (film_id, user_id)
                      VALUES (?, ?)
                """;
        execute(sqlLike, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        String sqlRemove = """
                        DELETE FROM Film_Likes
                        WHERE film_id = ? AND user_id = ?
                """;
        execute(sqlRemove, filmId, userId);
    }

    public List<Film> getPopularFilms(Long count) {
        String sqlPopular = """
                        SELECT f.id, COUNT(fl.user_id) AS like_count
                        FROM Films f
                        LEFT JOIN Film_Likes fl ON f.id = fl.film_id
                        GROUP BY f.id
                        ORDER BY like_count DESC
                        LIMIT ?
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sqlPopular, count);
        List<Film> finalList = new ArrayList<>();

        rows.forEach(row -> {
            Number idNumber = (Number) row.get("id");
            Long filmId = idNumber.longValue();

            Number likeNumber = (Number) row.get("like_count");
            Long likeCount = likeNumber.longValue();

            Film fullFilm = findById(filmId).orElseThrow(
                    () -> new InternalException("Фильм не найден"));

            Film filmWithLike = Film.builder()
                    .id(fullFilm.getId())
                    .name(fullFilm.getName())
                    .description(fullFilm.getDescription())
                    .releaseDate(fullFilm.getReleaseDate())
                    .duration(fullFilm.getDuration())
                    .mpaId(fullFilm.getMpaId())
                    .genresIds(fullFilm.getGenresIds())
                    .likeCount(likeCount)
                    .build();

            finalList.add(filmWithLike);
        });

        return finalList;
    }

    //вспомогательный метод для апдейта жанров
    private void updateGenres(Long filmId, Set<Long> genres) {
        if (genres == null) {
            genres = new HashSet<>();
        }

        String sqlDel = """
                    DELETE FROM Film_Genres
                    WHERE film_id = ?
                """;
        execute(sqlDel, filmId);

        genres.forEach(genreId -> {
            String sqlInsert = """
                            INSERT INTO Film_Genres (film_id, genre_id)
                            VALUES (?, ?)
                    """;
            execute(sqlInsert, filmId, genreId);
        });
    }

    //метод, который возвращает все лайки
    public Map<Long, Set<Long>> getAllUserLikes() {
        String sql = "SELECT user_id, film_id FROM Film_Likes";
        List<Map<String, Object>> rows = jdbc.queryForList(sql);

        Map<Long, Set<Long>> likesByUser = new HashMap<>();
        rows.forEach(row -> {
            Long userId = ((Number) row.get("user_id")).longValue();
            Long filmId = ((Number) row.get("film_id")).longValue();
            likesByUser.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        return likesByUser;
    }
}
