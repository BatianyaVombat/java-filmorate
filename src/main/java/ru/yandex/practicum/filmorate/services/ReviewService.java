package ru.yandex.practicum.filmorate.services;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.dto.reviews.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.reviews.ReviewResponse;
import ru.yandex.practicum.filmorate.dto.reviews.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewService(ReviewRepository reviewRepository, UserService userService, FilmService filmService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.filmService = filmService;
    }

    public ReviewResponse addNewReview(NewReviewRequest request) {
        userService.getUserById(request.getUserId());
        filmService.getFilmById(request.getFilmId());
        return ReviewMapper.toResponse(reviewRepository.saveReview(ReviewMapper.toEntity(request)).orElseThrow());
    }

    public ReviewResponse updateReview(UpdateReviewRequest request) {
        userService.getUserById(request.getUserId());
        filmService.getFilmById(request.getFilmId());
        return ReviewMapper.toResponse(reviewRepository.updateReview(ReviewMapper.toEntity(request)).orElseThrow());
    }

    public void removeReview(Long id) {
        reviewRepository.removeReview(id);
    }

    public ReviewResponse getReview(Long id) {
        return ReviewMapper.toResponse(reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден.")));
    }

    public Collection<ReviewResponse> getReviewsByFilmId(Long filmId, Long count) {
        if (filmId == 0) {
            return reviewRepository.getAllReviews(count).stream().map(ReviewMapper::toResponse).toList();
        } else {
            return reviewRepository.getReviewsByFilmId(filmId, count).stream().map(ReviewMapper::toResponse).toList();
        }
    }

    public ReviewResponse addLike(Long id, Long userId) {
        userService.getUserById(userId);
        Review review = reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.saveLike(id, userId);
        return ReviewMapper.toResponse(review);
    }

    public ReviewResponse addDislike(Long id, Long userId) {
        userService.getUserById(userId);
        Review review = reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.saveDislike(id, userId);
        return ReviewMapper.toResponse(review);
    }

    public ReviewResponse removeLike(Long id, Long userId) {
        userService.getUserById(userId);
        Review review = reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.deleteLike(id, userId);
        return ReviewMapper.toResponse(review);
    }

    public ReviewResponse removeDislike(Long id, Long userId) {
        userService.getUserById(userId);
        Review review = reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.deleteDislike(id, userId);
        return ReviewMapper.toResponse(review);
    }
}
