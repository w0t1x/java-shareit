package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
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
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto dto) {
        User user = findUserById(userId);

        validateItemCreate(dto);

        Item item = Item.builder()
                .userId(user.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();

        Item savedItem = itemRepository.save(item);

        ItemDto out = itemMapper.toItemDto(savedItem);

        out.setComments(new ArrayList<>());
        out.setLastBooking(null);
        out.setNextBooking(null);
        return out;
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        findUserById(userId);

        Item item = findItemById(itemId);

        if (!Objects.equals(item.getUserId(), userId)) {
            throw new ForbiddenException("Только владелец может обновить элемент");
        }

        if (dto.getName() != null) {
            if (dto.getName().isBlank()) throw new ValidationException("name must not be blank");
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            if (dto.getDescription().isBlank()) throw new ValidationException("description must not be blank");
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        Item saved = itemRepository.save(item);
        return enrich(saved, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getById(Long userId, Long itemId) {
        findUserById(userId);
        Item item = findItemById(itemId);
        return enrich(item, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAllByOwner(Long ownerId) {
        findUserById(ownerId);
        List<Item> items = itemRepository.findByUserIdOrderByIdAsc(ownerId);

        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(enrich(item, ownerId));
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(Long userId, String text) {
        findUserById(userId);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toItemDto).toList();
    }

    @Transactional
    @Override
    public CommentDto addComment(long userId, long itemId, CommentCreateDto dto) {
        User author = findUserById(userId);
        Item item = findItemById(itemId);

        boolean allowed = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndTimeBefore(
                itemId, userId, Status.APPROVED, LocalDateTime.now()
        );


        if (!allowed) {
            throw new ValidationException("Пользователь не выполнил утвержденное бронирование");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return commentMapper.toDto(commentRepository.save(comment));
    }

    private ItemDto enrich(Item item, long requesterId) {
        ItemDto dto = itemMapper.toItemDto(item);

        // comments
        List<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId())
                .stream()
                .map(commentMapper::toDto)
                .toList();
        dto.setComments(new ArrayList<>(comments));

        // last/next bookings только владельцу
        if (item.getUserId() == requesterId) {
            LocalDateTime now = LocalDateTime.now();
            BookingDtoShort last = bookingRepository
                    .findFirstByItemIdAndStartTimeBeforeAndStatusOrderByStartTimeDesc(item.getId(), now, Status.APPROVED)
                    .map(bookingMapper::toBookingDtoShort)
                    .orElse(null);

            BookingDtoShort next = bookingRepository
                    .findFirstByItemIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(item.getId(), now, Status.APPROVED)
                    .map(bookingMapper::toBookingDtoShort)
                    .orElse(null);

            dto.setLastBooking(last);
            dto.setNextBooking(next);
        } else {
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        return dto;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));
    }

    private Item findItemById(Long ItemId) {
        return itemRepository.findById(ItemId).orElseThrow(() -> new NotFoundException("Нет такого пользователя"));
    }

    private void validateItemCreate(ItemDto dto) {
        if (dto == null) throw new ValidationException("body is null");
        if (dto.getName() == null || dto.getName().isBlank()) throw new ValidationException("name must not be blank");
        if (dto.getDescription() == null || dto.getDescription().isBlank())
            throw new ValidationException("description must not be blank");
        if (dto.getAvailable() == null) throw new ValidationException("available must not be null");
    }
}
