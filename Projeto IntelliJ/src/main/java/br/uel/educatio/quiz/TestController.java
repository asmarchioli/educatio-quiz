package br.uel.educatio.quiz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public Map<String, String> testConnection() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conexão com o backend Java Spring bem-sucedida!");
        return response;
    }
}