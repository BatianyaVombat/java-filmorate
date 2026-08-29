package ru.yandex.practicum.filmorate.dto.directors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDirectorRequest {
    @NotNull
    private Long id;

    @NotBlank
    private String name;
}
