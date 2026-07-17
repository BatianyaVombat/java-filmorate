package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAll() {
        return userService.getAll();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUserInfo(@Valid @RequestBody User newUser) {
        return userService.updateUserInfo(newUser);
    }

    /*@PutMapping("/{id}/friends/{friendId}")
    public void addToFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        userService.addToFriends(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        userService.deleteFromFriends(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public void getAllFriends(@PathVariable("id") long userId) {
        userService.getAllFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable("id") long userId, @PathVariable("otherId") Long otherId) {
        return userService.getMutualFriends(userId, otherId);
    }*/
}
