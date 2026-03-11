package com.project.dogwalking.repository;

import com.project.dogwalking.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // все оценки, полученные пользователем (кому поставили)
    List<Rating> findByToUserId(Long toUserId);
    // все оценки, поставленные пользователем
    List<Rating> findByFromUserId(Long fromUserId);
    // проверить, оставлял ли пользователь уже оценку по данному договору
    boolean existsByFromUserIdAndContractId(Long fromUserId, Long contractId);

    // средний рейтинг пользователя
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.toUser.id = :userId")
    Double getAverageRatingForUser(@Param("userId") Long userId);
}