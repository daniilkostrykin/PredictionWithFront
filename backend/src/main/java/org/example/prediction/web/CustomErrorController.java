package org.example.prediction.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = status != null ? Integer.parseInt(status.toString()) : 500;

        request.setAttribute("errorCode", code);
        request.setAttribute("errorTitle", switch (code) {
            case 404 -> "Страница не найдена";
            case 403 -> "Доступ запрещён";
            default -> "Ошибка";
        });
        request.setAttribute("errorMessage", switch (code) {
            case 404 -> "Такой страницы не существует";
            case 403 -> "У вас нет прав для доступа";
            default -> "Произошла ошибка";
        });

        return "error/custom-error";
    }
}


