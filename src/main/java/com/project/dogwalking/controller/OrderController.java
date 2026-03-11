package com.project.dogwalking.controller;

import com.project.dogwalking.dto.ContractResponseDto;
import com.project.dogwalking.dto.OrderCreateDto;
import com.project.dogwalking.dto.OrderResponseDto;
import com.project.dogwalking.entity.Contract;
import com.project.dogwalking.service.OrderService;
import com.project.dogwalking.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ContractService contractService; // для преобразования Contract в DTO

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderCreateDto createDto) {
        OrderResponseDto createdOrder = orderService.createOrder(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) BigDecimal maxPayment,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime walkTime) {
        List<OrderResponseDto> orders = orderService.getOrders(district, maxPayment, walkTime);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<ContractResponseDto> respondToOrder(
            @PathVariable Long id,
            @RequestParam Long walkerId) {
        Contract contract = orderService.respondToOrder(id, walkerId);
        // Преобразуем сущность Contract в ContractResponseDto через сервис или маппер
        ContractResponseDto contractDto = contractService.getContractById(contract.getId()); // либо отдельный метод
        return ResponseEntity.ok(contractDto);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeOrder(
            @PathVariable Long id,
            @RequestParam Long walkerId) {
        orderService.completeOrder(id, walkerId);
        return ResponseEntity.noContent().build();
    }
}