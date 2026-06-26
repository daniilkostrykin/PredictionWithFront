package org.example.prediction.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {
    @GetMapping("/")
    public String homePage() {
        log.debug("Отображение главной страницы");
        return "index";
    }
    @GetMapping("/boom")
    public String simulateError() {
        // Деление на ноль или явный выброс исключения
        throw new RuntimeException("Это тестовая авария! Проверка Error Page.");
    }
}
