package com.project.dogwalking.specification;

import com.project.dogwalking.entity.Order;
import com.project.dogwalking.entity.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSpecifications {

    public static Specification<Order> hasStatusOpen() {
        return (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.OPEN);
    }

    public static Specification<Order> districtContains(String district) {
        return (root, query, cb) -> {
            if (district == null || district.trim().isEmpty()) {
                return cb.conjunction(); // всегда истинно (не добавляет условие)
            }
            return cb.like(cb.lower(root.get("meetingPoint")), "%" + district.toLowerCase() + "%");
        };
    }

    public static Specification<Order> maxPaymentLessThanOrEqual(BigDecimal maxPayment) {
        return (root, query, cb) -> {
            if (maxPayment == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("paymentAmount"), maxPayment);
        };
    }

    public static Specification<Order> minPaymentGreaterThanOrEqual(BigDecimal minPayment) {
        return (root, query, cb) -> {
            if (minPayment == null) {
                return cb.conjunction(); // не добавляем условие
            }
            return cb.greaterThanOrEqualTo(root.get("paymentAmount"), minPayment);
        };
    }

    public static Specification<Order> walkTimeGreaterThanOrEqual(LocalDateTime walkTime) {
        return (root, query, cb) -> {
            if (walkTime == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("walkDateTime"), walkTime);
        };
    }
}
