package ru.yandex.practicum.filmorate.services;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUserInfo(User newUser) {
        return userStorage.updateUserInfo(newUser);
    }

    public void addToFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (user.getFriendRequests().stream()
                .anyMatch(f -> f.getFriendId().equals(userId))) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        user.getFriendRequests().add(new Friendship(userId, friendId));
        friend.getFriendRequests().add(new Friendship(friendId, userId));

        userStorage.save(user);
        userStorage.save(friend);
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (user.getFriendRequests().stream()
                .noneMatch(f -> f.getFriendId().equals(friendId))) {
            return;
        }

        user.getFriendRequests().removeIf(f -> f.getFriendId().equals(friendId));
        friend.getFriendRequests().removeIf(f -> f.getFriendId().equals(userId));

        userStorage.save(user);
        userStorage.save(friend);
    }

    public Collection<User> getAllFriends(Long userId) {
        User user = getUserOrThrow(userId);

        return user.getFriendRequests().stream()
                .map(Friendship::getFriendId)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public Collection<User> getMutualFriends(Long userId, Long otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        Set<Long> userFriendIds = user.getFriendRequests().stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        Set<Long> otherFriendIds = other.getFriendRequests().stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        userFriendIds.retainAll(otherFriendIds);

        return userFriendIds.stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    //вспомогательный метод для получения user
    private User getUserOrThrow(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public void getUserById(Long id) {
        getUserOrThrow(id);
    }

    //метод для тестов
    public void resetUser() {
        userStorage.resetUsers();
    }
}
