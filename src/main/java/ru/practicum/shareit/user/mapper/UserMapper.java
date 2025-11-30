package ru.practicum.shareit.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.model.User;

@Component
public class UserMapper {
    public UserDTO toUserDto(User user) {
        if (user == null) return null;

        return new UserDTO(user.getId(),
                user.getName(),
                user.getEmail());
    }

    public User toUser(UserDTO userDTO) {
        if (userDTO == null) return null;

        return new User(userDTO.getId(),
                userDTO.getName(),
                userDTO.getEmail());
    }
}
