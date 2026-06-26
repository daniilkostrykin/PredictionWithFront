package org.example.prediction.web;

import org.springframework.dao.OptimisticLockingFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.example.prediction.models.exceptions.EventNotFoundException;
import org.hibernate.StaleObjectStateException;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StaleObjectStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleStaleObjectState(StaleObjectStateException ex, Model model) {
        log.warn("Объект был изменен другим процессом: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Конфликт одновременного доступа");
        model.addAttribute("errorMessage", "Данные были изменены другим пользователем. Пожалуйста, попробуйте выполнить операцию снова.");
        model.addAttribute("errorCode", "409");
        return "error/custom-error";
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOptimisticLock(OptimisticLockingFailureException ex, Model model) {
        log.warn("Ошибка оптимистичной блокировки: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Конфликт одновременного доступа");
        model.addAttribute("errorMessage", "Данные были изменены другим процессом. Пожалуйста, попробуйте выполнить операцию снова.");
        model.addAttribute("errorCode", "409");
        return "error/custom-error";
    }

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEventNotFound(EventNotFoundException ex, Model model) {
        log.warn("Событие не найдено: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Событие не найдено");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "404");
        return "error/custom-error";
    }
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorTitle", "Файл не найден");
        model.addAttribute("errorMessage", "Запрошенный ресурс отсутствует.");
        return "error/custom-error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.warn("Некорректные данные: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Некорректные данные");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "400");
        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Внутренняя ошибка сервера", ex);
        model.addAttribute("errorTitle", "Внутренняя ошибка сервера");
        model.addAttribute("errorMessage", "Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.");
        model.addAttribute("errorCode", "500");
        return "error/custom-error";
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationException(org.springframework.security.core.AuthenticationException ex, Model model) {
        log.error("Ошибка аутентификации", ex);
        model.addAttribute("errorTitle", "Ошибка аутентификации");
        model.addAttribute("errorMessage", "Ошибка при попытке аутентификации: " + ex.getMessage());
        model.addAttribute("errorCode", "401");
        return "error/custom-error";
    }
}
