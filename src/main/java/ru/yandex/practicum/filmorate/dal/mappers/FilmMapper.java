package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.film.FilmResponse;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.genres.GenreIdRequest;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class FilmMapper {
    public static Film toEntity(NewFilmRequest request) {
        Set<Long> genreIds = request.getGenres() != null
                ? request.getGenres().stream()
                .map(GenreIdRequest::getId)
                .collect(Collectors.toSet())
                : new HashSet<>();

        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .mpaId(request.getMpa().getId())
                .genresIds(genreIds)
                .build();
    }

    public static FilmResponse toResponse(Film film, MpaResponse mpa, List<GenreResponse> genres) {
        return FilmResponse.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .likeCount(film.getLikeCount())
                .mpa(mpa)
                .genres(genres)
                .build();
    }
}
