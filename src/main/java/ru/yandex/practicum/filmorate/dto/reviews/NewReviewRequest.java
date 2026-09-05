package ru.yandex.practicum.filmorate.dto.reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewReviewRequest {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Текст отзыва не может быть пустым.")
    private String content;

    @NotNull(message = "Должен быть указан тип отзыва.")
    private Boolean isPositive;

    @NotNull(message = "Должен быть указан пользователь.")
    private Long userId;

    @NotNull(message = "Должен быть указан фильм.")
    private Long filmId;
}
