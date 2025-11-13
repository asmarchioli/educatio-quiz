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

        int ultimaTentativa = respostaDAO.buscarUltimaTentativa(idAluno, idQuiz);
        int novaTentativa = ultimaTentativa + 1;
        
        // Lista para acumular as respostas antes de salvar em lote
        List<Resposta> respostasParaSalvar = new ArrayList<>();

        for (int i = 0; i < respostas.size(); i++) {
            Resposta resposta = respostas.get(i);
            resposta.setId_aluno(idAluno);
            resposta.setId_quiz(idQuiz);
            resposta.setTentativa(novaTentativa); // Define a tentativa

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
            String respostaAluno = resposta.getResposta_aluno_texto()
                .trim()
                .replaceAll("\\s+", " ")  // Substitui múltiplos espaços por um único
                .toLowerCase();

            String respostaCorreta = correta.get().getTexto_alternativa()
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();

            return respostaAluno.equals(respostaCorreta);
        } else {
            if (resposta.getResposta_aluno_num() == null) return false;
            
            long respostaDoAluno = resposta.getResposta_aluno_num().longValue();
            long respostaCorreta = correta.get().getNum_alternativa().longValue();

            return respostaDoAluno == respostaCorreta;
        }
    }

    public boolean alunoJaRealizouQuiz(long idAluno, long idQuiz) {
        return respostaDAO.alunoJaRealizouQuiz(idAluno, idQuiz);
    }

    public List<Resposta> buscarRespostasDoAluno(long idAluno, long idQuiz) {
        return respostaDAO.findByIdAlunoAndIdQuiz(idAluno, idQuiz);
    }

    public int calcularPontuacaoTotal(long idAluno, long idQuiz, int tentativa) {
        return respostaDAO.calcularPontuacaoTotalPorTentativa(idAluno, idQuiz, tentativa);
    }

    public int contarAcertos(long idAluno, long idQuiz, int tentativa) {
        return respostaDAO.contarAcertosPorTentativa(idAluno, idQuiz, tentativa);
    }

    public int buscarUltimaTentativa(long idAluno, long idQuiz) {
        return respostaDAO.buscarUltimaTentativa(idAluno, idQuiz);
    }

    public List<Resposta> buscarRespostasDoAlunoPorTentativa(long idAluno, long idQuiz, int tentativa) {
        return respostaDAO.findByIdAlunoAndIdQuizAndTentativa(idAluno, idQuiz, tentativa);
    }

    public List<Integer> buscarTodasTentativas(long idAluno, long idQuiz) {
        return respostaDAO.buscarTodasTentativas(idAluno, idQuiz);
    }

}