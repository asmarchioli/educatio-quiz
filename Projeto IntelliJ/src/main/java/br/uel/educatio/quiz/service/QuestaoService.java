package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Alternativa;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;



import java.util.List;
import java.util.Optional;


@Service
public class QuestaoService { 
    private final QuestaoDAO questaoDAO;
    private final AlternativaDAO alternativaDAO;

    public QuestaoService(QuestaoDAO questaoDAO, AlternativaDAO alternativaDAO) {
        this.questaoDAO = questaoDAO;
        this.alternativaDAO = alternativaDAO;
    }
    
    @Transactional // Garante que a operação seja atômica
    public Questao salvar(Questao questao, Long idProfessor) {
        // 1. REGRA DE NEGÓCIO: Injetar o criador
        questao.setProfessor_criador(idProfessor);

        // 2. REGRA DE NEGÓCIO: Limpeza de alternativas vazias
        if (questao.getAlternativas() != null) {
            questao.getAlternativas().removeIf(alternativa -> 
                alternativa.getTexto_alternativa() == null || 
                alternativa.getTexto_alternativa().trim().isEmpty()
            );
        }

        // 3. Persistir a Questão principal (obter o ID gerado se for nova)
        Questao questaoSalva = questaoDAO.save(questao); 

        // =========================================================
        // 4. LÓGICA DE PERSISTÊNCIA DAS ALTERNATIVAS (DELETE + INSERT)
        // =========================================================
        if (questaoSalva.getId_questao() != null) {
            // SE ESTIVER EM EDIÇÃO: Limpa todas as alternativas antigas
            alternativaDAO.deleteByIdQuestao(questaoSalva.getId_questao());
        }

        if (questaoSalva.getAlternativas() != null) {
            long numAlternativa = 1; 

            for (Alternativa alt : questaoSalva.getAlternativas()) {
                alt.setId_questao(questaoSalva.getId_questao()); // Liga a FK

                // / === NOVO PASSO CRÍTICO: INJETAR O NÚMERO SEQUENCIAL ===
                alt.setNum_alternativa(numAlternativa); 
                
                // Incrementa o contador para a próxima alternativa
                numAlternativa++;

                alternativaDAO.save(alt); 
            }
        }

        return questaoSalva;
    }

    //Antigo salvar
    // @Transactional
    // public void salvar(Questao questao, Professor professor) throws RuntimeException {
    //     // 1. Armazena a lista na memória ANTES de salvar o objeto pai.
    //     System.out.println("Questão: " + questao.getEnunciado());
            
    //     questao.setProfessor_criador(professor.getId_professor());
    //     // questao.setArea(professor.getArea()); // Pega a primeira área do professor

    //     System.out.println("Professor: " + professor.getId_professor());
        
    //     List<Alternativa> alternativas = questao.getAlternativas(); 

    //     alternativas.forEach(alternativa -> System.out.println(alternativa.getTexto_alternativa()));
        
    //     // 2. SALVA A QUESTÃO (O JPA/Hibernate salva Questao e GERA o id_questao)
    //     Questao questaoSalva = questaoDAO.save(questao);
        
    //     System.out.println("Questão salva com sucesso: " + questaoSalva.getId_questao());

    //     // Determina o índice correto para Múltipla Escolha
    //     Integer indiceCorretoMC = questao.getMc_correct_choice(); 

    //     // 4. Você USA a lista que foi armazenada na memória (passo 1) para salvar os filhos.
    //     for (int i = 0; i < alternativas.size(); i++) {
    //         Alternativa alt = alternativas.get(i);

    //         // Ignora alternativas com texto vazio (melhora a UX)
    //         if (alt.getTexto_alternativa() == null || alt.getTexto_alternativa().trim().isEmpty()) {
    //             continue; 
    //         }
    //         // Preenche as chaves estrangeiras e os números sequenciais
    //         alt.setId_questao(questaoSalva.getId_questao());
    //         alt.setNum_alternativa(Long.valueOf(i + 1));

    //         // Se for Múltipla Escolha (usando o campo auxiliar)
    //         if ("MULTIPLA_ESCOLHA".equals(questaoSalva.getTipo_questao().name())) {
    //             if (indiceCorretoMC != null && i == indiceCorretoMC) {
    //                 alt.setFlg_eh_correta('S'); // Define como Correta
    //             } else {
    //                 alt.setFlg_eh_correta('N'); // Define as outras como Incorretas
    //             }
    //         } 
    //         // Se for Verdadeiro ou Falso, o flg_eh_correta ('S' ou 'N') já veio mapeado
    //         // Se for Preencher Lacuna, o flg_eh_correta já veio como 'S' no [0] e o resto é ignorado

            
    //         // 5. Salva a alternativa individualmente
    //         alternativaDAO.save(alt); 
    //     }
    // }

    
    public List<Alternativa> listarAlternativas(Long id_questao) {
        return alternativaDAO.findByQuestaoId(id_questao);
    }

    public List<Questao> listarQuestoesPorArea(Long id_area){
        return questaoDAO.findByArea(id_area);
    }
    
    public List<Questao> listarQuestoesPorProf(Long idProfessor) {
        return questaoDAO.findByProfessorId(idProfessor);
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

        // 1. CHAMA O DAO PARA DELETAR OS FILHOS (ALTERNATIVA)
        alternativaDAO.deleteByIdQuestao(id_questao);

        
        // 2. CHAMA O DAO PARA DELETAR O PAI (QUESTAO)
        questaoDAO.deleteById(id_questao);
    }
}

