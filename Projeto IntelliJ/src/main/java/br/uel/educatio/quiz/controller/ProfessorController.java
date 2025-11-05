package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.service.ProfessorService;
import br.uel.educatio.quiz.service.QuizService;
import jakarta.servlet.http.HttpSession; // Importação chave do Arquivo 1
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller; // Anotação chave do Arquivo 1
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // <-- Anotação essencial (do Arquivo 1)
@RequestMapping("/professor")
public class ProfessorController {
    private final ProfessorService profService;
    private final QuizService quizService;


    public ProfessorController(ProfessorService professorService, QuizService quizService) {
        this.profService = professorService;
        this.quizService = quizService;
    }

    // --- Método essencial (só existe no Arquivo 1) ---
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        model.addAttribute("professor", professor);
        return "professor/home";

    }
    @GetMapping("/criar_quiz")
    public String criar_quiz(HttpSession session, Model model) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        if (professor == null) {
            return "redirect:/login";
        }

        model.addAttribute("professor", professor);
        return "professor/criar_quiz";
    }

    /////////////////////////////////////////
    // --- Métodos idênticos (comuns a ambos) ---

    @GetMapping("/listar")
    public String listarTodos (Model model){
        model.addAttribute("professores", profService.listarTodos());
        return "pageFictícia";
    }

    @GetMapping("/buscar")
    public String buscar (@RequestParam("id") long id_prof, Model model){
        model.addAttribute("professor", profService.buscarPorId(id_prof));
        return "pageFictícia";
    }

    @PostMapping("salvar")
    public String salvarProfessor(@Valid @ModelAttribute Professor prof, Model model, RedirectAttributes ra){
        try {
            profService.salvar(prof);
            ra.addFlashAttribute("mensagem", "Professor salvo!");
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @PatchMapping("/editar/{id}")
    public String atualizarPerfilProfessor(@PathVariable("id") long id, @Valid @ModelAttribute Professor prof_atualizado, Model model, RedirectAttributes ra) {
        try {
            profService.atualizarPerfilProfessor(id, prof_atualizado);
            ra.addFlashAttribute("mensagem", "Perfil Atualizado!");
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @DeleteMapping("/{id}")
    public String deletarProfessor(@PathVariable("id") long id_prof, RedirectAttributes ra) {
        try {
            profService.deletarPorId(id_prof);
            ra.addFlashAttribute("mensagem", "Professor deletado com sucesso!");
            return "ficticio"; //"form"

        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @PostMapping("/adicionarArea")
    public String adicionarArea(@RequestParam("id_area") long id_area, @RequestParam("id_prof") long id_prof, RedirectAttributes ra) {
        try {
            profService.adicionarAreaProfessor(id_area, id_prof);
            ra.addFlashAttribute("mensagem", "Área adicionada com sucesso!");
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", "Não foi possível adicionar a área!");
            return "redirect:/ficticio";
        }
    }

    @DeleteMapping("/removerArea")
    public String removerArea(@RequestParam("id_area") long id_area, @RequestParam("id_prof") long id_prof, RedirectAttributes ra) {
        try {
            profService.removerAreaProfessor(id_prof, id_area);
            ra.addFlashAttribute("mensagem", "Área removida com sucesso!");
            return "ficticio"; //"form"

        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @GetMapping("/listar/professor/areas/{id_prof}")
    public String listarAreasDoProfessor(@PathVariable("id_prof") long id_prof, Model model){
        model.addAttribute("professorAreas", profService.listarAreasPorProfessor(id_prof));
        return "pageFictícia";
    }

    @GetMapping("/listar/areas/professores/{id_area}")
    public String listarProfsPorArea(@PathVariable("id_area") long id_area, Model model){
        model.addAttribute("profQuizzes", profService.listarProfessoresPorArea(id_area));
        return "pageFictícia";
    }

    @GetMapping("/listar/quizzes/{id_prof}")
    public String listarQuizzesCriados(@PathVariable("id_prof") long id_prof, Model model, RedirectAttributes ra){
        try {
            model.addAttribute("profQuizzes", quizService.getQuizzesByProfessor(id_prof));
            return "ficticio"; //"form"

        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }
}