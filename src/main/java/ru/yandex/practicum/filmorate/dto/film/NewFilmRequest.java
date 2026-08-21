package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.filmorate.annotation.After;
import ru.yandex.practicum.filmorate.dto.genres.GenreIdRequest;
import ru.yandex.practicum.filmorate.dto.mpa.MpaIdRequest;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NewFilmRequest {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    //не null или хотя-бы 1 непробельный символ
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание фильма не может быть пустым")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull
    @After(value = "1895-12-28")
    private LocalDate releaseDate;

    @NotNull(message = "Длительность фильма обязательна")
    @Positive(message = "Длительность фильма должна быть больше нуля")
    private Long duration;

    private List<GenreIdRequest> genres;

    @NotNull
    private MpaIdRequest mpa;
}
