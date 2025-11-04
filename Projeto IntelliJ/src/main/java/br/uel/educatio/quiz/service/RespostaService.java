package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.RespostaDAO;
import br.uel.educatio.quiz.model.Alternativa;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Resposta;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RespostaService {

    private final RespostaDAO respostaDAO;
    private final AlternativaDAO alternativaDAO;
    private final QuestaoDAO questaoDAO; // Para buscar pontuação

    public RespostaService(RespostaDAO respostaDAO, AlternativaDAO alternativaDAO, QuestaoDAO questaoDAO) {
        this.respostaDAO = respostaDAO;
        this.alternativaDAO = alternativaDAO;
        this.questaoDAO = questaoDAO;
    }

    public void processAndSaveRespostas(Map<String, String> submittedAnswers, Long quizId, Long alunoId) {
        List<Resposta> respostasParaSalvar = new ArrayList<>();

        // Precisamos das questões para saber a pontuação de cada uma
        // (O campo 'pontuacao' transiente será preenchido pelo QuestaoDAO)
        List<Questao> questoesDoQuiz = questaoDAO.findQuestoesByQuizId(quizId);

        for (Questao questao : questoesDoQuiz) {
            Long questaoId = questao.getId_questao();
            // O name do input no HTML será "q-{{questao.id_questao}}"
            String respostaAluno = submittedAnswers.get("q-" + questaoId);

            if (respostaAluno == null || respostaAluno.isBlank()) {
                continue; // Aluno não respondeu esta questão
            }

            Alternativa correta = alternativaDAO.findAlternativaCorreta(questaoId);
            if (correta == null) continue; // Questão sem gabarito, pula

            Resposta r = new Resposta();
            r.setId_questao(questaoId);
            r.setId_quiz(quizId);
            r.setId_aluno(alunoId);

            boolean acertou = false;

            if (questao.getTipo_questao() == br.uel.educatio.quiz.model.enums.TipoQuestao.PREENCHER_LACUNA) {
                // Compara a resposta (String)
                acertou = respostaAluno.trim().equalsIgnoreCase(correta.getTexto_alternativa().trim());
                r.setResposta_aluno_texto(respostaAluno);
            } else {
                // Compara a alternativa (Número)
                try {
                    // O valor da alternativa vem como Long (num_alternativa)
                    long numResposta = Long.parseLong(respostaAluno);
                    acertou = (numResposta == correta.getNum_alternativa());
                    r.setResposta_aluno_num((int) numResposta); // Salva como int
                } catch (NumberFormatException e) {
                    // Resposta inválida
                }
            }

            if (acertou) {
                r.setFlg_acertou('S');
                r.setPontuacao_aluno(questao.getPontuacao()); // Pega a pontuação transiente
            } else {
                r.setFlg_acertou('N');
                r.setPontuacao_aluno(0);
            }

            respostasParaSalvar.add(r);
        }

        if (!respostasParaSalvar.isEmpty()) {
            respostaDAO.saveBatch(respostasParaSalvar);
        }
    }
}

