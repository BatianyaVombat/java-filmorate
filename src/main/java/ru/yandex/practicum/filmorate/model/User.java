package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"email", "login"})
@Builder
public class User {
    private Long id;

    @Email(message = "Электронная почта не может быть пустой и должна иметь корректный формат")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private final Set<Friendship> friendRequests = new HashSet<>(); //заявки в друзья

    //метод проверяет данные User при обновлении и не допускает внезапные null если часть данных не обновляется
    public User mergeWith(User newData) {
        //следим за изменением имени
        String finalName;
        if (newData.getName() != null && !newData.getName().isBlank()) {
            finalName = newData.getName();
        } else {
            finalName = newData.getLogin() != null ? newData.getLogin() : this.login;
        }

        return User.builder()
                .id(newData.getId() != null ? newData.getId() : this.id)
                .email(newData.getEmail() != null ? newData.getEmail() : this.email)
                .login(newData.getLogin() != null ? newData.getLogin() : this.login)
                .name(finalName)
                .birthday(newData.getBirthday() != null ? newData.getBirthday() : this.birthday)
                .build();
    }

    //нормализация поля name: оно может быть пустым или вылезать null при обновлении
    public void normalize() {
        if (this.name == null || name.isBlank()) {
            this.name = this.login;
        }
    }

}
