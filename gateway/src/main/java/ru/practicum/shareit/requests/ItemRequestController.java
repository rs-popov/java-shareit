package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;
    private static final String USERID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USERID) Long requesterId,
                                                @Valid @NotNull @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Create request by userId={}", requesterId);
        return itemRequestClient.createRequest(requesterId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USERID) Long userId,
                                                 @PathVariable @NotNull Long requestId) {
        log.info("Get request with requestId={} by userId={}", requestId, userId);
        return itemRequestClient.getRequestById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsFromRequester(@RequestHeader(USERID) Long requesterId) {
        log.info("Get requests by userId={}", requesterId);
        return itemRequestClient.getAllRequestsFromRequester(requesterId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USERID) Long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = "20") @Positive Integer size) {
        log.info("Get requests by owner with userId={}", userId);
        return itemRequestClient.getAllRequests(userId, from, size);
    }
}