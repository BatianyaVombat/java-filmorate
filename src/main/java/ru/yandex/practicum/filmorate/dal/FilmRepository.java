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
                    .directorId(oldFilm.getDirectorId())
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
                .directorId(film.getDirectorId())
                .build();

        return Optional.of(foundFilm);
    }

    public Film saveFilm(Film film) {
        String sqlFilm = """
                       INSERT INTO Films (name, description, releaseDate, duration, rating_id, director_id)
                       VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sqlFilm, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaId(), film.getDirectorId());

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
                    UPDATE Films SET name = ?, description = ?, releaseDate = ?,
                    duration = ?, rating_id = ?, director_id = ?
                    WHERE id = ?
                """;
        update(sqlUpd, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpaId(), film.getDirectorId(), film.getId());

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

        return mapRowsToFilms(rows);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlCommon = """
                SELECT f.id, COUNT(fl_all.user_id) AS like_count
                                    FROM Films f
                                    JOIN Film_Likes fl_user ON f.id = fl_user.film_id AND fl_user.user_id = ?
                                    JOIN Film_Likes fl_friend ON f.id = fl_friend.film_id AND fl_friend.user_id = ?
                                    LEFT JOIN Film_Likes fl_all ON f.id = fl_all.film_id
                                    GROUP BY f.id
                                    ORDER BY like_count DESC
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(sqlCommon, userId, friendId);

        return mapRowsToFilms(rows);
    }

    //Вынес общий метод, который считает лайки по всем фильмам
    //Один метод подходит к 2-ум методам (getCommonFilms и getPopularFilms)
    private List<Film> mapRowsToFilms(List<Map<String, Object>> rows) {
        List<Film> finalList = new ArrayList<>();

        rows.forEach(row -> {
            Number idNumber = (Number) row.get("id");
            Long filmId = idNumber.longValue();

            Number likeNumber = (Number) row.get("like_count");
            Long likeCount = likeNumber.longValue();

            Film fullFilm = findById(filmId).orElseThrow(
                    () -> new InternalException("Фильм не найден"));

            Film filmWithLike = rebuildWithLikeCount(fullFilm, likeCount);
            finalList.add(filmWithLike);
        });

        return finalList;
    }

    public List<Film> getFilmsByDirectorSorted(String sortBy, Long directorId) {
        List<Film> rawList;

        if (sortBy.equals("year")) {
            String sqlYearSort = """
                              SELECT *
                              FROM Films
                              WHERE director_id = ?
                              ORDER BY releaseDate ASC
                    """;

            rawList = findMany(sqlYearSort, directorId);
            return rawList;
        } else if (sortBy.equals("likes")) {
            String sqlLikeCount = """
                            SELECT f.id, COUNT(fl.user_id) AS like_count
                            FROM Films f
                            LEFT JOIN Film_Likes fl ON f.id = fl.film_id
                            WHERE f.director_id = ?
                            GROUP BY f.id
                            ORDER BY like_count DESC
                    """;

            List<Map<String, Object>> rows = jdbc.queryForList(sqlLikeCount, directorId);
            List<Film> finalList = new ArrayList<>();

            rows.forEach(row -> {
                Number idNumber = (Number) row.get("id");
                Long filmId = idNumber.longValue();

                Number likeNumber = (Number) row.get("like_count");
                Long likeCount = likeNumber.longValue();

                Film fullFilm = findById(filmId).orElseThrow(
                        () -> new InternalException("Фильм не найден"));

                Film filmWithLike = rebuildWithLikeCount(fullFilm, likeCount);
                finalList.add(filmWithLike);
            });

            return finalList;
        } else {
            return new ArrayList<>();
        }
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
    
    //вспомогательный метод для пересборки фильма с жанрами
    private Film rebuildWithLikeCount(Film film, Long likeCount) {
        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpaId(film.getMpaId())
                .genresIds(film.getGenresIds())
                .directorId(film.getDirectorId())
                .likeCount(likeCount)
                .build();
    }
}
