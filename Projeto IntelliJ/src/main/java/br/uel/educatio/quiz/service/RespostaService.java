package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.RespostaDAO;
import br.uel.educatio.quiz.model.Alternativa;
import br.uel.educatio.quiz.model.Resposta;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Importado para o 'saveBatch'
import java.util.List;
import java.util.Optional;

@Service
public class RespostaService {

    private final RespostaDAO respostaDAO;
    private final AlternativaDAO alternativaDAO;

    @Autowired
    public RespostaService(RespostaDAO respostaDAO, AlternativaDAO alternativaDAO) {
        this.respostaDAO = respostaDAO;
        this.alternativaDAO = alternativaDAO;
    }

    @Transactional
    public void salvarRespostas(long idAluno, long idQuiz, List<Resposta> respostas, List<Integer> pontuacoesQuestoes, List<TipoQuestao> tiposQuestoes) {

        // Lista para acumular as respostas antes de salvar em lote
        List<Resposta> respostasParaSalvar = new ArrayList<>();

        for (int i = 0; i < respostas.size(); i++) {
            Resposta resposta = respostas.get(i);
            resposta.setId_aluno(idAluno);
            resposta.setId_quiz(idQuiz);
            resposta.setTentativa(1); // Define a tentativa

            // Lógica de verificação (vinda do Arquivo 2)
            boolean acertou = verificarResposta(resposta, tiposQuestoes.get(i));
            resposta.setFlg_acertou(acertou ? 'S' : 'N');
            resposta.setPontuacao_aluno(acertou ? pontuacoesQuestoes.get(i) : 0);

            respostasParaSalvar.add(resposta);
        }

        // Salva tudo de uma vez (Lógica do Arquivo 1)
        if (!respostasParaSalvar.isEmpty()) {
            respostaDAO.saveBatch(respostasParaSalvar);
        }
    }

 
    private boolean verificarResposta(Resposta resposta, TipoQuestao tipoQuestao) {
        Optional<Alternativa> correta = alternativaDAO.findAlternativaCorreta(resposta.getId_questao());

        if (correta.isEmpty()) return false;

        if (tipoQuestao == TipoQuestao.PREENCHER_LACUNA) {
            if (resposta.getResposta_aluno_texto() == null) return false;
            return resposta.getResposta_aluno_texto().trim().equalsIgnoreCase(
                correta.get().getTexto_alternativa().trim()
            );
        } else {
            if (resposta.getResposta_aluno_num() == null) return false;
            // Compara o número da alternativa (Integer) com o número da alternativa (Long)
            return resposta.getResposta_aluno_num().equals(correta.get().getNum_alternativa().intValue());
        }
    }

    public boolean alunoJaRealizouQuiz(long idAluno, long idQuiz) {
        return respostaDAO.alunoJaRealizouQuiz(idAluno, idQuiz);
    }

    public List<Resposta> buscarRespostasDoAluno(long idAluno, long idQuiz) {
        return respostaDAO.findByIdAlunoAndIdQuiz(idAluno, idQuiz);
    }

    public int calcularPontuacaoTotal(long idAluno, long idQuiz) {
        return respostaDAO.calcularPontuacaoTotal(idAluno, idQuiz);
    }

    public int contarAcertos(long idAluno, long idQuiz) {
        return respostaDAO.contarAcertos(idAluno, idQuiz);
    }
}