package ru.practicum.shareit.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.requests.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query(" select ir from ItemRequest ir " +
            "where ir.requestor.id = ?1 " +
            "order by ir.id desc")
    List<ItemRequest> findItemRequestsByRequestorId(Long itemRequestId);
}