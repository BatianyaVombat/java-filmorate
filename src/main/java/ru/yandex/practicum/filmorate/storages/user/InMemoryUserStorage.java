package ru.yandex.practicum.filmorate.storages.user;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User createUser(User user) {
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

    @Override
    public User updateUserInfo(User newUser) {
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

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void saveUser(User user) {
        users.put(user.getId(), user);
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
    @Override
    public void resetUsers() {
        users.clear();
    }
}
