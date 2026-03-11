package com.project.dogwalking.service;

import com.project.dogwalking.dto.OrderCreateDto;
import com.project.dogwalking.dto.OrderResponseDto;
import com.project.dogwalking.entity.Contract;
import com.project.dogwalking.entity.Order;
import com.project.dogwalking.entity.User;
import com.project.dogwalking.entity.enums.ContractStatus;
import com.project.dogwalking.entity.enums.OrderStatus;
import com.project.dogwalking.exception.AccessDeniedException;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.ContractRepository;
import com.project.dogwalking.repository.OrderRepository;
import com.project.dogwalking.specification.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ContractRepository contractRepository;
    private final UserService userService;  // для получения сущностей пользователей

    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        // проверяем, что владелец существует и имеет роль OWNER
        User owner = userService.findUserEntityById(dto.getOwnerId());
        if (!owner.getRole().name().equals("OWNER")) {
            throw new BusinessLogicException("Only users with role OWNER can create orders");
        }

        Order order = new Order();
        order.setOwner(owner);
        order.setDogBreed(dto.getDogBreed());
        order.setDogNeeds(dto.getDogNeeds());
        order.setWalkDateTime(dto.getWalkDateTime());
        order.setDurationMinutes(dto.getDurationMinutes());
        order.setMeetingPoint(dto.getMeetingPoint());
        order.setPaymentAmount(dto.getPaymentAmount());
        // статус по умолчанию OPEN

        order = orderRepository.save(order);
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrders(String district, BigDecimal maxPayment, LocalDateTime walkTime) {
        Specification<Order> spec = Specification.where(OrderSpecifications.hasStatusOpen())
                .and(OrderSpecifications.districtContains(district))
                .and(OrderSpecifications.maxPaymentLessThanOrEqual(maxPayment))
                .and(OrderSpecifications.walkTimeGreaterThanOrEqual(walkTime));

        List<Order> orders = orderRepository.findAll(spec);
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToDto(order);
    }

    @Transactional
    public Contract respondToOrder(Long orderId, Long walkerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessLogicException("Order is not open for response");
        }

        User walker = userService.findUserEntityById(walkerId);
        if (!walker.getRole().name().equals("WALKER")) {
            throw new BusinessLogicException("Only users with role WALKER can respond to orders");
        }

        if (contractRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessLogicException("Contract already exists for this order");
        }

        // договор
        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setWalker(walker);
        contract.setPrepaid(true);  // имитация списания предоплаты

        contract = contractRepository.save(contract);

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);

        return contract;  // вернём сущность, потом можно будет преобразовать в DTO в контроллере
    }

    @Transactional
    public void completeOrder(Long orderId, Long walkerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessLogicException("Order cannot be completed because it's not in progress");
        }

        Contract contract = contractRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessLogicException("No contract found for this order"));

        if (!contract.getWalker().getId().equals(walkerId)) {
            throw new AccessDeniedException("Only the assigned walker can complete the order");
        }

        order.setStatus(OrderStatus.COMPLETED);
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());

        // Здесь можно добавить логику списания полной оплаты с карты (имитация)
        // Например, отправить запрос в платёжный шлюз

        orderRepository.save(order);
        contractRepository.save(contract);
    }

    // маппинг в дто
    private OrderResponseDto mapToDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOwnerId(order.getOwner().getId());
        dto.setOwnerName(order.getOwner().getUsername());
        dto.setDogBreed(order.getDogBreed());
        dto.setDogNeeds(order.getDogNeeds());
        dto.setWalkDateTime(order.getWalkDateTime());
        dto.setDurationMinutes(order.getDurationMinutes());
        dto.setMeetingPoint(order.getMeetingPoint());
        dto.setPaymentAmount(order.getPaymentAmount());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}