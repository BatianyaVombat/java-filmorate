package ru.yandex.practicum.filmorate.dto.film;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
public class FilmResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Long likeCount;
    private List<GenreResponse> genres;
    private MpaResponse mpa;
}
