package ru.yandex.practicum.filmorate.dal;

import org.apache.logging.log4j.util.InternalException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public List<User> findAll() {
        String sqlFindAll = "SELECT * FROM Users";
        return findMany(sqlFindAll);
    }

    public Optional<User> findById(Long userId) {
        String sqlFindById = "SELECT * FROM Users WHERE id = ?";
        return findOne(sqlFindById, userId);
    }

    public Optional<User> findByEmail(String email) {
        String sqlFindByEmail = "SELECT * FROM Users WHERE email = ?";
        return findOne(sqlFindByEmail, email);
    }

    public Optional<User> findByLogin(String login) {
        String sqlFindByLogin = "SELECT * FROM Users WHERE login = ?";
        return findOne(sqlFindByLogin, login);
    }

    public User saveUser(User user) {
        String sqlUser = "INSERT INTO Users (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";
        jdbc.update(sqlUser, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        Optional<User> saved = findByEmail(user.getEmail());

        return saved.orElseThrow(
                () -> new InternalException("Не удалось найти пользователя после сохранения")
        );
    }

    public void updateUser(User user) {
        String sqlUpd = "UPDATE Users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        update(sqlUpd, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
    }

    public List<User> findFriends(Long userId) {
        String sql = "SELECT u.* FROM Users u " +
                "JOIN Friends f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 'CONFIRMED'";

        return findMany(sql, userId);
    }

    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        String sql = "INSERT INTO Friends (user_id, friend_id, status) " +
                "VALUES (?, ?, ?)";
        execute(sql, userId, friendId, status.name());
    }

    public boolean removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM Friends " +
                "WHERE user_id = ? AND friend_id = ?";

        return delete(sql, userId, friendId);
    }
}
