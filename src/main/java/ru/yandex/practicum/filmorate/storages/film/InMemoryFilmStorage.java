package ru.yandex.practicum.filmorate.storages.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film addNewFilm(Film film) {
        Film newFilm = Film.builder()
                .id(getNextFilmId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .build();

        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film updateFilmInfo(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            Film updateFilm = oldFilm.mergeWith(newFilm);

            films.put(newFilm.getId(), updateFilm);

            return updateFilm;
        }

        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public void save(Film film) {
        films.put(film.getId(), film);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextFilmId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }

    //вспомогательный метод для тестов
    @Override
    public void resetFilms() {
        films.clear();
    }
}
