package ru.yandex.practicum.filmorate.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component //связка со Spring
@Slf4j //для ведения журнала
public class LoggingAspect {

    //адресно слушаем пакет с контроллерами
    /* любой метод, в пакете - com.yourproject.controller..* и всех его подпакетах (..)
       .*(..) - любой метод с любыми аргументами
    */
    @Pointcut("execution(* ru.yandex.practicum.filmorate.controller..*.*(..))") //фильтр строка
    @SuppressWarnings("unused") //отпугиваем warning
    public void allControllerMethods() {
    }

    //вызывается перед вызовом метода и фиксирует входные данные
    @Before("allControllerMethods()")
    @SuppressWarnings("unused")
    public void entryLogging(JoinPoint joinPoint) {
        //JoinPoint - объект Spring, который содержит детали вызова

        String methodName = joinPoint.getSignature().getName(); //имя метода
        Object[] arguments = joinPoint.getArgs(); //аргументы метода

        log.info("Вызван метод: {} | Аргументы: {}", methodName, Arrays.toString(arguments));
    }

    //вызывается после удачного завершения метода
    @AfterReturning(pointcut = "allControllerMethods()", returning = "результат")
    @SuppressWarnings("unused")
    public void exitLogging(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("Метод: {} успешно завершился. Результат: {}", methodName, result);
    }

    //вызывается если метод выбросил ошибку
    @AfterThrowing(pointcut = "allControllerMethods()", throwing = "ошибка")
    @SuppressWarnings("unused")
    public void exceptionLogging(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        log.error("Метод: {} завершился с ошибкой: {}", methodName, ex.getMessage());
    }
}
