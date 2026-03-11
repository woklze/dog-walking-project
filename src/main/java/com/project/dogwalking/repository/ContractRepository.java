package com.project.dogwalking.repository;

import com.project.dogwalking.entity.Contract;
import com.project.dogwalking.entity.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // найти договор по заказу (уникальный, поэтому опшнал)
    Optional<Contract> findByOrderId(Long orderId);
    // все договоры выгульщика (по айди)
    List<Contract> findByWalkerId(Long walkerId);
    // все договоры со активным статусом
    List<Contract> findByStatus(ContractStatus status);

    // проверить, есть ли уже активный договор по заказу
    boolean existsByOrderIdAndStatus(Long orderId, ContractStatus status);
}