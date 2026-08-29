package ru.yandex.practicum.filmorate.model;

import lombok.*;

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
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Long likeCount; //количество лайков

    @Builder.Default //сохраняет значение по умолчанию
    private Set<Long> genresIds = new HashSet<>(); //жанры
    private Long mpaId; //рейтинг МРА
    private Long directorId;
}
