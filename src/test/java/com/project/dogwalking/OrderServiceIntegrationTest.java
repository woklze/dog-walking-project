package com.project.dogwalking;

import com.project.dogwalking.dto.OrderCreateDto;
import com.project.dogwalking.dto.OrderResponseDto;
import com.project.dogwalking.dto.OrderUpdateDto;
import com.project.dogwalking.entity.Order;
import com.project.dogwalking.entity.User;
import com.project.dogwalking.entity.enums.OrderStatus;
import com.project.dogwalking.entity.enums.Role;

import com.project.dogwalking.exception.AccessDeniedException;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.OrderRepository;
import com.project.dogwalking.repository.UserRepository;
import com.project.dogwalking.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",               // отключаем миграции для h2
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})

class OrderServiceIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;

    private User owner;
    private User walker;

    @BeforeEach
    void setUp() {
        owner = createUser("owner@test.com", "owner", Role.OWNER);
        walker = createUser("walker@test.com", "walker", Role.WALKER);
    }

    // ========== Нормальные сценарии ==========
    @Test
    void createOrder_Success() {
        OrderCreateDto dto = validCreateDto(owner.getId());
        OrderResponseDto created = orderService.createOrder(dto);
        assertThat(created.getId()).isNotNull();
    }

    // ========== Аномальные сценарии при создании ==========
    @Test
    void createOrder_NullMeetingPoint_ThrowsDataIntegrityViolation() {
        OrderCreateDto dto = validCreateDto(owner.getId());
        dto.setMeetingPoint(null);
        assertThrows(DataIntegrityViolationException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_NullDogBreed_ThrowsDataIntegrityViolation() {
        OrderCreateDto dto = validCreateDto(owner.getId());
        dto.setDogBreed(null);
        assertThrows(DataIntegrityViolationException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_NullWalkDateTime_ThrowsDataIntegrityViolation() {
        OrderCreateDto dto = validCreateDto(owner.getId());
        dto.setWalkDateTime(null);
        assertThrows(DataIntegrityViolationException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_NegativeDuration_AllowedByDatabase() {
        OrderCreateDto dto = validCreateDto(owner.getId());
        dto.setDurationMinutes(-10);
        // БД позволяет отрицательные значения, если нет CHECK constraint
        OrderResponseDto created = orderService.createOrder(dto);
        assertThat(created.getDurationMinutes()).isEqualTo(-10);
        // Это бизнес-аномалия: тест показывает, что валидация отсутствует
    }

    @Test
    void createOrder_WalkerAsOwner_ThrowsBusinessLogicException() {
        OrderCreateDto dto = validCreateDto(walker.getId()); // walker пытается создать заказ
        assertThrows(BusinessLogicException.class, () -> orderService.createOrder(dto));
    }

    // ========== Аномалии при отклике ==========
    @Test
    void respondToOrder_InvalidOrderId_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.respondToOrder(999L, walker.getId()));
    }

    @Test
    void respondToOrder_InvalidWalkerId_ThrowsResourceNotFoundException() {
        Order order = createOpenOrder(owner);
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.respondToOrder(order.getId(), 999L));
    }

    @Test
    void respondToOrder_OrderAlreadyInProgress_ThrowsBusinessLogicException() {
        Order order = createOpenOrder(owner);
        orderService.respondToOrder(order.getId(), walker.getId());
        assertThrows(BusinessLogicException.class,
                () -> orderService.respondToOrder(order.getId(), walker.getId()));
    }

    // ========== Аномалии при завершении ==========
    @Test
    void completeOrder_OrderNotInProgress_ThrowsBusinessLogicException() {
        Order order = createOpenOrder(owner);
        assertThrows(BusinessLogicException.class,
                () -> orderService.completeOrder(order.getId(), walker.getId()));
    }

    @Test
    void completeOrder_WrongWalker_ThrowsAccessDeniedException() {
        Order order = createOpenOrder(owner);
        orderService.respondToOrder(order.getId(), walker.getId());
        User otherWalker = createUser("other@w.com", "other", Role.WALKER);
        assertThrows(AccessDeniedException.class,
                () -> orderService.completeOrder(order.getId(), otherWalker.getId()));
    }

    // ========== Аномалии при отмене владельцем ==========
    @Test
    void cancelOrderByOwner_AlreadyCancelled_ThrowsBusinessLogicException() {
        Order order = createOpenOrder(owner);
        orderService.cancelOrderByOwner(order.getId(), owner.getId());
        assertThrows(BusinessLogicException.class,
                () -> orderService.cancelOrderByOwner(order.getId(), owner.getId()));
    }

    @Test
    void cancelOrderByOwner_NotOwner_ThrowsAccessDeniedException() {
        Order order = createOpenOrder(owner);
        assertThrows(AccessDeniedException.class,
                () -> orderService.cancelOrderByOwner(order.getId(), walker.getId()));
    }

    // ========== Аномалии при отмене выгульщиком ==========
    @Test
    void cancelOrderByWalker_OrderNotInProgress_ThrowsBusinessLogicException() {
        Order order = createOpenOrder(owner);
        assertThrows(BusinessLogicException.class,
                () -> orderService.cancelOrderByWalker(order.getId(), walker.getId()));
    }

    @Test
    void cancelOrderByWalker_NotAssignedWalker_ThrowsAccessDeniedException() {
        Order order = createOpenOrder(owner);
        orderService.respondToOrder(order.getId(), walker.getId());
        User otherWalker = createUser("other2@w.com", "other2", Role.WALKER);
        assertThrows(AccessDeniedException.class,
                () -> orderService.cancelOrderByWalker(order.getId(), otherWalker.getId()));
    }

    // ========== Аномалии обновления заказа ==========
    @Test
    void updateOrderByOwner_OrderNotOpen_ThrowsBusinessLogicException() {
        Order order = createOpenOrder(owner);
        orderService.respondToOrder(order.getId(), walker.getId());
        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setDogBreed("New");
        assertThrows(BusinessLogicException.class,
                () -> orderService.updateOrderByOwner(order.getId(), owner.getId(), updateDto));
    }

    @Test
    void updateOrderByOwner_NotOwner_ThrowsAccessDeniedException() {
        Order order = createOpenOrder(owner);
        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setDogBreed("New");
        assertThrows(AccessDeniedException.class,
                () -> orderService.updateOrderByOwner(order.getId(), walker.getId(), updateDto));
    }

    // ========== Вспомогательные методы ==========
    private User createUser(String email, String username, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setPasswordHash("hash");
        u.setRole(role);
        return userRepository.save(u);
    }

    private Order createOpenOrder(User owner) {
        Order order = new Order();
        order.setOwner(owner);
        order.setDogBreed("TestBreed");
        order.setDogNeeds("Need walk");
        order.setWalkDateTime(LocalDateTime.now().plusDays(1));
        order.setDurationMinutes(30);
        order.setMeetingPoint("TestPoint");
        order.setPaymentAmount(BigDecimal.valueOf(500));
        order.setStatus(OrderStatus.OPEN);
        return orderRepository.save(order);
    }

    private OrderCreateDto validCreateDto(Long ownerId) {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setOwnerId(ownerId);
        dto.setDogBreed("Poodle");
        dto.setDogNeeds("Walking");
        dto.setWalkDateTime(LocalDateTime.now().plusDays(1));
        dto.setDurationMinutes(45);
        dto.setMeetingPoint("Park");
        dto.setPaymentAmount(BigDecimal.valueOf(600));
        return dto;
    }
}
