package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;
import br.uel.educatio.quiz.model.Quiz;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {
    private final QuizDAO quizDAO;

    public QuizService(QuizDAO quizDao) {
        this.quizDAO = quizDao;
    }

    public List<Quiz> listarQuizzesPorProfessor(long idProfessor) {
        return null;
    }
}
