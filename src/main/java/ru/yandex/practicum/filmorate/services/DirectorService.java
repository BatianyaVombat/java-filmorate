package ru.yandex.practicum.filmorate.services;

import jakarta.validation.ValidationException;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.dto.directors.DirectorResponse;
import ru.yandex.practicum.filmorate.dto.directors.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.directors.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectorService {
    private final DirectorRepository directorRepository;
    private final FilmRepository filmRepository;

    @Autowired
    public DirectorService(DirectorRepository directorRepository, FilmRepository filmRepository) {
        this.directorRepository = directorRepository;
        this.filmRepository = filmRepository;
    }

    public List<DirectorResponse> getAllDirectors() {
        return directorRepository.findAll().stream()
                .map(DirectorMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DirectorResponse createNewDirector(NewDirectorRequest request) {
        if (directorRepository.findByName(request.getName()).isPresent()) {
            throw new ValidationException("Такой режиссёр уже добавлен");
        }

        Director newDirector = directorRepository.createDirector(DirectorMapper.toEntity(request));

        return DirectorMapper.toResponse(newDirector);
    }

    public DirectorResponse updateDirector(Long id, UpdateDirectorRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidationException("Id режиссёра обязателен");
        }

        Director oldDirector = getDirectorOrThrow(id);
        String finalName = request.getName() != null ? request.getName() : oldDirector.getName();

        if (!finalName.equals(oldDirector.getName()) && directorRepository.findByName(finalName).isPresent()) {
            throw new ValidationException("Такой режиссёр уже добавлен");
        }

        Director updateDirector = Director.builder()
                .id(oldDirector.getId())
                .name(finalName)
                .build();

        directorRepository.updateDirector(updateDirector);

        Director savedDirector = directorRepository.findById(updateDirector.getId())
                .orElseThrow(() -> new InternalException("Не удалось найти режиссёра после обновления"));

        return DirectorMapper.toResponse(savedDirector);
    }

    public void deleteDirector(Long id) {
        getDirectorOrThrow(id);
        filmRepository.updateDirectorToNull(id);
        directorRepository.deleteDirector(id);
    }

    private Director getDirectorOrThrow(Long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
    }

    public DirectorResponse getDirectorById(Long id) {
        Director director = getDirectorOrThrow(id);
        return DirectorMapper.toResponse(director);
    }
}
