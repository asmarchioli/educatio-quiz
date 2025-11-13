package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizDAO quizDAO;
    private final QuestaoDAO questaoDAO;
    private final AlternativaDAO alternativaDAO;


    @Autowired
    public QuizService(QuizDAO quizDAO, QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO) {
        this.quizDAO = quizDAO;
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
    }

    // ---
    // MÉTODOS PARA O 'ALUNO' CONTROLLER (Retornam Optional)
    // ---

    public List<Quiz> buscarQuizzesPublicosPorNivel(String nivelEducacional) {
        return quizDAO.findQuizzesPublicosPorNivel(nivelEducacional);
    }

    public Optional<Quiz> buscarPorPin(String pin) {
        // Método do Aluno usa o DAO 'findByPin'
        return quizDAO.findByPin(pin);
    }

    /**
     * Busca o Quiz completo para o ALUNO realizar
     * (com questões e alternativas)
     */
    public Optional<Quiz> buscarPorId(long id) {
        Optional<Quiz> quizOpt = quizDAO.findById(id);
        if (quizOpt.isEmpty()) {
            return Optional.empty();
        }

        Quiz quiz = quizOpt.get();
        List<Questao> questoes = questaoDAO.findQuestoesDoQuiz(id);

        // Adiciona as alternativas nas questões
        for (Questao questao : questoes) {
            if (questao.getTipo_questao() != br.uel.educatio.quiz.model.enums.TipoQuestao.PREENCHER_LACUNA) {
                questao.setAlternativas(alternativaDAO.findByQuestaoId(questao.getId_questao()));
            }
        }
        quiz.setQuestoes(questoes);
        return Optional.of(quiz);
    }

    public int calcularPontuacaoMaxima(long idQuiz) {
        List<Questao> questoes = questaoDAO.findQuestoesDoQuiz(idQuiz);
        return questoes.stream()
                       .mapToInt(Questao::getPontuacao)
                       .sum();
    }


    public int contarQuestoes(long idQuiz) {
        return questaoDAO.findQuestoesDoQuiz(idQuiz).size();
    }


    // ---
    // MÉTODOS PARA O 'PROFESSOR/ADMIN' CONTROLLER (Lançam Exceção)
    // ---

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

    /**
     * RENOMEADO: Busca um quiz para edição (Admin)
     * (Não carrega alternativas, lança exceção se não achar)
     */
    @Transactional
    public Quiz buscarQuizParaEdicaoPorPin(String pin){
        // Método do Admin usa o DAO 'findByPinAcesso'
        Optional<Quiz> quizOpt = quizDAO.findByPinAcesso(pin);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz não encontrado!");
        }
        return quizOpt.get();
    }

    /**
     * RENOMEADO: Busca um quiz para edição (Admin)
     * (Não carrega alternativas, lança exceção se não achar)
     */
    @Transactional
    public Quiz buscarQuizParaEdicao(long id){
        Optional<Quiz> quizOpt = quizDAO.findById(id);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz não encontrado!");
        }
        return quizOpt.get();
    }

    @Transactional
    public List<Quiz> buscarPorProfessorCriador(long id_prof){
        return quizDAO.findByProfessorCriador(id_prof);
    }

    @Transactional
    public void criar(Quiz quiz) throws RuntimeException {
        quizDAO.save(quiz);
    }

    public List<Quiz> getPublicQuizzes() {
        return quizDAO.findPublicQuizzes();
    }
}