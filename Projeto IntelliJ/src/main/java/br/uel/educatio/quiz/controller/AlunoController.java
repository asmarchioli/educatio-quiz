package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.service.AlunoService;
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
        this.service = alunoService;
    }

}
