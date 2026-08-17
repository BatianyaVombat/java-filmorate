package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenresRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmRepository.class, FilmRowMapper.class, GenreRepository.class, MpaRepository.class, GenresRowMapper.class,
        MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmRepositoryTest {
    private final FilmRepository filmRepository;
    private final JdbcTemplate jdbc;

    private Long insertFilm(String name, String description, String releaseDate, long duration, long ratingId) {
        jdbc.update(
                "INSERT INTO Films (name, description, releaseDate, duration, rating_id) VALUES (?, ?, ?, ?, ?)",
                name, description, java.sql.Date.valueOf(releaseDate), duration, ratingId
        );
        return jdbc.queryForObject("SELECT id FROM Films WHERE name = ?", Long.class, name);
    }

    //вставка жанра в связующую таблицу
    private void insertFilmGenre(long filmId, long genreId) {
        jdbc.update("INSERT INTO Film_Genres (film_id, genre_id) VALUES (?, ?)", filmId, genreId);
    }

    @Test
    void findByIdShouldReturnFilmWithGenres() {
        Long filmId = insertFilm("Film", "Desc", "2000-01-01", 120, 1);
        insertFilmGenre(filmId, 1);
        insertFilmGenre(filmId, 2);

        Optional<Film> filmOpt = filmRepository.findById(filmId);

        assertThat(filmOpt).isPresent();
        assertThat(filmOpt.get().getGenresIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void findAllShouldReturnAllFilms() {
        insertFilm("Film1", "Desc1", "2000-01-01", 120, 1);
        insertFilm("Film2", "Desc2", "2001-02-02", 130, 2);

        List<Film> films = filmRepository.findAll();
        assertThat(films).hasSize(2);
    }

    @Test
    void saveFilmShouldInsertFilmAndGenres() {
        Film film = Film.builder()
                .name("New Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2020, 5, 5))
                .duration(150L)
                .mpaId(1L)
                .genresIds(Set.of(1L, 2L))
                .build();

        Film saved = filmRepository.saveFilm(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(filmRepository.findById(saved.getId()).get().getGenresIds()).isNotEmpty();
    }

    @Test
    void updateFilmShouldChangeName() {
        Long id = insertFilm("Old", "Desc", "2000-01-01", 120, 1);
        Film film = filmRepository.findById(id).get();

        Film updated = Film.builder()
                .id(film.getId())
                .name("New Name")
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpaId(film.getMpaId())
                .genresIds(film.getGenresIds())
                .build();

        filmRepository.updateFilm(updated);

        assertThat(filmRepository.findById(id).get().getName()).isEqualTo("New Name");
    }

    @Test
    void likeLifecycleShouldWork() {
        Long filmId = insertFilm("Film", "Desc", "2000-01-01", 120, 1);

        jdbc.update("INSERT INTO Users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "u@example.com", "login", "User", java.sql.Date.valueOf("1990-01-01"));
        Long userId = jdbc.queryForObject("SELECT id FROM Users WHERE email = ?", Long.class,
                "u@example.com");

        filmRepository.addLike(filmId, userId);

        assertThat(filmRepository.getPopularFilms(10L)).isNotEmpty();

        filmRepository.removeLike(filmId, userId);
    }

    @Test
    void getPopularFilmsShouldReturnSortedByLikes() {
        Long film1 = insertFilm("Film2", "D", "2001-01-01", 110, 1);

        jdbc.update(
                "INSERT INTO Users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user@example.com", "userLogin", "Test User", java.sql.Date.valueOf("1990-01-01")
        );
        Long userId = jdbc.queryForObject(
                "SELECT id FROM Users WHERE email = ?", Long.class, "user@example.com"
        );

        filmRepository.addLike(film1, userId);

        List<Film> popular = filmRepository.getPopularFilms(10L);
        assertThat(popular).isNotEmpty();
        assertThat(popular.getFirst().getId()).isEqualTo(film1);
    }
}
