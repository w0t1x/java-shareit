package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDTO get(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %s не найден", id)));
        return userMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDTO> getAll() {
        Collection<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDTO add(UserDTO userDto) {
        validateCreate(userDto);

        User user = userMapper.toUser(userDto);
        User save = userRepository.save(user);
        return userMapper.toUserDto(save);
    }

    @Transactional
    @Override
    public UserDTO patch(UserDTO userDto, Long id) {
        User user = userMapper.toUser(userDto);
        User targetUser = findUserById(id);

        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("Имя не должно быть пустым");
            }
            targetUser.setName(user.getName());
        }

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new ValidationException("Email не должен быть пустым");
            }
            targetUser.setEmail(user.getEmail());
        }

        return userMapper.toUserDto(userRepository.save(targetUser));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private void validateCreate(UserDTO dto) {
        if (dto == null) throw new ValidationException("body is null");
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("name must not be blank");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ValidationException("email must not be blank");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));
    }
}
