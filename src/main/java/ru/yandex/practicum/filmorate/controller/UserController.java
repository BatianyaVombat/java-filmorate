package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        if (users.containsValue(user)) {
            throw new DuplicatedDataException("Пользователь с такими данными уже существует");
        }

        User newUser = User.builder()
                .id(getNextUserId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        newUser.normalize();
        users.put(newUser.getId(), newUser);

        return newUser;
    }

    @PutMapping
    public User updateUserInfo(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            User updatedUser = oldUser.mergeWith(newUser);

            users.put(newUser.getId(), updatedUser);

            return updatedUser;
        }

        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextUserId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }

    //вспомогательный метод для тестов
    public void resetUsers() {
        users.clear();
    }
}
