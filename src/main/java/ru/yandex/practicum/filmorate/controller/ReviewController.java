package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.reviews.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.reviews.ReviewResponse;
import ru.yandex.practicum.filmorate.dto.reviews.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.services.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewResponse createReview(@Valid @RequestBody NewReviewRequest request) {
        return reviewService.addNewReview(request);
    }

    @PutMapping
    public ReviewResponse updateReview(@Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(request);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable("reviewId") Long id) {
        reviewService.removeReview(id);
    }

    @GetMapping("/{reviewId}")
    public ReviewResponse getReviewById(@PathVariable("reviewId") Long id) {
        return reviewService.getReview(id);
    }

    @GetMapping
    public Collection<ReviewResponse> getReviewsByFilmId(@RequestParam(defaultValue = "0") Long filmId,
                                                         @RequestParam(defaultValue = "10") Long count) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public ReviewResponse addLike(@RequestBody(required = false) UpdateReviewRequest request,
                                  @PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        if (request != null) reviewService.updateReview(request);
        return reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ReviewResponse addDislike(@RequestBody(required = false) UpdateReviewRequest request,
                                     @PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        if (request != null) reviewService.updateReview(request);
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ReviewResponse removeLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ReviewResponse removeDislike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewService.removeDislike(id, userId);
    }
}
