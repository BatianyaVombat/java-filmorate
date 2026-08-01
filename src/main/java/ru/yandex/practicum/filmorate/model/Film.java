package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.annotation.After;
import ru.yandex.practicum.filmorate.enums.Genres;
import ru.yandex.practicum.filmorate.enums.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */

@Data
@Builder
@EqualsAndHashCode(of = {"releaseDate"})
public class Film {
    private Long id;
    //не null или хотя-бы 1 непробельный символ
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @After(value = "1895-12-28")
    private LocalDate releaseDate;

    @Positive(message = "Длительность фильма должна быть больше нуля")
    private long duration;

    private final Set<Long> likes = new HashSet<>(); //лайки

    private final Set<Genres> genres; //жанры

    private final MpaRating mpa; //рейтинг МРА

    //метод проверяет данные Film при обновлении и не допускает внезапные null если часть данных не обновляется
    public Film mergeWith(Film newData) {
        return Film.builder()
                .id(newData.getId() != null ? newData.getId() : this.id)
                .name(newData.getName() != null ? newData.getName() : this.name)
                .description(newData.getDescription() != null ? newData.getDescription() : this.description)
                .releaseDate(newData.getReleaseDate() != null ? newData.getReleaseDate() : this.releaseDate)
                .duration(newData.getDuration())
                .genres(newData.getGenres())
                .mpa(newData.getMpa())
                .build();
    }
}
