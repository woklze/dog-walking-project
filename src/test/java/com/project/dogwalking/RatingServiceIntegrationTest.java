package com.project.dogwalking;

import com.project.dogwalking.dto.RatingCreateDto;
import com.project.dogwalking.dto.RatingResponseDto;
import com.project.dogwalking.entity.*;
import com.project.dogwalking.entity.enums.ContractStatus;
import com.project.dogwalking.entity.enums.OrderStatus;
import com.project.dogwalking.entity.enums.Role;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.service.RatingService;
import com.project.dogwalking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})

class RatingServiceIntegrationTest {

    @Autowired private RatingService ratingService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ContractRepository contractRepository;

    private User owner, walker;
    private Contract completedContract;

    @BeforeEach
    void setUp() {
        owner = createUser("owner@rate.com", "ownerRate", Role.OWNER);
        walker = createUser("walker@rate.com", "walkerRate", Role.WALKER);
        completedContract = createCompletedContract(owner, walker);
    }

    // ========== Нормальный сценарий ==========
    @Test
    void createRating_Valid_Success() {
        RatingCreateDto dto = validDto(owner.getId(), walker.getId());
        RatingResponseDto response = ratingService.createRating(dto);
        assertThat(response.getStars()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Good");
    }

    // ========== Аномалии ==========
    @Test
    void createRating_StarsLessThan1_ThrowsValidationException() {
        RatingCreateDto dto = validDto(owner.getId(), walker.getId());
        dto.setStars(0);
        assertThrows(Exception.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_StarsMoreThan5_ThrowsValidationException() {
        RatingCreateDto dto = validDto(owner.getId(), walker.getId());
        dto.setStars(6);
        assertThrows(Exception.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_ContractNotCompleted_ThrowsBusinessLogicException() {
        Contract active = createContractWithStatus(owner, walker, ContractStatus.ACTIVE);
        RatingCreateDto dto = validDtoForContract(active, owner.getId(), walker.getId());
        assertThrows(BusinessLogicException.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_ContractCancelled_ThrowsBusinessLogicException() {
        Contract cancelled = createContractWithStatus(owner, walker, ContractStatus.CANCELLED);
        RatingCreateDto dto = validDtoForContract(cancelled, owner.getId(), walker.getId());
        assertThrows(BusinessLogicException.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_FromUserNotParticipant_ThrowsBusinessLogicException() {
        User stranger = createUser("stranger@x.com", "stranger", Role.OWNER);
        RatingCreateDto dto = validDto(stranger.getId(), walker.getId());
        assertThrows(BusinessLogicException.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_ToUserNotParticipant_ThrowsBusinessLogicException() {
        User stranger = createUser("stranger2@x.com", "stranger2", Role.WALKER);
        RatingCreateDto dto = validDto(owner.getId(), stranger.getId());
        assertThrows(BusinessLogicException.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_SelfRating_ThrowsBusinessLogicException() {
        RatingCreateDto dto = validDto(owner.getId(), owner.getId());
        assertThrows(BusinessLogicException.class, () -> ratingService.createRating(dto));
    }

    @Test
    void createRating_Duplicate_ThrowsBusinessLogicException() {
        ratingService.createRating(validDto(owner.getId(), walker.getId()));
        assertThrows(BusinessLogicException.class,
                () -> ratingService.createRating(validDto(owner.getId(), walker.getId())));
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

    private Contract createCompletedContract(User owner, User walker) {
        Order order = new Order();
        order.setOwner(owner);
        order.setDogBreed("Breed");
        order.setDogNeeds("Needs");
        order.setWalkDateTime(LocalDateTime.now().minusDays(1));
        order.setDurationMinutes(30);
        order.setMeetingPoint("Place");
        order.setPaymentAmount(BigDecimal.valueOf(200));
        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setWalker(walker);
        contract.setStatus(ContractStatus.COMPLETED);
        contract.setPrepaid(true);
        return contractRepository.save(contract);
    }

    private Contract createContractWithStatus(User owner, User walker, ContractStatus status) {
        Order order = new Order();
        order.setOwner(owner);
        order.setDogBreed("Other");
        order.setDogNeeds("Needs");
        order.setWalkDateTime(LocalDateTime.now().minusDays(2));
        order.setDurationMinutes(45);
        order.setMeetingPoint("Square");
        order.setPaymentAmount(BigDecimal.valueOf(150));
        order.setStatus(OrderStatus.COMPLETED);
        order = orderRepository.save(order);

        Contract contract = new Contract();
        contract.setOrder(order);
        contract.setWalker(walker);
        contract.setStatus(status);
        contract.setPrepaid(true);
        return contractRepository.save(contract);
    }

    private RatingCreateDto validDto(Long fromUserId, Long toUserId) {
        RatingCreateDto dto = new RatingCreateDto();
        dto.setContractId(completedContract.getId());
        dto.setFromUserId(fromUserId);
        dto.setToUserId(toUserId);
        dto.setStars(5);
        dto.setComment("Good");
        return dto;
    }

    private RatingCreateDto validDtoForContract(Contract contract, Long fromUserId, Long toUserId) {
        RatingCreateDto dto = new RatingCreateDto();
        dto.setContractId(contract.getId());
        dto.setFromUserId(fromUserId);
        dto.setToUserId(toUserId);
        dto.setStars(4);
        dto.setComment("OK");
        return dto;
    }
}
