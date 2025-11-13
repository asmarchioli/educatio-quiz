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
            model.addAttribute("senha", "");
            model.addAttribute("areas", areaService.listarTodas());
            model.addAttribute("tipoErro", "aluno");
            System.out.println("opa bl");
            return "cadastro";
        }
        

        try {
            alunoService.cadastrar(aluno);
            return "redirect:/login?cadastro=sucesso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("professor", new Professor());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("senha", "");
            model.addAttribute("areas", areaService.listarTodas());
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("tipoErro", "aluno");
            System.out.println("opa bl");
            return "cadastro";
        }
    }

    @PostMapping("/professor")
    public String cadastrarProfessor(
            @Valid @ModelAttribute("professor") Professor professor,
            BindingResult result,
            @RequestParam(required = false) List<Long> areasProfessor,
            Model model) {


        if (result.hasErrors()) {
            model.addAttribute("aluno", new Aluno());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());
            // model.addAttribute("tipoErro", "professor");
            return "cadastro";
        }

        // Verifica se pelo menos uma Área de Formação foi selecionada
        if (areasProfessor == null || areasProfessor.isEmpty()) {

            // Adiciona a mensagem de erro ao Model
            model.addAttribute("erro", "É obrigatório selecionar pelo menos uma Área de Formação");
            // model.addAttribute("tipoErro", "professor");
            model.addAttribute("aluno", new Aluno());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());

            return "cadastro";
        }

        try {
            professorService.cadastrar(professor, areasProfessor);
            System.out.println("chamalala");
            return "redirect:/login?cadastro=sucesso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("aluno", new Aluno());
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("areas", areaService.listarTodas());
            model.addAttribute("erro", e.getMessage());
            // model.addAttribute("tipoErro", "professor");
            System.out.println(e.getMessage());
            return "cadastro";
        }
    }
}