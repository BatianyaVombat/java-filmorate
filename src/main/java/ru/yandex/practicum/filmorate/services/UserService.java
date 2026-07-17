package ru.yandex.practicum.filmorate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storages.user.UserStorage;

import java.util.Collection;


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

    /*public void addToFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователи уже являются друзьями");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.saveUser(user);
        userStorage.saveUser(friend);
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (!user.getFriends().contains(friendId)) {
            throw new ValidationException("Пользователи не являются друзьями");
        }

        user.getFriends().remove(friendId);
        user.getFriends().remove(userId);

        userStorage.saveUser(user);
        userStorage.saveUser(friend);
    }

    public Collection<User> getAllFriends(Long userId) {
        User user = getUserOrThrow(userId);

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<User> getMutualFriends(Long userId, Long otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());

        return commonIds.stream()
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }*/

    //вспомогательный метод для получения user
    private User getUserOrThrow(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    //метод для тестов
    public void resetUser() {
        userStorage.resetUsers();
    }
}
