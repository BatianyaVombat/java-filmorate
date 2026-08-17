package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRepository.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryTest {
    private final UserRepository userRepository;
    private final JdbcTemplate jdbc;

    private Long insertUser(String email, String login, String name, String birthday) {
        jdbc.update(
                "INSERT INTO Users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                email, login, name, java.sql.Date.valueOf(birthday)
        );
        // получаем id
        return jdbc.queryForObject("SELECT id FROM Users WHERE email = ?", Long.class, email);
    }

    @Test
    void findByIdShouldReturnUser() {
        Long id = insertUser("test@example.com", "testLogin", "Test User", "1990-01-01");

        Optional<User> userOpt = userRepository.findById(id);

        assertThat(userOpt)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(id);
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                });
    }

    @Test
    void findByIdShouldReturnEmpty() {
        Optional<User> userOpt = userRepository.findById(999L);
        assertThat(userOpt).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnUser() {
        insertUser("test@example.com", "testLogin", "Test User", "1990-01-01");
        Optional<User> userOpt = userRepository.findByEmail("test@example.com");
        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getLogin()).isEqualTo("testLogin");
    }

    @Test
    void findAllShouldReturnAllUsers() {
        insertUser("u1@example.com", "login1", "User1", "1990-01-01");
        insertUser("u2@example.com", "login2", "User2", "1991-02-02");

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void saveUserShouldGenerateId() {
        User user = User.builder()
                .email("new@example.com")
                .login("newLogin")
                .name("New User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User saved = userRepository.saveUser(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByEmail("new@example.com")).isPresent();
    }

    @Test
    void updateUserShouldChangeEmail() {
        Long id = insertUser("old@example.com", "oldLogin", "Old User", "1990-01-01");
        User user = userRepository.findById(id).get();
        user.setEmail("new@example.com");

        userRepository.updateUser(user);

        assertThat(userRepository.findById(id).get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void friendLifecycleShouldWork() {
        Long user1 = insertUser("u1@example.com", "login1", "User1", "1990-01-01");
        Long user2 = insertUser("u2@example.com", "login2", "User2", "1991-02-02");

        userRepository.addFriend(user1, user2, FriendshipStatus.CONFIRMED);

        List<User> friends = userRepository.findFriends(user1);
        assertThat(friends).hasSize(1);
        assertThat(friends.getFirst().getId()).isEqualTo(user2);

        userRepository.removeFriend(user1, user2);

        assertThat(userRepository.findFriends(user1)).isEmpty();
    }
}
