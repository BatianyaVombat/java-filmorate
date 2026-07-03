package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotation.After;

import java.time.LocalDate;

//описываю логику своей кастомной аннотации
public class AfterValidator implements ConstraintValidator<After, LocalDate> {
    private LocalDate minimalDate;

    //забираем пороговую дату из аннотации
    @Override
    public void initialize(After annotation) {
        this.minimalDate = LocalDate.parse(annotation.value());
    }

    //проверяем releaseDate
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) return true;

        return !date.isBefore(minimalDate);
    }
}
