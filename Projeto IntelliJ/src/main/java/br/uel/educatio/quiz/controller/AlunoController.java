package br.uel.educatio.quiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/aluno")
public class AlunoController {
    private final AlunoService service;

    @Autowired
    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping("/buscarQuizzes")
    public void BuscarHistoricoQuizzes(Long id_aluno){
        model.attribute("historicoQuizzes", service.buscarRespostasQuiz(id_aluno, id_quiz));
        return "page";

    }


    @GetMapping("/buscarRespostas")
    public void BuscarRespostasQuiz(long id_quiz, long id_aluno){
        model.attribute("historicoQuizzes", service.buscarRespostasQuiz(id_aluno, id_quiz));
        return "page";
    }

}
