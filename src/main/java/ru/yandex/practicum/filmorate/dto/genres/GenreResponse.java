package ru.yandex.practicum.filmorate.dto.genres;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenreResponse {
    private Long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String name;

    public GenreResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
