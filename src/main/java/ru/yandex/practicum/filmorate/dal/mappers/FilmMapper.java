package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.directors.DirectorResponse;
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


@UtilityClass
public class FilmMapper {
    public Film toEntity(NewFilmRequest request) {
        Set<Long> genreIds = request.getGenres() != null
                ? request.getGenres().stream()
                .map(GenreIdRequest::getId)
                .collect(Collectors.toSet())
                : new HashSet<>();

        Long directorId = (request.getDirectors() != null && !request.getDirectors().isEmpty())
                ? request.getDirectors().getFirst().getId()
                : null;

        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .mpaId(request.getMpa().getId())
                .genresIds(genreIds)
                .directorId(directorId)
                .build();
    }

    public FilmResponse toResponse(Film film, MpaResponse mpa, List<GenreResponse> genres, List<DirectorResponse> directors) {
        return FilmResponse.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .likeCount(film.getLikeCount())
                .mpa(mpa)
                .genres(genres)
                .directors(directors)
                .build();
    }
}
