package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validator.AfterValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AfterValidator.class)
@Target(ElementType.FIELD)//только поля
@Retention(RetentionPolicy.RUNTIME)//доступна во время работы приложения
public @interface After {
    String message() default "Дата должна быть позже указанной";

    String value();

    //Поля обязательное по стандарту для кастомных аннотаций
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
