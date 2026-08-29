package ru.yandex.practicum.filmorate.dto.directors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DirectorResponse {
    private Long id;
    private String name;
}
