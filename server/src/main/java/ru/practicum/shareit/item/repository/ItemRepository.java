package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    void deleteAllByOwnerId(int id);

    Page<Item> findAllByOwnerId(int id, Pageable page);

    Page<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(String name,
                                                                                                 String description,
                                                                                                 Pageable page);

    List<Item> findAllByRequestId(int requestId);
}
