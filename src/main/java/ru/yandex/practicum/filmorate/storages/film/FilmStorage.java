package ru.yandex.practicum.filmorate.storages.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Film addNewFilm(Film film);

    Film updateFilmInfo(Film newFilm);

    Optional<Film> getFilmById(Long id);

    void save(Film film);

    void resetFilms();
}
