package ru.yandex.practicum.filmorate.storages.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Film addNewFilm(Film film);

    Film updateFilmInfo(Film newFilm);

    void resetFilms();
}
