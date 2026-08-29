package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.directors.DirectorResponse;
import ru.yandex.practicum.filmorate.dto.directors.NewDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@UtilityClass
public class DirectorMapper {

    public Director toEntity(NewDirectorRequest request) {
        return Director.builder()
                .name(request.getName())
                .build();
    }

    public DirectorResponse toResponse(Director director) {
        return DirectorResponse.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }
}
