package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;

@Data
@EqualsAndHashCode(of = {"userId", "friendId"})
public class Friendship {
    private final Long userId;
    private final Long friendId;
    private final FriendshipStatus status;

    public Friendship(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = FriendshipStatus.PENDING;
    }
}
