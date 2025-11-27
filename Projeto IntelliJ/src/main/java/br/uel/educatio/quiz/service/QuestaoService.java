package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuizQuestaoDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Alternativa;
import br.uel.educatio.quiz.model.QuizQuestao;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import br.uel.educatio.quiz.model.enums.Dificuldade;
    

import java.util.List;
import java.util.Optional;


@Service
public class QuestaoService { 
    private final QuestaoDAO questaoDAO;
    private final AlternativaDAO alternativaDAO;
    private final QuizQuestaoDAO quizQuestaoDAO;

    public QuestaoService(QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO, QuizQuestaoDAO quizQuestaoDAO) {
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
        this.quizQuestaoDAO = quizQuestaoDAO;
    }
    
    @Transactional // Garante que a operação seja atômica
    public Questao salvar(Questao questao, Long idProfessor) {

        questao.setProfessor_criador(idProfessor);


        

        Questao questaoSalva = questaoDAO.save(questao); 

    
        if (questaoSalva.getId_questao() != null) {
            // Se estiver em edição: Limpa todas as alternativas antigas
            alternativaDAO.deleteByIdQuestao(questaoSalva.getId_questao());
        }

        if (questaoSalva.getAlternativas() != null) {
            long numAlternativa = 1; 

            for (Alternativa alt : questaoSalva.getAlternativas()) {
                alt.setId_questao(questaoSalva.getId_questao()); // Liga a FK

        
                alt.setNum_alternativa(numAlternativa); 
                
                // Incrementa o contador para a próxima alternativa
                numAlternativa++;

                alternativaDAO.save(alt); 
            }
        }

        return questaoSalva;
    }

    public List<Questao> buscarQuestoesPorFiltro(Long id_professor, String filtroTipo, String termoBusca) {

        TipoQuestao tipoEnum = null;

        System.out.println("Tipo: " + filtroTipo);
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

        System.out.println("Tipo Enum: " + tipoEnum);

 
        if (termoBusca != null && termoBusca.trim().isEmpty()) {
            termoBusca = null;
        }

   
        return questaoDAO.findByFiltros(id_professor, tipoEnum, termoBusca);
    }

    
    
    
    public List<Alternativa> listarAlternativas(Long id_questao) {
        return alternativaDAO.findByQuestaoId(id_questao);
    }

    public List<Questao> listarQuestoesPorArea(Long id_area){
        return questaoDAO.findByArea(id_area);
    }
    
    public List<Questao> listarQuestoesPorProf(Long idProfessor) {
        return questaoDAO.findByProfessorId(idProfessor);
    }

    
    //Usada para buscar questões para o banco de questões geral
    public List<Questao> listarTodasQuestoesComFiltro(Long id_area, String dificuldade, String filtroTipo, String termoBusca) {

        TipoQuestao tipoEnum = null;
        if (filtroTipo != null && !filtroTipo.isEmpty()) {
            try {
                tipoEnum = TipoQuestao.valueOf(filtroTipo); 
            } catch (IllegalArgumentException e) {
                try {
                    tipoEnum = TipoQuestao.fromString(filtroTipo);
                } catch (Exception ex) {
                    System.out.println("Tipo de questão inválido ou vazio: " + filtroTipo);
                }
            }
        }
    
    
        Dificuldade dificuldadeEnum = null;
        if (dificuldade != null && !dificuldade.isEmpty()) {
            try {
                // Tenta converter pelo nome exato do Enum (ex: "FACIL")
                dificuldadeEnum = Dificuldade.valueOf(dificuldade);
            } catch (IllegalArgumentException e) {
                try {
                    // Se falhar, tenta pelo display value (ex: "Fácil") se o seu Enum tiver esse método
                    dificuldadeEnum = Dificuldade.fromString(dificuldade);
                } catch (Exception ex) {
                    System.out.println("Dificuldade inválida ou vazia: " + dificuldade);
                }
            }
        }


        if (termoBusca != null && termoBusca.trim().isEmpty()) {
            termoBusca = null;
        }
   
        return questaoDAO.findAllByFiltros(id_area, dificuldadeEnum, tipoEnum, termoBusca);
    }

    
    public List<Questao> listarQuestoes() {
        return questaoDAO.findAll();
    }

    public Questao buscarQuestao(Long id_questao) {
        Optional<Questao> questaoOpt = questaoDAO.findById(id_questao);
        if (questaoOpt.isEmpty()) {
            throw new RuntimeException("Questão não encontrada");
        }
        
        Questao questao = questaoOpt.get();
        return questao;
    }

  
    //Deleta a questão do banco de dados
    @Transactional // Garante que a operação seja atômica
    public void deletarQuestaoDoBanco(Long id_questao){
        alternativaDAO.deleteByIdQuestao(id_questao);

        quizQuestaoDAO.removeQuestaoFromAllQuizzes(id_questao);

        questaoDAO.deleteById(id_questao);
    }
}

