package ru.yandex.practicum.filmorate.dal;

import com.sun.jdi.InternalException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private static final String INSERT_REVIEW_QUERY = "INSERT INTO Reviews (content, isPositive, user_id, " +
            "film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_REVIEW_BY_ID_QUERY = "SELECT * FROM Reviews WHERE id = ?";
    private static final String UPDATE_REVIEW_QUERY = "UPDATE Reviews SET content = ?, isPositive = ?, " +
            "user_id = ?, film_id = ?, useful = ? WHERE id = ?";
    private static final String DELETE_REVIEW_BY_ID_QUERY = "DELETE FROM Reviews WHERE id = ?";
    private static final String FIND_REVIEWS_LIMIT_QUERY = "SELECT * FROM Reviews ORDER BY useful DESC LIMIT ?";
    private static final String FIND_REVIEWS_BY_FILMID_LIMIT_QUERY = "SELECT * FROM Reviews " +
            "WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO Review_Likes (review_id, user_id) VALUES (?, ?)";
    private static final String INSERT_DISLIKE_QUERY = "INSERT INTO Review_Dislikes (review_id, user_id) VALUES (?, ?)";
    private static final String DISLIKE_DECREMENT = "UPDATE Reviews SET useful = useful - 1 WHERE id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM Review_Likes WHERE review_id = ? AND user_id = ?";
    private static final String DELETE_DISLIKE_QUERY = "DELETE FROM Review_Dislikes WHERE review_id = ? AND user_id = ?";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Review> saveReview(Review review) {
        return findOne(FIND_REVIEW_BY_ID_QUERY,
                insert(INSERT_REVIEW_QUERY,
                        review.getContent(),
                        review.getIsPositive(),
                        review.getUserId(),
                        review.getFilmId(),
                        review.getUseful()
                )
        );
    }

    public Optional<Review> updateReview(Review review) {
        update(UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful(),
                review.getId()
        );
        return findOne(FIND_REVIEW_BY_ID_QUERY, review.getId());
    }

    public void removeReview(Long id) {
        delete(DELETE_REVIEW_BY_ID_QUERY, id);
    }

    public Optional<Review> getReview(Long id) {
        return findOne(FIND_REVIEW_BY_ID_QUERY, id);
    }

    public Collection<Review> getAllReviews(Long count) {
        return findMany(FIND_REVIEWS_LIMIT_QUERY, count);
    }

    public Collection<Review> getReviewsByFilmId(Long filmId, Long count) {
        return findMany(FIND_REVIEWS_BY_FILMID_LIMIT_QUERY, filmId, count);
    }

    public void saveLike(Long id, Long userId) {
        int rowsUpdate = jdbc.update(INSERT_LIKE_QUERY, id, userId);
        if (rowsUpdate == 0) {
            throw new InternalException("Не удалось сохранить данные.");
        }
    }

    public void saveDislike(Long id, Long userId) {
        int rowsUpdate = jdbc.update(INSERT_DISLIKE_QUERY, id, userId);
        if (rowsUpdate == 0) {
            throw new InternalException("Не удалось сохранить данные.");
        }

        jdbc.update(DISLIKE_DECREMENT, id); //счётчик useful в таблице Reviews должен обновляться
    }

    public void deleteLike(Long id, Long userId) {
        delete(DELETE_LIKE_QUERY, id, userId);
    }

    public void deleteDislike(Long id, Long userId) {
        delete(DELETE_DISLIKE_QUERY, id, userId);

    }
}
