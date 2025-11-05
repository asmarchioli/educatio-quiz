package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO; // Importado do Arquivo 1
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Quiz;
import org.springframework.beans.factory.annotation.Autowired; // Importado do Arquivo 2
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importado do Arquivo 1

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizDAO quizDAO;
    private final QuestaoDAO questaoDAO;
    private final AlternativaDAO alternativaDAO; // Vindo do Arquivo 1


    @Autowired // Adicionado para consistência
    public QuizService(QuizDAO quizDAO, QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO) {
        this.quizDAO = quizDAO;
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
    }


    public List<Quiz> buscarQuizzesPublicosPorNivel(String nivelEducacional) {
        return quizDAO.findQuizzesPublicosPorNivel(nivelEducacional);
    }


    public Optional<Quiz> buscarPorPin(String pin) {
        // Nota: Este método (do Arquivo 2) usa quizDAO.findByPin(pin)
        return quizDAO.findByPin(pin);
    }

    /**
     * *** O MÉTODO MAIS IMPORTANTE DA MESCLAGEM ***
     * Assinatura (Vinda do Arquivo 2)
     * Implementação (Adaptada do Arquivo 1)
     *
     * Motivo: O AlunoController chama 'buscarPorId' e espera um 'Optional'.
     * A implementação original do Arquivo 2 era INCOMPLETA (não buscava as alternativas).
     * A implementação do Arquivo 1 (do método 'getFullQuizForTaking') estava CORRETA.
     *
     * Este método mesclado usa a assinatura do Arquivo 2 com a lógica correta do Arquivo 1.
     */
    public Optional<Quiz> buscarPorId(long id) {
        Optional<Quiz> quizOpt = quizDAO.findById(id);
        if (quizOpt.isEmpty()) {
            return Optional.empty();
        }

        Quiz quiz = quizOpt.get();
        // Usa 'findQuestoesDoQuiz' (dos DAOs mesclados)
        List<Questao> questoes = questaoDAO.findQuestoesDoQuiz(id);

        // Lógica crucial (vinda do Arquivo 1) para adicionar alternativas
        for (Questao questao : questoes) {
            if (questao.getTipo_questao() != br.uel.educatio.quiz.model.enums.TipoQuestao.PREENCHER_LACUNA) {
                // Usa 'findByQuestaoId' (dos DAOs mesclados)
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


    // --- Métodos Mantidos do Arquivo 1 (Para o Professor/Admin Controller) ---

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
        return quizDAO.findByProfessorCriador(id_prof);
    }

    @Transactional
    public void criar(Quiz quiz) throws RuntimeException {
        quizDAO.save(quiz);
    }

    // Métodos de 'get' que também estavam no Arquivo 1
    public List<Quiz> getPublicQuizzes() {
        return quizDAO.findPublicQuizzes();
    }

    public List<Quiz> getQuizzesByProfessor(Long professorId) {
        return quizDAO.findByProfessorCriador(professorId);
    }

    public Optional<Quiz> getQuizByPin(String pin) {
        return quizDAO.findByPinAcesso(pin);
    }
}