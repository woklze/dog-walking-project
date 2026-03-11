package com.project.dogwalking.repository;

import com.project.dogwalking.entity.Order;
import com.project.dogwalking.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByOwnerId(Long ownerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    // Удали старый метод findOrdersByFilters
}