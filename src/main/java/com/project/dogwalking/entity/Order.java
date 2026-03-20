package com.project.dogwalking.entity;

import com.project.dogwalking.entity.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)   // многие заказы к одному пользователю
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "dog_breed", nullable = false)
    private String dogBreed;

    @Column(name = "dog_needs", columnDefinition = "TEXT")
    private String dogNeeds; 

    @Column(name = "walk_date_time", nullable = false)
    private LocalDateTime walkDateTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "meeting_point", nullable = false)
    private String meetingPoint;

    @Column(name = "payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.OPEN;  // по умолчанию OPEN

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
