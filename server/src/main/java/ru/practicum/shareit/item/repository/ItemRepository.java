package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByUserIdOrderByIdAsc(long userId);

    @Query("""
                select i from Item i
                where i.available = true
                  and (
                       lower(i.name) like lower(concat('%', :text, '%'))
                    or lower(i.description) like lower(concat('%', :text, '%'))
                  )
            """)
    List<Item> searchAvailable(@Param("text") String text);

    List<Item> findAllByRequestIdIn(Collection<Long> requestIds);

    List<Item> findAllByRequestIdOrderByIdAsc(Long requestId);

    List<Item> findAllByUserIdOrderByIdAsc(Long requestId);
}
