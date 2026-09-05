package ru.yandex.practicum.filmorate.dto.reviews;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateReviewRequest {
    @NotNull(message = "Id обязателен при обновлении.")
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Long useful;
}
