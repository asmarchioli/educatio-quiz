package br.uel.educatio.quiz.controller;

import org.springframework.stereotype.Controller; // <-- ADICIONADO
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // <-- ADICIONADO
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.service.QuizService;
import jakarta.validation.Valid;

@Controller // <-- ADICIONADO (ESSENCIAL)
@RequestMapping("/quiz") // <-- ADICIONADO (Define o prefixo da URL para esta classe)
public class QuizController {
    private final QuizService service;


    public QuizController(QuizService QuizService) {
        this.service = QuizService;
    }


    @GetMapping("/listar/quiz/areas/{id}")
    public String listarAreasDoQuiz(@PathVariable("id") long id_quiz, Model model){
        model.addAttribute("Areas", service.listarQuizAreas(id_quiz));
        return "pageFictícia";
    }

    @DeleteMapping("/{id}")
    public String deletarQuiz(@PathVariable("id") long id_quiz, RedirectAttributes ra) {
        try {
            service.deletar(id_quiz);
            ra.addFlashAttribute("mensagem", "Quiz deletado com sucesso!");
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @GetMapping("/listar")
    public String listar(Model model){
        model.addAttribute("Quizzes", service.listarTodos());
        return "pageFictícia";
    }

    @GetMapping("/buscar/pin")
    public String buscarPorPin(@RequestParam String pin, Model model, RedirectAttributes ra){
        try {
            model.addAttribute("Quizzes", service.buscarPorPin(pin));
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @GetMapping("/buscar/{id}")
    public String buscarPorId(@PathVariable("id") long id, Model model, RedirectAttributes ra){
        try {
            model.addAttribute("Quizzes", service.buscarPorId(id));
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @GetMapping("/buscar/ProfessorCriador/{id}")
    public String buscarPorIdProfessor(@PathVariable("id") long id_prof, Model model, RedirectAttributes ra){
        try {
            model.addAttribute("Quizzes", service.buscarPorProfessorCriador(id_prof));
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @PostMapping("salvar")
    public String criar(@Valid @ModelAttribute Quiz quiz, Model model, RedirectAttributes ra){
        try {
            service.criar(quiz);
            ra.addFlashAttribute("mensagem", "Quiz criado com sucesso!");
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", "Houve algum erro na criação do quiz!");
            return "redirect:/ficticio";
        }
    }
}