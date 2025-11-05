package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.service.QuestaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/questao")
public class QuestaoController {
    private final QuestaoService service;

    public QuestaoController(QuestaoService service){
        this.service = service;
    }

    @GetMapping("/listarPorProfessor/{id}")
    public String listarQuestoesPorProfessor(
            @PathVariable("id") long id, // Note que mudei o nome da variável local para 'id'
            Model model
    ) {
        model.addAttribute("questoes", service.listarQuestoesPorProf(id));
        return "professor/banco_questoes";
    }

    @GetMapping("/listar")
    public String listarTodasQuestoes(Model model) {
        model.addAttribute("listaQuestoes", service.listarQuestoes());
        return "paginaficticia";
    }

    @GetMapping("/buscar/{id}")
    public String buscarQuestao(@PathVariable("id") long id_questao, Model model, RedirectAttributes ra) {
        try{
            Questao questao = service.buscarQuestao(id_questao);
            model.addAttribute("questao", questao);
            return "pagefictícia";
        } catch(Exception e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    


}

