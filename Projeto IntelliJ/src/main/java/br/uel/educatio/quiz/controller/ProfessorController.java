package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Area;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.service.ProfessorService;
import br.uel.educatio.quiz.service.QuizService;
import br.uel.educatio.quiz.service.AreaService;
import jakarta.servlet.http.HttpSession; // Importação chave do Arquivo 1
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller; // Anotação chave do Arquivo 1
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Exibicao;

import java.util.List;


@Controller // <-- Anotação essencial (do Arquivo 1)
@RequestMapping("/professor")
public class ProfessorController {
    private final ProfessorService profService;
    private final QuizService quizService;
    private final AreaService areaService;


    public ProfessorController(ProfessorService professorService, QuizService quizService, AreaService areaService) {
        this.profService = professorService;
        this.quizService = quizService;
        this.areaService = areaService;
    }

    @GetMapping("/home")
    public String home(@RequestAttribute("professor") Professor professorLogado, HttpSession session, Model model) {

        model.addAttribute("professor", professorLogado);
        return "professor/home";
    }
}