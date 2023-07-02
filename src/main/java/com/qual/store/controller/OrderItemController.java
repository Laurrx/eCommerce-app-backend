package com.qual.store.controller;

import com.qual.store.converter.OrderItemConverter;
import com.qual.store.dto.OrderItemDto;
import com.qual.store.converter.lazyConverter.OrderItemLazyConverter;
import com.qual.store.dto.lazyDto.OrderItemWithProductDto;
import com.qual.store.logger.Log;
import com.qual.store.model.OrderItem;
import com.qual.store.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/orderItems")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderItemConverter orderItemConverter;

    @Autowired
    private OrderItemLazyConverter orderItemLazyConverter;

    @GetMapping
    @Log
    public List<OrderItemDto> getAllOrderItems() {
        return orderItemService.getAllOrderItems().stream()
                .map(orderItem -> orderItemConverter.convertModelToDto(orderItem))
                .collect(Collectors.toList());
    }

    @GetMapping("/lazy")
    @Log
    public List<OrderItemWithProductDto> getAllOrderItemsWithProduct() {
        return orderItemService.getAllOrderItems().stream()
                .map(orderItem -> orderItemLazyConverter.convertModelToDto(orderItem))
                .collect(Collectors.toList());
    }


    @PostMapping(value = "/{productId}")
    @Log
    public OrderItemDto addOrderItem(@PathVariable("productId") Long productId, @RequestBody OrderItem orderItem) {
        return orderItemConverter.convertModelToDto(
                orderItemService.addOrderItem(productId, orderItem)
        );
    }

    @DeleteMapping(value = "/{id}")
    @Log
    public ResponseEntity<?> deleteOrderItemById(@PathVariable("id") Long id) {
        orderItemService.deleteOrderItemById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body("OrderItem deleted");
    }

    @PutMapping(value = "/{id}/quantity")
    @Log
    public ResponseEntity<?> decreaseQuantity(@PathVariable("id") Long id, @RequestParam Integer quantity) {
        orderItemService.findOrderItemById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));

        orderItemService.modifyQuantity(id, quantity);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Quantity modified");
    }
}