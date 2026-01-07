package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplTest {

    private ItemRequestRepository requestRepo;
    private UserRepository userRepo;
    private ItemRepository itemRepo;

    private ItemRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        requestRepo = mock(ItemRequestRepository.class);
        userRepo = mock(UserRepository.class);
        itemRepo = mock(ItemRepository.class);
        service = new ItemRequestServiceImpl(requestRepo, userRepo, itemRepo);
    }

    @Test
    void create_noUser_throwsNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("x");
        assertThrows(NotFoundException.class, () -> service.create(1L, dto));
    }

    @Test
    void create_blankDescription_throwsValidation() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(ru.practicum.shareit.user.model.User.builder().id(1L).build()));
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription(" ");
        assertThrows(ValidationException.class, () -> service.create(1L, dto));
    }

    @Test
    void getOwn_attachesItems() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(ru.practicum.shareit.user.model.User.builder().id(1L).build()));
        ItemRequest r1 = ItemRequest.builder().id(10L).requestorId(1L).description("d").created(java.time.LocalDateTime.now()).build();
        when(requestRepo.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(r1));

        Item item = Item.builder().id(99L).requestId(10L).userId(2L).name("i").description("d").available(true).build();
        when(itemRepo.findAllByRequestIdIn(any())).thenReturn(List.of(item));

        List<ItemRequestDto> out = service.getOwn(1L);
        assertEquals(1, out.size());
        assertEquals(1, out.get(0).getItems().size());
        assertEquals(99L, out.get(0).getItems().get(0).getId());
    }
}
