package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlunoDAO;
import br.uel.educatio.quiz.dao.QuizQuestaoDAO;
import br.uel.educatio.quiz.model.QuizQuestao;
import org.springframework.stereotype.Service;

@Service
public class AlunoService {
    private final AlunoDAO alunoDao;
    private final QuizQuestaoDAO quizQuestaoDAO;

    public AlunoService(AlunoDAO alunoDao, QuizQuestaoDAO quizQuestaoDAO) {
        this.alunoDao = alunoDao;
        this.quizQuestaoDAO = quizQuestaoDAO;
    }

    public void buscarHistoricoQuizzes(long idAluno) {
    }
    
    public void buscarRespostasQuiz(long idAluno, long idQuiz) {
    }
}
