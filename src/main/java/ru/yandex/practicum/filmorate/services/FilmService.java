package ru.yandex.practicum.filmorate.services;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storages.film.FilmStorage;

import java.util.Collection;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addNewFilm(Film film) {
        return filmStorage.addNewFilm(film);
    }

    public Film updateFilmInfo(Film newFilm) {
        return filmStorage.updateFilmInfo(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        userService.getUserById(userId);

        Film film = getFilmOrThrow(filmId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.save(film);
    }

    public void removeLike(Long filmId, Long userId) {
        userService.getUserById(userId);
        Film film = getFilmOrThrow(filmId);

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);
        filmStorage.save(film);
    }

    public Collection<Film> getPopularFilmList(Long count) {
        Collection<Film> allFilms = filmStorage.getAllFilms();

        return allFilms.stream()
                .filter(film -> !film.getLikes().isEmpty())
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .toList();
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    //оставил для тестов
    public void resetFilms() {
        filmStorage.resetFilms();
    }
}
