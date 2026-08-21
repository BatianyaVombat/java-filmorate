package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest {
    @NotNull(message = "Id обязателен при обновлении")
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
