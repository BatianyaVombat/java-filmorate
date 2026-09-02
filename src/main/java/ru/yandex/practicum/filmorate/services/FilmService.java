package ru.yandex.practicum.filmorate.services;

import jakarta.validation.ValidationException;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dto.directors.DirectorResponse;
import ru.yandex.practicum.filmorate.dto.film.FilmResponse;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.genres.GenreIdRequest;
import ru.yandex.practicum.filmorate.dto.genres.GenreResponse;
import ru.yandex.practicum.filmorate.dto.mpa.MpaResponse;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final UserService userService;
    private final ReviewService reviewService;
    private final EventService eventService;

    @Autowired
    public FilmService(FilmRepository filmRepository, MpaRepository mpaRepository,
                       GenreRepository genreRepository, DirectorRepository directorRepository,
                       UserService userService, ReviewService reviewService, EventService eventService) {
        this.filmRepository = filmRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
        this.userService = userService;
        this.reviewService = reviewService;
        this.eventService = eventService;
    }

    public List<FilmResponse> getAllFilms() {
        return filmRepository.findAll().stream()
                .map(this::toFilmResponse)
                .collect(Collectors.toList());
    }

    public FilmResponse addNewFilm(NewFilmRequest request) {
        if (request.getMpa() == null) {
            throw new ValidationException("MPA не должен быть пустым");
        }

        Long mpaId = request.getMpa().getId();

        mpaRepository.findById(mpaId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + mpaId + " не найден"));

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            request.getGenres().forEach(genreIdRequest -> genreRepository.findById(genreIdRequest.getId())
                    .orElseThrow(() ->
                            new NotFoundException("Жанр с id = " + genreIdRequest.getId() + " не найден")));
        }

        Film saved = filmRepository.saveFilm(FilmMapper.toEntity(request));

        return toFilmResponse(saved);
    }

    public FilmResponse updateFilm(Long id, UpdateFilmRequest request) {
        if (request == null || request.getId() == null) {
            throw new ValidationException("Id фильма обязателен");
        }

        Film oldFilm = getFilmOrThrow(id);

        String finalName = request.getName() != null
                ? request.getName() : oldFilm.getName();
        String finalDescription = request.getDescription() != null
                ? request.getDescription() : oldFilm.getDescription();
        LocalDate finalReleaseDate = request.getReleaseDate() != null
                ? request.getReleaseDate() : oldFilm.getReleaseDate();
        Long finalDuration = request.getDuration() != null
                ? request.getDuration() : oldFilm.getDuration();
        Long finalMpaId = request.getMpa() != null
                ? request.getMpa().getId() : oldFilm.getMpaId();
        Long finalDirector = request.getDirectors() != null
                ? request.getDirectors().getFirst().getId()
                : oldFilm.getDirectorId();

        Set<Long> finalGenreIds = request.getGenres() != null
                ? request.getGenres().stream()
                  .map(GenreIdRequest::getId)
                  .collect(Collectors.toSet()) : oldFilm.getGenresIds();

        Film updatedFilm = Film.builder()
                .id(oldFilm.getId())
                .name(finalName)
                .description(finalDescription)
                .releaseDate(finalReleaseDate)
                .duration(finalDuration)
                .mpaId(finalMpaId)
                .genresIds(finalGenreIds)
                .directorId(finalDirector)
                .build();

        filmRepository.updateFilm(updatedFilm);

        Film savedFilm = filmRepository.findById(updatedFilm.getId())
                .orElseThrow(() -> new InternalException("Не удалось найти фильм после обновления"));

        return toFilmResponse(savedFilm);
    }

    public void addLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.getUserById(userId);

        try {
            filmRepository.addLike(filmId, userId);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Пользователь уже ставил лайк этому фильму");
        }

        eventService.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getFilmOrThrow(filmId);
        userService.getUserById(userId);

        filmRepository.removeLike(filmId, userId);

        eventService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }

    public List<Film> getPopularFilmList(Long count) {
        return filmRepository.getPopularFilms(count);
    }

    public List<FilmResponse> getSortedFilms(String sortBy, Long directorId) {
        directorRepository.findById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + directorId + " не найден"));

        List<Film> films = filmRepository.getFilmsByDirectorSorted(sortBy, directorId);

        return films.stream()
                .map(this::toFilmResponse)
                .collect(Collectors.toList());
    }

    private FilmResponse toFilmResponse(Film film) {
        MpaResponse mpa = mpaRepository.findById(film.getMpaId())
                .orElseThrow(() -> new NotFoundException("Рейтинг не найден"));

        List<GenreResponse> genres = genreRepository.findAllById(film.getGenresIds());

        List<DirectorResponse> directors = new ArrayList<>();
        if (film.getDirectorId() != null) {
            Optional<Director> director = directorRepository.findById(film.getDirectorId());

            if (director.isPresent()) {
                DirectorResponse dr = DirectorMapper.toResponse(director.get());
                directors.add(dr);
            }
        }

        return FilmMapper.toResponse(film, mpa, genres, directors);
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmRepository.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    public FilmResponse getFilmById(Long id) {
        Film film = getFilmOrThrow(id);
        return toFilmResponse(film);
    }

    public List<FilmResponse> getCommonFilms(Long userId, Long friendId) {
        userService.getUserById(userId);
        userService.getUserById(friendId);

        return filmRepository.getCommonFilms(userId, friendId).stream()
                .map(this::toFilmResponse)
                .collect(Collectors.toList());
    }

    public List<FilmResponse> getRecommendations(Long userId) {
        userService.getUserById(userId);

        Map<Long, Set<Long>> allLikes = filmRepository.getAllUserLikes();
        Set<Long> targetLikes = allLikes.getOrDefault(userId, Set.of());

        Long bestUserId = null;
        int bestIntersection = 0;

        for (Map.Entry<Long, Set<Long>> entry : allLikes.entrySet()) {
            if (entry.getKey().equals(userId)) continue;
            Set<Long> intersection = new HashSet<>(entry.getValue());
            intersection.retainAll(targetLikes);
            if (intersection.size() > bestIntersection) {
                bestIntersection = intersection.size();
                bestUserId = entry.getKey();
            }
        }

        if (bestUserId == null) {
            return List.of(); // ни у кого нет пересечений — рекомендаций нет, это не ошибка
        }

        Set<Long> recommendedIds = new HashSet<>(allLikes.get(bestUserId));
        recommendedIds.removeAll(targetLikes);

        return recommendedIds.stream()
                .map(this::getFilmById)
                .collect(Collectors.toList());
    }

    public void removeFilm(Long filmId) {
        getFilmOrThrow(filmId);
        reviewService.removeReviewByFilmId(filmId);
        filmRepository.removeLikesByFilmId(filmId);
        filmRepository.removeFilmGenresByFilmId(filmId);
        filmRepository.removeFilm(filmId);
    }
}
