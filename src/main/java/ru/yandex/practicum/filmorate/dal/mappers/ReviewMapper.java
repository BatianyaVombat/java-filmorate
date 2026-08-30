package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.reviews.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.reviews.ReviewResponse;
import ru.yandex.practicum.filmorate.dto.reviews.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

@UtilityClass
public class ReviewMapper {
    public Review toEntity(NewReviewRequest request) {
        return Review.builder()
                .id(request.getId())
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .build();
    }

    public Review toEntity(UpdateReviewRequest request) {
        return Review.builder()
                .id(request.getReviewId())
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .build();
    }

    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }
}
