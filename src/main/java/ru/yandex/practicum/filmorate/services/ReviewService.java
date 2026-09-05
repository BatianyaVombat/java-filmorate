package ru.yandex.practicum.filmorate.services;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.dto.reviews.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.reviews.ReviewResponse;
import ru.yandex.practicum.filmorate.dto.reviews.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final EventService eventService;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                         FilmRepository filmRepository, EventService eventService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.eventService = eventService;
    }

    public ReviewResponse addNewReview(NewReviewRequest request) {
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + request.getUserId() + " не найден"));
        filmRepository.findById(request.getFilmId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getFilmId() + " не найден"));

        ReviewResponse response = ReviewMapper.toResponse(reviewRepository.saveReview(ReviewMapper
                .toEntity(request)).orElseThrow());
        eventService.addEvent(response.getUserId(), EventType.REVIEW, EventOperation.ADD, response.getReviewId());

        return response;
    }

    public ReviewResponse updateReview(UpdateReviewRequest request) {
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + request.getUserId() + " не найден"));
        filmRepository.findById(request.getFilmId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getFilmId() + " не найден"));

        ReviewResponse response = ReviewMapper.toResponse(
                reviewRepository.updateReview(ReviewMapper.toEntity(request)).orElseThrow());
        eventService.addEvent(response.getUserId(), EventType.REVIEW, EventOperation.UPDATE, response.getReviewId());

        return response;
    }

    public void removeReview(Long id) {
        Review review = reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));

        reviewRepository.removeReview(id);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, EventOperation.REMOVE, id);
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
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.saveLike(id, userId);
        return ReviewMapper.toResponse(reviewRepository.getReview(id).orElseThrow());
    }

    public ReviewResponse addDislike(Long id, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.saveDislike(id, userId);
        return ReviewMapper.toResponse(reviewRepository.getReview(id).orElseThrow());
    }

    public ReviewResponse removeLike(Long id, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.deleteLike(id, userId);
        return ReviewMapper.toResponse(reviewRepository.getReview(id).orElseThrow());
    }

    public ReviewResponse removeDislike(Long id, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        reviewRepository.getReview(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + id + " не найден."));
        reviewRepository.deleteDislike(id, userId);
        return ReviewMapper.toResponse(reviewRepository.getReview(id).orElseThrow());
    }

    public void removeReviewsByUserId(Long userId) {
        reviewRepository.removeReviewsByUserId(userId);
    }

    public void removeReviewLikesByUserId(Long userId) {
        reviewRepository.deleteLikesByUserId(userId);
    }

    public void removeReviewDislikesByUserId(Long userId) {
        reviewRepository.deleteDislikesByUserId(userId);
    }

    public void removeReviewByFilmId(Long filmId) {
        getReviewsByFilmId(filmId, 0L).forEach(r -> removeReview(r.getReviewId()));
    }
}
