package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.service.AlunoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");

        if (aluno == null) {
            return "redirect:/login";
        }

        model.addAttribute("aluno", aluno);
        return "aluno/home";
    }

    @GetMapping("/buscarQuizzes")
    public String BuscarHistoricoQuizzes(Long id_aluno, Model model){
        model.addAttribute("historicoQuizzes", service.buscarHistoricoQuizzes(id_aluno));
        return "pageFicticia";
    }

    @GetMapping("/historicoQuiz")
    public String BuscarRespostasQuiz(@RequestParam("id_aluno") long id_aluno, @RequestParam("id_quiz") long id_quiz, Model model){
        model.addAttribute("historicoQuizzes", service.buscarRespostasQuiz(id_aluno, id_quiz));
        return "pageFicticia";
    }
}