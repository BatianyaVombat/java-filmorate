package ru.yandex.practicum.filmorate.enums;

import lombok.Getter;

@Getter
public enum Genres {
    ACTION(1, "Боевик"),
    COMEDY(2, "Комедия"),
    DRAMA(3, "Драма"),
    HORROR(4, "Ужасы"),
    ADVENTURE(5, "Приключения"),
    FANTASY(6, "Фэнтези");

    private final int id;
    private final String name;

    Genres(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
