package ru.yandex.practicum.filmorate.dto.directors;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewDirectorRequest {
    @NotBlank
    private String name;
}
