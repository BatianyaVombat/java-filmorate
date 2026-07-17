package ru.yandex.practicum.filmorate.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice //Все ответы из этого класса должны быть в JSON
@Slf4j
public class GlobalExceptionHandler {

    // фиксируем ошибки валидации, подключается когда что-то идёт не так и выдаёт расшифровку в консоль
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @SuppressWarnings("unused")
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                details.add(fieldError.getDefaultMessage())
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "Ошибка валидации");
        response.put("details", details);

        log.warn("Ошибка валидации: {}", details);

        return response;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
