package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmResponse;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmResponse> getAllFilms() {
        return filmService.getAllFilms();
    }

    @PostMapping
    public FilmResponse addNewFilm(@Valid @RequestBody NewFilmRequest request) {
        return filmService.addNewFilm(request);
    }

    @GetMapping("/{id}")
    public FilmResponse getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping
    public FilmResponse updateFilmInfo(@Valid @RequestBody UpdateFilmRequest request) {
        return filmService.updateFilm(request.getId(), request);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmResponse> getPopularFilmList(@RequestParam(defaultValue = "10") Long count,
                                                 @RequestParam(required = false) Long genreId,
                                                 @RequestParam(required = false) Long year) {
        return filmService.getPopularFilmsByParams(count, genreId, year);
    }

    @GetMapping("/common")
    public Collection<FilmResponse> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmResponse> sortedFilmList(@RequestParam(defaultValue = "year") String sortBy,
                                             @PathVariable Long directorId) {
        return filmService.getSortedFilms(sortBy, directorId);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable("id") Long filmId) {
        filmService.removeFilm(filmId);
    }
}
