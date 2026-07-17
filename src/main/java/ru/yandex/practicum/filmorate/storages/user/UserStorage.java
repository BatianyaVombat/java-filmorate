package ru.yandex.practicum.filmorate.storages.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User updateUserInfo(User newUser);

    Collection<User> getAll();

    void resetUsers();

    Optional<User> getUserById(Long id);

    void saveUser(User user);
}
