package com.project.dogwalking.service;

import com.project.dogwalking.dto.RatingCreateDto;
import com.project.dogwalking.dto.RatingResponseDto;
import com.project.dogwalking.entity.Contract;
import com.project.dogwalking.entity.Rating;
import com.project.dogwalking.entity.User;
import com.project.dogwalking.entity.enums.ContractStatus;
import com.project.dogwalking.exception.BusinessLogicException;
import com.project.dogwalking.exception.ResourceNotFoundException;
import com.project.dogwalking.repository.ContractRepository;
import com.project.dogwalking.repository.RatingRepository;
import com.project.dogwalking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final UserService userService; // для обновления рейтинга

    @Transactional
    public RatingResponseDto createRating(RatingCreateDto dto) {
        // договор существует и завершён
        Contract contract = contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + dto.getContractId()));

        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BusinessLogicException("Cannot rate an incomplete contract");
        }

        boolean isFromUserValid = contract.getOrder().getOwner().getId().equals(dto.getFromUserId()) ||
                contract.getWalker().getId().equals(dto.getFromUserId());
        if (!isFromUserValid) {
            throw new BusinessLogicException("You are not a participant of this contract");
        }

        boolean isToUserValid = contract.getOrder().getOwner().getId().equals(dto.getToUserId()) ||
                contract.getWalker().getId().equals(dto.getToUserId());
        if (!isToUserValid) {
            throw new BusinessLogicException("The rated user is not a participant of this contract");
        }

        if (dto.getFromUserId().equals(dto.getToUserId())) {
            throw new BusinessLogicException("You cannot rate yourself");
        }

        if (ratingRepository.existsByFromUserIdAndContractId(dto.getFromUserId(), dto.getContractId())) {
            throw new BusinessLogicException("You have already rated this contract");
        }


        Rating rating = new Rating();
        rating.setFromUser(userService.findUserEntityById(dto.getFromUserId()));
        rating.setToUser(userService.findUserEntityById(dto.getToUserId()));
        rating.setContract(contract);
        rating.setStars(dto.getStars());
        rating.setComment(dto.getComment());

        rating = ratingRepository.save(rating);

        // считаем рейтинг
        updateUserRating(dto.getToUserId());

        return mapToDto(rating);
    }

    @Transactional
    public void updateUserRating(Long userId) {
        Double avgRating = ratingRepository.getAverageRatingForUser(userId);
        User user = userService.findUserEntityById(userId);
        if (avgRating != null) {
            user.setRating(avgRating);
        } else {
            user.setRating(0.0);
        }
        userRepository.save(user);
    }

    private RatingResponseDto mapToDto(Rating rating) {
        RatingResponseDto dto = new RatingResponseDto();
        dto.setId(rating.getId());
        dto.setFromUserId(rating.getFromUser().getId());
        dto.setToUserId(rating.getToUser().getId());
        dto.setContractId(rating.getContract().getId());
        dto.setStars(rating.getStars());
        dto.setComment(rating.getComment());
        dto.setCreatedAt(rating.getCreatedAt());
        return dto;
    }
}