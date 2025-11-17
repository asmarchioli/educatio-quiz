package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizQuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;

import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.model.QuizQuestao;

import org.springframework.dao.DataAccessException;
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
    private final QuizQuestaoDAO quizQuestaoDAO;


    @Autowired
    public QuizService(QuizDAO quizDAO, QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO, QuizQuestaoDAO quizQuestaoDAO) {
        this.quizDAO = quizDAO;
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
        this.quizQuestaoDAO = quizQuestaoDAO;
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

    //Usado em editar_quiz.html
    public List<QuizQuestao> buscarPorIdQuestoes_Quiz(long id) {
        return quizQuestaoDAO.findQuestoesByQuizId(id);
    }

    //Usado em criar_quiz.html
    public Optional<Quiz> buscarQuizPorId(long id) {
        Optional<Quiz> quizOpt = quizDAO.findById(id);
        if (quizOpt.isEmpty()) {
            return Optional.empty();
        }
        return quizOpt;
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

    //Atualmente utilizada para adicionar questões ao quiz (pois está sendo setado o valor padrão da pontuação da questão como 10, para depois ser editado na aba de edição do quiz)
    public void adicionarQuestaoAoQuiz(Long id_questao, Long id_quiz){
        System.out.println("Quiz ID Service: " + id_quiz);
        System.out.println("Questão ID Service: " + id_questao);
        try{
            quizQuestaoDAO.addQuestaoToQuiz(id_questao, id_quiz, 10); 
        } catch (DataAccessException e) {
            // Se cair aqui, é um erro de banco de dados (Foreign Key, Duplicidade, etc.)

            // Relança outras exceções de forma genérica
            throw new RuntimeException("Erro ao salvar relação Quiz-Questão no banco de dados.", e);
        }
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
    public void deletarQuiz(long id_quiz){
        quizQuestaoDAO.removeAllQuestoesFromQuiz(id_quiz);
        quizDAO.deleteById(id_quiz);
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
    public Quiz salvar(Quiz quiz, Long id_professor) throws RuntimeException {
        if (quiz.getId_quiz() == null){
            quiz.setProfessor_criador(id_professor);  //Seta o professor criador como o professor logado
        }
        
        quiz.setData_criacao(java.time.LocalDate.now()); //Seta a data de criação como a data atual
        
        if (quiz.getPin_acesso().isEmpty()){
            quiz.setPin_acesso(null); // Se o PIN estiver vazio, define como NULL
        }
        
        return quizDAO.save(quiz);
    }

    @Transactional // Garante que a operação seja atômica
    public void deletarQuestaoDoQuiz(Long id_questao, Long id_quiz){
        quizQuestaoDAO.removeQuestaoFromQuiz(id_questao, id_quiz);
    }

    public List<Quiz> getPublicQuizzes() {
        return quizDAO.findPublicQuizzes();
    }
}