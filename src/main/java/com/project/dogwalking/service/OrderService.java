package com.project.dogwalking.service;

import com.project.dogwalking.dto.OrderCreateDto;
import com.project.dogwalking.dto.OrderResponseDto;
import com.project.dogwalking.dto.OrderUpdateDto;
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
import jakarta.validation.Valid;
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
            throw new BusinessLogicException("Только пользователи с ролью ВЛАДЕЛЕЦ могут создавать заказы");
        }

        Order order = new Order();
        order.setOwner(owner);
        order.setDogBreed(dto.getDogBreed());
        order.setDogNeeds(dto.getDogNeeds());
        order.setWalkDateTime(dto.getWalkDateTime());
        order.setDurationMinutes(dto.getDurationMinutes());
        order.setMeetingPoint(dto.getMeetingPoint());
        order.setPaymentAmount(dto.getPaymentAmount());

        order = orderRepository.save(order);
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrders(String district,
                                            BigDecimal minPayment,
                                            BigDecimal maxPayment,
                                            LocalDateTime walkTime) {
        Specification<Order> spec = Specification.where(OrderSpecifications.hasStatusOpen())
                .and(OrderSpecifications.districtContains(district))
                .and(OrderSpecifications.minPaymentGreaterThanOrEqual(minPayment))
                .and(OrderSpecifications.maxPaymentLessThanOrEqual(maxPayment))
                .and(OrderSpecifications.walkTimeGreaterThanOrEqual(walkTime));

        List<Order> orders = orderRepository.findAll(spec);
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден контракт с ID: " + id));
        return mapToDto(order);
    }

    @Transactional
    public Contract respondToOrder(Long orderId, Long walkerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден контракт с ID: " + orderId));

        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessLogicException("Заказ закрыт для принятия");
        }

        User walker = userService.findUserEntityById(walkerId);
        if (!walker.getRole().name().equals("WALKER")) {
            throw new BusinessLogicException("Только пользователи с ролью ВЫГУЛЬЩИК могут принимать заказы");
        }

        if (contractRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessLogicException("Контракт для этого заказа уже существует");
        }

        // договор
        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setWalker(walker);
        contract.setPrepaid(true);  // имитация списания предоплаты

        contract = contractRepository.save(contract);

        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);

        return contract;
    }

    @Transactional
    public void completeOrder(Long orderId, Long walkerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Не найден контракт с ID: " + orderId));

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessLogicException(Заказ не может быть завершен, потому что он не 'в процессе'");
        }

        Contract contract = contractRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessLogicException("Для этого заказа контракт не найден"));

        if (!contract.getWalker().getId().equals(walkerId)) {
            throw new AccessDeniedException("Только закрепленный выгульщик может выполнить этот заказ");
        }

        order.setStatus(OrderStatus.COMPLETED);
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setCompletedAt(LocalDateTime.now());

        orderRepository.save(order);
        contractRepository.save(contract);
    }

    @Transactional
    public void cancelOrderByOwner(Long orderId, Long ownerId) {
        // Находим заказ
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден с id: " + orderId));

        // Проверяем, что заказ принадлежит этому владельцу
        if (!order.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Вы не являетесь владельцем этого заказа");
        }

        // Проверяем, можно ли отменить заказ (только OPEN или IN_PROGRESS)
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessLogicException("Нельзя отменить завершённый или уже отменённый заказ");
        }

        // Если заказ в статусе IN_PROGRESS, значит есть договор и предоплата
        if (order.getStatus() == OrderStatus.IN_PROGRESS) {
            Contract contract = contractRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new BusinessLogicException("Договор не найден для заказа в статусе IN_PROGRESS"));

            // Предоплата остаётся исполнителю (ничего не делаем с contract.prepaid)
            
            // Меняем статус договора на CANCELLED
            contract.setStatus(ContractStatus.CANCELLED);
            contractRepository.save(contract);
        }
        
        // Меняем статус заказа на CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

    }

    @Transactional
    public void cancelOrderByWalker(Long orderId, Long walkerId) {
        // Находим заказ
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден с id: " + orderId));

        // Заказ должен быть IN_PROGRESS (только тогда есть договор с выгульщиком)
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessLogicException("Выгульщик может отменить только заказ, на который уже откликнулся");
        }

        // Находим договор
        Contract contract = contractRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessLogicException("Договор не найден для этого заказа"));

        // Проверяем, что отменяет именно тот выгульщик, который взял заказ
        if (!contract.getWalker().getId().equals(walkerId)) {
            throw new AccessDeniedException("Вы не являетесь исполнителем по этому заказу");
        }

        // Предоплата возвращается владельцу
        contract.setPrepaid(false); // Помечаем, что предоплата возвращена
        contract.setStatus(ContractStatus.CANCELLED);

        // В реальном проекте здесь был бы вызов платёжного шлюза для возврата средств

        // Меняем статус заказа обратно на OPEN, чтобы другие выгульщики могли откликнуться
        order.setStatus(OrderStatus.OPEN);

        contractRepository.save(contract);
        orderRepository.save(order);

    }

    @Transactional
    public OrderResponseDto updateOrderByOwner(Long orderId, Long ownerId, @Valid OrderUpdateDto updateDto) {
        // Находим заказ
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден с id: " + orderId));

        // Проверяем, что заказ принадлежит этому владельцу
        if (!order.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Вы не являетесь владельцем этого заказа");
        }

        // Проверяем, что заказ ещё открыт (статус OPEN)
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessLogicException("Нельзя изменить заказ, который уже в работе или завершён");
        }

        // Проверяем, что на заказ ещё не откликнулись (нет договора)
        if (contractRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessLogicException("Нельзя изменить заказ, на который уже откликнулся исполнитель");
        }

        // Обновляем только разрешённые поля
        order.setDogBreed(updateDto.getDogBreed());
        order.setDogNeeds(updateDto.getDogNeeds());
        order.setWalkDateTime(updateDto.getWalkDateTime());
        order.setDurationMinutes(updateDto.getDurationMinutes());
        order.setMeetingPoint(updateDto.getMeetingPoint());
        order.setPaymentAmount(updateDto.getPaymentAmount());

        // Сохраняем изменения
        order = orderRepository.save(order);

        return mapToDto(order);
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
