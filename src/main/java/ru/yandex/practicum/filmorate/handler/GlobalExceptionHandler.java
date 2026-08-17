package ru.yandex.practicum.filmorate.handler;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestControllerAdvice //Все ответы из этого класса должны быть в JSON
@Slf4j
public class GlobalExceptionHandler {

    // фиксируем ошибки валидации, подключается когда что-то идёт не так и выдаёт расшифровку в консоль
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @SuppressWarnings("unused")
    public Map<String, Object> handleValidation(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());

        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @SuppressWarnings("unused")
    public Map<String, String> handleNotFound(NotFoundException e) {
        log.error("Не смогли найти данные на сервере", e);

        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @SuppressWarnings("unused")
    public Map<String, String> handleGeneralException(Exception e) {
        log.error("Внутренняя ошибка сервера: ", e);

        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")
    public Map<String, String> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("Дубликат: {}", e.getMessage());
        return Map.of("error", "Такой объект уже существует");
    }

    @ExceptionHandler(InternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @SuppressWarnings("unused")
    public Map<String, String> handleInternal(InternalException e) {
        log.error("Внутренняя ошибка: {}", e.getMessage(), e);
        return Map.of("error", "Внутренняя ошибка сервера");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")
    public Map<String, Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> errors.add(fe.getDefaultMessage()));
        log.warn("Ошибка валидации DTO: {}", errors);
        return Map.of("error", "Ошибка валидации", "details", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")
    public Map<String, String> handleJsonError(HttpMessageNotReadableException e) {
        return Map.of("error", "Некорректное тело запроса");
    }
}
