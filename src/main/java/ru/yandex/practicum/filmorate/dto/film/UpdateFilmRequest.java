package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.filmorate.dto.genres.GenreIdRequest;
import ru.yandex.practicum.filmorate.dto.mpa.MpaIdRequest;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateFilmRequest {
    @NotNull(message = "Id обязателен при обновлении")
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private List<GenreIdRequest> genres;
    private MpaIdRequest mpa;
}
