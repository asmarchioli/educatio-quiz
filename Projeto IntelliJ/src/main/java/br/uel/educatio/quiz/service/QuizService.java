package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.RespostaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizQuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;
import br.uel.educatio.quiz.dao.RankingDAO;
import br.uel.educatio.quiz.dao.GraficoQuestsDAO;
import br.uel.educatio.quiz.dao.RankingQuestsDAO;

import br.uel.educatio.quiz.model.dto.GraficoQuestsDTO;
import br.uel.educatio.quiz.model.dto.RankingDTO;
import br.uel.educatio.quiz.model.dto.RankingQuestsDTO;
import br.uel.educatio.quiz.model.enums.TipoQuestao;

import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.model.QuizQuestao;

import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class QuizService {
    private final QuizDAO quizDAO;
    private final QuestaoDAO questaoDAO;
    private final AlternativaDAO alternativaDAO;
    private final RespostaDAO respostaDAO;
    private final QuizQuestaoDAO quizQuestaoDAO;
    private final RankingDAO rankingDAO;
    private final RankingQuestsDAO rankingQuestsDAO;
    private final GraficoQuestsDAO graficoQuestsDAO;


    @Autowired
    public QuizService(QuizDAO quizDAO, QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO, QuizQuestaoDAO quizQuestaoDAO, RankingDAO rankingDAO, RankingQuestsDAO rankingQuestsDAO, GraficoQuestsDAO graficoQuestsDAO, RespostaDAO respostaDAO) {
        this.quizDAO = quizDAO;
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
        this.quizQuestaoDAO = quizQuestaoDAO;
        this.respostaDAO = respostaDAO;
        this.rankingDAO = rankingDAO;
        this.rankingQuestsDAO = rankingQuestsDAO;
        this.graficoQuestsDAO = graficoQuestsDAO;
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
    
    public int numeroResolucoesQuiz(Long id_quiz){ //Número de alunos que resolveram o quiz
        return respostaDAO.frequenciaResolucaoQuiz(id_quiz);
    }

    public int pontuacaoMaximaQuiz(Long id_quiz){
        return quizQuestaoDAO.getPontuacaoQuiz(id_quiz);
    }
    
    public List<RankingDTO> buscarRankingQuiz(Long id_quiz){
        return rankingDAO.buscarRankingDoQuiz(id_quiz);
    }

    
    public List<RankingDTO> buscarRankingQuizPorNome(Long id_quiz, String termoBusca){
        return rankingDAO.buscarRankingPorNome(id_quiz, termoBusca);
    }    

    public List<RankingQuestsDTO> buscarRankingQuests(Long id_quiz){
        return rankingQuestsDAO.buscarRankingQuests(id_quiz);
    }

    public List<GraficoQuestsDTO> DadosGraficoQuests(Long id_quiz){
        return graficoQuestsDAO.buscarDadosParaGrafico(id_quiz);
    }
    
    //Usado em editar_quiz.html
    public List<QuizQuestao> buscarPorIdQuestoes_Quiz_Filtrado(long id_quiz, String filtroTipo, String termoBusca) {
        TipoQuestao tipoEnum = null;

        // Verifica se filtroTipo não é nulo, nem vazio, e nem a opção padrão "Todos os Tipos" (se houver)
        if (filtroTipo != null && !filtroTipo.isEmpty()) {
            try {
                // IMPORTANTE: Se o value do seu select no HTML for o nome do enum (ex: MULTIPLA_ESCOLHA), use valueOf.
                // Se for o displayValue (ex: "Múltipla Escolha"), use seu método fromString.
                tipoEnum = TipoQuestao.valueOf(filtroTipo); 
            } catch (IllegalArgumentException e) {
                // Tenta pelo método customizado se o valueOf falhar (caso o HTML envie displayValue)
                try {
                    tipoEnum = TipoQuestao.fromString(filtroTipo);
                } catch (Exception ex) {
                    System.out.println("Tipo de questão inválido ou vazio: " + filtroTipo);
                }
            }
        }

        // 2. Trata o Termo de Busca (String vazia -> Null)
        if (termoBusca != null && termoBusca.trim().isEmpty()) {
            termoBusca = null;
        }

        return quizQuestaoDAO.findQuestoesByQuizIdFiltrado(id_quiz, tipoEnum, termoBusca);
    }

    //Usado em criar_quiz.html
    public Optional<Quiz> buscarQuizPorId(long id) {
        Optional<Quiz> quizOpt = quizDAO.findById(id);
        if (quizOpt.isEmpty()) {
            return Optional.empty();
        }
        return quizOpt;
    }
    

    public List<Questao> buscarQuestaoParaQuiz(Long idProfessor, Long idQuiz, String modoBusca, String filtroTipo, String termoBusca) {


        TipoQuestao tipoEnum = null;
        if (filtroTipo != null && !filtroTipo.isEmpty() && !filtroTipo.equals("Todos os Tipos")) {
            try {
                tipoEnum = TipoQuestao.valueOf(filtroTipo); 
            } catch (IllegalArgumentException e) {
                try {
                    tipoEnum = TipoQuestao.fromString(filtroTipo);
                } catch (Exception ex) {
                    // Tipo inválido ou vazio, mantém null (busca todos)
                }
            }
        }
        Optional<Quiz> quizOpt = quizDAO.findById(idQuiz);
        if (quizOpt.isEmpty()) return new ArrayList<>();
        Quiz quiz = quizOpt.get();

        // Modo de Busca: Público ou Minhas Questões
        if ("COMPATIVEL".equals(modoBusca)) {
            // Busca no Banco Público (Com filtro de tipo)
            return questaoDAO.findPublicasCompativeis(
                quiz.getArea(), 
                quiz.getNivel_educacional().toString(), 
                tipoEnum, 
                termoBusca
            );
        } else {
            // Busca nas Minhas Questões 
            return questaoDAO.findByFiltros(idProfessor, tipoEnum, termoBusca); 
        }
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

    public int calcularPontuacaoMaxima(long id_quiz) {
        List<Questao> questoes = questaoDAO.findQuestoesDoQuiz(id_quiz);
        return questoes.stream()
                       .mapToInt(Questao::getPontuacao)
                       .sum();
    }
// 
    
    public int contarQuestoes(long id_quiz) {
        return questaoDAO.findQuestoesDoQuiz(id_quiz).size();
    }

    // ---
    // MÉTODOS PARA O 'PROFESSOR/ADMIN' CONTROLLER (Lançam Exceção)
    // ---

    public void adicionarQuestaoAoQuiz(Long id_questao, Long id_quiz, int pontuacao){
        System.out.println("Adicionando Questão " + id_questao + " ao Quiz " + id_quiz + " com valor: " + pontuacao);

        try{
            quizQuestaoDAO.addQuestaoToQuiz(id_questao, id_quiz, pontuacao); 

        } catch (DataAccessException e) {
            throw new RuntimeException("Erro ao salvar relação Quiz-Questão no banco de dados.", e);
        }
    }

    
    public void validarPontuacao(Long id_quiz, Integer novaPontuacao, Long idQuestaoSendoEditada) {
     
        int somaAtual = quizQuestaoDAO.getPontuacaoQuiz(id_quiz);

        //Se estiver editando, subtrair o valor antigo dessa questão antes de somar o novo
        if (idQuestaoSendoEditada != null) {
            // Busca quanto essa questão valia antes
            Optional<Integer> pontuacaoAntiga = quizQuestaoDAO.findPontuacao(idQuestaoSendoEditada, id_quiz);
            if (pontuacaoAntiga.isPresent()) {
                somaAtual -= pontuacaoAntiga.get();
            }
        }

        // Verifica se estoura 100
        if (somaAtual + novaPontuacao > 100) {
            throw new RuntimeException("A pontuação total não pode exceder 100 pontos! (Máximo Atual: " + (100 - somaAtual) + " para esta questão)");
        }
    }
    
    @Transactional
    public void atualizarPontuacaoQuestao(Long id_questao, Long id_quiz, int novaPontuacao) {
        quizQuestaoDAO.atualizarPontuacao(id_questao, id_quiz, novaPontuacao);
    }

    
    public List<Quiz> getPublicQuizzes() {
        return quizDAO.findPublicQuizzes();
    }
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


    @Transactional
    public Quiz buscarQuizParaEdicaoPorPin(String pin){
        // Método do Admin usa o DAO 'findByPinAcesso'
        Optional<Quiz> quizOpt = quizDAO.findByPinAcesso(pin);
        if (quizOpt.isEmpty()) {
            throw new RuntimeException("Quiz não encontrado!");
        }
        return quizOpt.get();
    }

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
        
        respostaDAO.deleteById(id_questao); //Tira também as respostas dos alunos nessa questão do quiz
        
        quizQuestaoDAO.removeQuestaoFromQuiz(id_questao, id_quiz);
    }

}