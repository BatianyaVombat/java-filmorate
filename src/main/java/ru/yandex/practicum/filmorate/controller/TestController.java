package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.services.FilmService;
import ru.yandex.practicum.filmorate.services.UserService;

@RestController
@RequestMapping("/test")
public class TestController {

    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    public TestController(UserService userService, FilmService filmService) {
        this.userService = userService;
        this.filmService = filmService;
    }

    @DeleteMapping("/reset")
    public void reset() {
        userService.resetUser();
        filmService.resetFilms();
    }
}