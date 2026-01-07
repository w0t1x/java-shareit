package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;
    private ItemMapper itemMapper;
    private BookingMapper bookingMapper;
    private CommentMapper commentMapper;
    private ItemRequestRepository requestRepo;

    private ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
        itemMapper = mock(ItemMapper.class);
        bookingMapper = mock(BookingMapper.class);
        commentMapper = mock(CommentMapper.class);
        requestRepo = mock(ItemRequestRepository.class);

        service = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository,
                itemMapper, bookingMapper, commentMapper, requestRepo);
    }

    @Test
    void create_nullBody_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        assertThrows(ValidationException.class, () -> service.create(1L, null));
    }

    @Test
    void create_requestNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(requestRepo.findById(10L)).thenReturn(Optional.empty());

        ItemDto dto = new ItemDto();
        dto.setName("n");
        dto.setDescription("d");
        dto.setAvailable(true);
        dto.setRequestId(10L);

        assertThrows(NotFoundException.class, () -> service.create(1L, dto));
    }

    @Test
    void update_notOwner_throwsForbidden() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(Item.builder().id(2L).userId(99L).build()));

        ItemDto patch = new ItemDto();
        patch.setName("new");

        assertThrows(ForbiddenException.class, () -> service.update(1L, 2L, patch));
    }

    @Test
    void addComment_withoutApprovedBooking_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).name("u").build()));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(Item.builder().id(2L).userId(99L).build()));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndTimeBefore(eq(2L), eq(1L), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("hi");

        assertThrows(ValidationException.class, () -> service.addComment(1L, 2L, dto));
    }

    @Test
    void addComment_success_savesAndMaps() {
        User author = User.builder().id(1L).name("u").build();
        Item item = Item.builder().id(2L).userId(99L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndTimeBefore(eq(2L), eq(1L), eq(Status.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);

        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommentDto out = new CommentDto();
        when(commentMapper.toDto(any())).thenReturn(out);

        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("hi");

        assertSame(out, service.addComment(1L, 2L, dto));
        verify(commentRepository).save(any());
        verify(commentMapper).toDto(any());
    }
}
