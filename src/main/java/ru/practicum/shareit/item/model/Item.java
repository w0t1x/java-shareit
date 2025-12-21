package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId; // владелец вещи (id пользователя)

    @Column(nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(nullable = false)
    private Boolean available; // статус: доступна ли к аренде

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id) && Objects.equals(name, item.name)
                && Objects.equals(description, item.description) && Objects.equals(available, item.available)
                && Objects.equals(userId, item.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, available, userId);
    }
}
