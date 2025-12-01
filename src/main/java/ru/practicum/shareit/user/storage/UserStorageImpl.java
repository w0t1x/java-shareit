package ru.practicum.shareit.user.storage;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserStorageImpl implements UserStorage {
    private Long increment = 0L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User getUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User add(User user) {
        validateEmail(user);
        user.setId(++increment);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User patch(User user) {
        User current = getUser(user.getId());

        if (user.getName() != null && !user.getName().isBlank()) {
            current.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            validateEmail(user);
            current.setEmail(user.getEmail());
        }
        return current;
    }

    @Override
    public Boolean deleate(Long id) {
        users.remove(id);
        return !users.containsKey(id);
    }

    private void validateEmail(User user) {
        if (user.getEmail() == null) {
            return;
        }

        boolean emailExists = users.values().stream()
                .anyMatch(stored ->
                        stored.getEmail() != null &&
                                stored.getEmail().equalsIgnoreCase(user.getEmail()) &&
                                !stored.getId().equals(user.getId())
                );

        if (emailExists) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
    }
}
