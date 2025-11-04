package br.uel.educatio.quiz.service;


import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.model.Questao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class QuestaoService {
    private final QuestaoDAO questaoDAO;

    public QuestaoService(QuestaoDAO questaoDao) {
        this.questaoDAO = questaoDao;
    }

    public List<Questao> listarQuestoesPorProf(long idProfessor) {
        return questaoDAO.findByProfessorId(idProfessor);
    }
    
    public List<Questao> listarQuestoes() {
        return questaoDAO.findAll();
    }

    public Questao buscarQuestao(long id_questao) {
        Optional<Questao> questaoOpt = questaoDAO.findById(id_questao);
        if (questaoOpt.isEmpty()) {
            throw new RuntimeException("Questão não encontrada");
        }
        
        Questao questao = questaoOpt.get();
        return questao;
    }

}

