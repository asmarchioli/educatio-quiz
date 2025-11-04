package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Quiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizDAO quizDAO;
    private final QuestaoDAO questaoDAO;
    private final AlternativaDAO alternativaDAO;

    public QuizService(QuizDAO quizDAO, QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO) {
        this.quizDAO = quizDAO;
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
    }

    // Busca quizzes para a home do Aluno
    public List<Quiz> getPublicQuizzes() {
        // CORREÇÃO: Método já estava correto
        return quizDAO.findPublicQuizzes();
    }

    // Busca quizzes para a home do Professor
    public List<Quiz> getQuizzesByProfessor(Long professorId) {
        // CORREÇÃO: Usando o método DAO moderno
        return quizDAO.findByProfessorCriador(professorId);
    }

    // Busca um quiz por PIN
    public Optional<Quiz> getQuizByPin(String pin) {
        // CORREÇÃO: Usando o método DAO moderno
        return quizDAO.findByPinAcesso(pin);
    }

    // Monta o Quiz completo para a página de realização
    public Optional<Quiz> getFullQuizForTaking(Long quizId) {
        Optional<Quiz> quizOpt = quizDAO.findById(quizId);
        if (quizOpt.isEmpty()) {
            return Optional.empty();
        }

        Quiz quiz = quizOpt.get();
        List<Questao> questoes = questaoDAO.findQuestoesByQuizId(quizId);

        for (Questao questao : questoes) {
            if (questao.getTipo_questao() != br.uel.educatio.quiz.model.enums.TipoQuestao.PREENCHER_LACUNA) {
                questao.setAlternativas(alternativaDAO.findAlternativasByQuestaoId(questao.getId_questao()));
            }
        }
        quiz.setQuestoes(questoes);
        return Optional.of(quiz);
    }

    // --- Métodos usados pelo seu QuizController (já estavam corretos) ---

    @Transactional
    public List<String> listarQuizAreas(long id_quiz){
        return quizDAO.findAreasQuiz(id_quiz);
    }

    @Transactional
    public List<Quiz> listarTodos(){
        return quizDAO.findAll();
    }

    @Transactional
    public void deletar(long id){
        quizDAO.deleteById(id);
    }

    @Transactional
    public Quiz buscarPorPin(String pin){
        Optional<Quiz> quizOpt = quizDAO.findByPinAcesso(pin);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz não encontrado!");
        }
        return quizOpt.get();
    }

    @Transactional
    public Quiz buscarPorId(long id){
        Optional<Quiz> quizOpt = quizDAO.findById(id);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz não encontrado!");
        }
        return quizOpt.get();
    }

    @Transactional
    public List<Quiz> buscarPorProfessorCriador(long id_prof){
        List<Quiz> quiz = quizDAO.findByProfessorCriador(id_prof);
        // Remover a exceção para lista vazia é uma boa prática
        return quiz;
    }

    @Transactional
    public void criar(Quiz quiz) throws RuntimeException {
        quizDAO.save(quiz);
    }
}