package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.ProfessorAreaDAO;
import br.uel.educatio.quiz.dao.ProfessorDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.model.Questao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestaoService {
    private final QuestaoDAO questaoDAO;

    public QuestaoService(QuestaoDAO questaoDao) {
        this.questaoDAO = questaoDao;
    }

    public List<Questao> listarQuestoesPorProfessor(long idProfessor) {
        return null;
    }
}
