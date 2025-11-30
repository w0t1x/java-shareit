package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserStorage {
    User getUser(Long id);

    Collection<User> getAllUsers();

    User add(User user);

    User patch(User user);

    Boolean deleate(Long id);
}
