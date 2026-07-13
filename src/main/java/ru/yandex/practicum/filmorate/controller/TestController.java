package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final FilmController filmController;
    private final UserController userController;

    // Конструктор для внедрения зависимостей
    public TestController(FilmController filmController, UserController userController) {
        this.filmController = filmController;
        this.userController = userController;
    }

    @DeleteMapping("/reset")
    public void reset() {
        filmController.resetFilms();
        userController.resetUsers();
    }
}