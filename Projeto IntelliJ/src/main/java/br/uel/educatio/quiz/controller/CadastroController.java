package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.service.AlunoService;
import br.uel.educatio.quiz.service.AreaService;
import br.uel.educatio.quiz.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private AreaService areaService;

    @GetMapping
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("aluno", new Aluno());
        model.addAttribute("professor", new Professor());
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("areas", areaService.listarTodas());
        return "cadastro";
    }

    @PostMapping("/aluno")
    public String cadastrarAluno(
            @Valid @ModelAttribute("aluno") Aluno aluno,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("professor", new Professor());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());
            model.addAttribute("tipoErro", "aluno");
            return "cadastro";
        }

        try {
            alunoService.cadastrar(aluno);
            return "redirect:/login?cadastro=sucesso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("professor", new Professor());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());
            result.rejectValue("email", "error.aluno", e.getMessage());
            model.addAttribute("tipoErro", "aluno");
            return "cadastro";
        }
    }

    @PostMapping("/professor")
    public String cadastrarProfessor(
            @Valid @ModelAttribute("professor") Professor professor,
            @RequestParam(required = false) List<Long> areasAdicionais,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("aluno", new Aluno());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());
            model.addAttribute("tipoErro", "professor");
            return "cadastro";
        }

        try {
            professorService.cadastrar(professor, areasAdicionais);
            return "redirect:/login?cadastro=sucesso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("aluno", new Aluno());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());
            result.rejectValue("email", "error.professor", e.getMessage());
            model.addAttribute("tipoErro", "professor");
            return "cadastro";
        }
    }
}