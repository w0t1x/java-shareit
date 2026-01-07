package ru.practicum.shareit.user.service;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

public interface UserService{
    UserDTO get(Long id);

    Collection<UserDTO> getAll();

    UserDTO add(UserDTO userDto);

    UserDTO patch(UserDTO userDto, Long id);

    void delete(Long id);
}
