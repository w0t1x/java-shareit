package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserStorage userStorage;

    @Override
    public UserDTO get(Long id) {
        return userMapper.toUserDto(userStorage.getUser(id));
    }

    @Override
    public Collection<UserDTO> getAll() {
        return userStorage.getAllUsers().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDTO add(UserDTO userDto) {
        User user = userStorage.add(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDTO patch(UserDTO userDto, Long id) {
        userDto.setId(id);
        User user = userStorage.patch(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public Boolean delete(Long id) {
        return userStorage.deleate(id);
    }
}
