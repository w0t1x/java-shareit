package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDTO;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository repo;
    private UserMapper mapper;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        mapper = mock(UserMapper.class);

        // ВАЖНО: у вас конструктор (UserMapper, UserRepository)
        service = new UserServiceImpl(mapper, repo);
    }

    @Test
    void create_nullBody_throws() {
        assertThrows(ValidationException.class, () -> callCreate(service, null));
    }

    @Test
    void create_blankName_throws() {
        UserDTO dto = new UserDTO();
        dto.setName(" ");
        dto.setEmail("a@b.ru");

        assertThrows(ValidationException.class, () -> callCreate(service, dto));
    }

    @Test
    void create_success_savesAndReturnsDto() {
        UserDTO in = new UserDTO();
        in.setName("Ann");
        in.setEmail("a@b.ru");

        User toSave = new User();
        toSave.setName("Ann");
        toSave.setEmail("a@b.ru");

        User saved = new User();
        saved.setName("Ann");
        saved.setEmail("a@b.ru");

        when(mapper.toUser(in)).thenReturn(toSave);
        when(repo.save(any())).thenReturn(saved);
        when(mapper.toUserDto(saved)).thenReturn(in);

        UserDTO out = callCreate(service, in);

        assertSame(in, out);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        assertEquals("Ann", captor.getValue().getName());
    }

    @Test
    void getById_notFound_throws() {
        // Универсально по типу ID (Integer/Long) — any()
        when(repo.findById(any())).thenReturn(Optional.empty());
        // если у вас внутри сервиса используется getById/getReferenceById — тоже подстрахуемся
        tryStubGetByIdToThrow();

        assertThrows(NotFoundException.class, () -> callGetById(service, 99));
    }

    @Test
    void getAll_returnsMapped() {
        User u1 = new User();
        User u2 = new User();

        when(repo.findAll()).thenReturn(List.of(u1, u2));
        when(mapper.toUserDto(any())).thenReturn(new UserDTO());

        List<?> res = callGetAll(service);

        assertEquals(2, res.size());
        verify(mapper, times(2)).toUserDto(any());
    }

    // ---- reflection helpers ----

    private static UserDTO callCreate(Object service, UserDTO dto) {
        return invokeAny(service,
                new String[]{"create", "addUser", "add", "createUser"},
                new Object[]{dto});
    }

    private static Object callGetById(Object service, int id) {
        // пробуем разные типы ID: int/Integer/long/Long
        try {
            return invokeAny(service, new String[]{"getById", "getUserById", "get"}, new Object[]{id});
        } catch (Exception ignored) {
        }
        try {
            return invokeAny(service, new String[]{"getById", "getUserById", "get"}, new Object[]{Integer.valueOf(id)});
        } catch (Exception ignored) {
        }
        try {
            return invokeAny(service, new String[]{"getById", "getUserById", "get"}, new Object[]{(long) id});
        } catch (Exception ignored) {
        }
        return invokeAny(service, new String[]{"getById", "getUserById", "get"}, new Object[]{Long.valueOf(id)});
    }

    @SuppressWarnings("unchecked")
    private static <T> T callGetAll(Object service) {
        return invokeAny(service,
                new String[]{"getAll", "getAllUsers", "getAllUser"},
                new Object[]{});
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeAny(Object target, String[] names, Object[] args) {
        for (String name : names) {
            for (Method m : target.getClass().getMethods()) {
                if (!m.getName().equals(name)) continue;
                if (m.getParameterCount() != args.length) continue;

                try {
                    return (T) m.invoke(target, args);

                } catch (IllegalArgumentException ignored) {
                    // сигнатура не подошла — пробуем дальше

                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause();
                    if (cause instanceof RuntimeException re) throw re;
                    if (cause instanceof Error err) throw err;
                    throw new RuntimeException(cause);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new IllegalStateException("No suitable method found. Tried: " + String.join("/", names));
    }


    private void tryStubGetByIdToThrow() {
        // Если сервис использует getById/getReferenceById вместо findById
        try {
            doThrow(new RuntimeException("not found"))
                    .when(repo).getById(any());
        } catch (Throwable ignored) {
        }
        try {
            doThrow(new RuntimeException("not found"))
                    .when(repo).getReferenceById(any());
        } catch (Throwable ignored) {
        }
    }
}
