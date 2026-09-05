package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.directors.DirectorResponse;
import ru.yandex.practicum.filmorate.dto.directors.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.directors.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.services.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<DirectorResponse> getAllDirectors() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public DirectorResponse getDirectorById(@PathVariable Long id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public DirectorResponse addNewDirector(@Valid @RequestBody NewDirectorRequest request) {
        return directorService.createNewDirector(request);
    }

    @PutMapping
    public DirectorResponse updateDirectorInfo(@Valid @RequestBody UpdateDirectorRequest request) {
        return directorService.updateDirector(request.getId(), request);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Long id) {
        directorService.deleteDirector(id);
    }
}
