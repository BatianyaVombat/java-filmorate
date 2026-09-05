package ru.yandex.practicum.filmorate.services;

import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.UserMapper;
import ru.yandex.practicum.filmorate.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ReviewService reviewService;
    private final EventService eventService;

    @Autowired
    public UserService(UserRepository userRepository, FilmRepository filmRepository,
                       ReviewService reviewService, EventService eventService) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.reviewService = reviewService;
        this.eventService = eventService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User addNewUser(NewUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Email уже используется");
        }

        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ValidationException("Логин уже используется");
        }

        String finalName = request.getName();
        if (finalName == null || finalName.isBlank()) {
            finalName = request.getLogin();
        }

        //собираем модель из DTO
        User newUser = UserMapper.toEntity(request);
        newUser.setName(finalName);

        return userRepository.saveUser(newUser);
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidationException("Id пользователя обязателен");
        }

        User oldUser = getUserOrThrow(id);

        String finalEmail = request.getEmail() != null ? request.getEmail() : oldUser.getEmail();
        String finalLogin = request.getLogin() != null ? request.getLogin() : oldUser.getLogin();
        String finalName = request.getName() != null && !request.getName().isBlank()
                ? request.getName() : finalLogin;
        LocalDate finalBirthday = request.getBirthday() != null ? request.getBirthday() : oldUser.getBirthday();

        if (!finalEmail.equals(oldUser.getEmail()) && userRepository.findByEmail(finalEmail).isPresent()) {
            throw new ValidationException("Email уже используется");
        }

        if (!finalLogin.equals(oldUser.getLogin()) && userRepository.findByLogin(finalLogin).isPresent()) {
            throw new ValidationException("Логин уже используется");
        }

        User updateUser = User.builder()
                .id(oldUser.getId())
                .email(finalEmail)
                .login(finalLogin)
                .name(finalName)
                .birthday(finalBirthday)
                .build();

        userRepository.updateUser(updateUser);
        return updateUser;
    }

    public void addToFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userRepository.addFriend(userId, friendId, FriendshipStatus.CONFIRMED);
        eventService.addEvent(userId, EventType.FRIEND, EventOperation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userRepository.removeFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        getUserOrThrow(userId);

        return userRepository.findFriends(userId);
    }

    public List<User> getMutualFriends(Long userId, Long otherId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherId);

        List<User> userFriendIds = userRepository.findFriends(userId);
        List<User> otherFriendIds = userRepository.findFriends(otherId);

        return userFriendIds.stream()
                .filter(otherFriendIds::contains)
                .collect(Collectors.toList());
    }

    //вспомогательный метод для получения user
    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public User getUserById(Long id) {
        return getUserOrThrow(id);
    }

    public void removeUser(Long userId) {
        getUserOrThrow(userId);
        reviewService.removeReviewsByUserId(userId);
        reviewService.removeReviewLikesByUserId(userId);
        reviewService.removeReviewDislikesByUserId(userId);
        userRepository.removeFriendsByUserId(userId, userId);
        filmRepository.removeLikesByUserId(userId);
        eventService.removeEventsByUserId(userId);
        userRepository.removeUser(userId);
    }
}
