package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.QuizQuestao;
import br.uel.educatio.quiz.model.Resposta;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RespostaDAO {

    private final JdbcTemplate jdbcTemplate;

    public RespostaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Resposta> rowMapper = (rs, rowNum) -> {
        Resposta r = new Resposta();
        r.setId_resposta(rs.getLong("id_resposta"));
        r.setId_questao(rs.getLong("id_questao")); // FK
        r.setId_quiz(rs.getLong("id_quiz")); // FK
        r.setId_aluno(rs.getLong("id_aluno")); // FK
        r.setTentativa(rs.getInt("tentativa"));
        r.setPontuacao_aluno(rs.getInt("pontuacao_aluno"));
        String flag = rs.getString("flg_acertou");
        if (flag != null && !flag.isEmpty()) {
            r.setFlg_acertou(flag.charAt(0));
        }
        return r;
    };

    public Optional<Resposta> findById(long id) {
        String sql = "SELECT * FROM resposta WHERE id_resposta = ?";
        try {
            Resposta r = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca todas as respostas de um aluno para um quiz específico.
     * @param idAluno ID do Aluno.
     * @param idQuiz ID do Quiz.
     * @return Lista de respostas.
     */
    public List<Resposta> findByIdAlunoAndIdQuiz(long idAluno, long idQuiz) {
        String sql = "SELECT * FROM resposta WHERE id_aluno = ? AND id_quiz = ? ORDER BY id_questao, tentativa";
        return jdbcTemplate.query(sql, new Object[]{idAluno, idQuiz}, rowMapper);
    }

    /**
     * Salva uma nova resposta. A atualização pode não fazer sentido dependendo das regras.
     * @param resposta A resposta a ser salva.
     * @return A resposta salva com o ID gerado.
     */
    public Resposta save(Resposta resposta) {
        // Assumindo que só inserimos novas respostas
        String sql = "INSERT INTO resposta (id_questao, id_quiz, id_aluno, tentativa, pontuacao_aluno, flg_acertou)" +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_resposta";
        Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                resposta.getId_questao(),
                resposta.getId_quiz(),
                resposta.getId_aluno(),
                resposta.getTentativa(),
                resposta.getPontuacao_aluno(),
                String.valueOf(resposta.getFlg_acertou()) // Converte char para String
        }, Long.class);
        resposta.setId_resposta(newId != null ? newId : 0L);
        return resposta;
    }

    /**
     * Busca uma resposta específica pela chave única composta.
     * @param idQuestao ID da Questão.
     * @param idQuiz ID do Quiz.
     * @param idAluno ID do Aluno.
     * @param tentativa Número da Tentativa.
     * @return Optional da Resposta, se encontrada.
     */
    public Optional<Resposta> findByKeyComposta(long idQuestao, long idQuiz, long idAluno, int tentativa) {
        String sql = "SELECT * FROM resposta WHERE id_questao = ? AND id_quiz = ? AND id_aluno = ? AND tentativa = ?";
        try {
            Resposta r = jdbcTemplate.queryForObject(sql, new Object[]{idQuestao, idQuiz, idAluno, tentativa}, rowMapper);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Deletar uma resposta específica pelo ID
    public void deleteById(long id) {
        String sql = "DELETE FROM resposta WHERE id_resposta = ?";
        jdbcTemplate.update(sql, id);
    }
}