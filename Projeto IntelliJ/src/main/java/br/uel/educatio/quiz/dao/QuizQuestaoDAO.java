package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.QuizQuestao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuizQuestaoDAO {

    private final JdbcTemplate jdbcTemplate;

    public QuizQuestaoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<QuizQuestao> rowMapper = (rs, rowNum) -> {
        QuizQuestao qq = new QuizQuestao();
        qq.setId_questao(rs.getLong("id_questao"));
        qq.setId_quiz(rs.getLong("id_quiz"));
        qq.setPontuacao_questao(rs.getInt("pontuacao_questao"));
        return qq;
    };


    /**
     * Adiciona uma questão a um quiz com uma pontuação.
     * @param idQuestao ID da Questão.
     * @param idQuiz ID do Quiz.
     * @param pontuacao Pontuação da questão neste quiz.
     * @return O número de linhas afetadas.
     */
    public void addQuestaoToQuiz(long id_questao, long id_quiz, int pontuacao) {
        // ON CONFLICT permite atualizar a pontuação se a relação já existir
        String sql = "INSERT INTO quiz_questao (id_questao, id_quiz, pontuacao_questao) VALUES (?, ?, ?) ON CONFLICT (id_questao, id_quiz) DO UPDATE SET pontuacao_questao = EXCLUDED.pontuacao_questao";

       // Correção: Usando o método correto para atualização
        jdbcTemplate.update(sql, id_questao, id_quiz, pontuacao);

   
    }

    /**
     * Remove uma questão de um quiz.
     * @param idQuestao ID da Questão.
     * @param idQuiz ID do Quiz.
     */
    public void removeQuestaoFromQuiz(long idQuestao, long idQuiz) {
        System.out.println("idQuestao: " + idQuestao + " idQuiz: " + idQuiz);
        String sql = "DELETE FROM quiz_questao WHERE id_questao = ? AND id_quiz = ?";
        jdbcTemplate.update(sql, idQuestao, idQuiz);
    }

    /**
     * Remove todas as questões de um quiz. Útil ao deletar um quiz.
     * @param idQuiz ID do Quiz.
     */
    public void removeAllQuestoesFromQuiz(long idQuiz) {
        String sql = "DELETE FROM quiz_questao WHERE id_quiz = ?";
        jdbcTemplate.update(sql, idQuiz);
    }

    /**
     * Encontra todas as associações (incluindo pontuação) de um quiz específico.
     * @param idQuiz ID do Quiz.
     * @return Lista de objetos QuizQuestao.
     */
    public List<QuizQuestao> findQuestoesByQuizId(long idQuiz) {
        String sql = "SELECT * FROM quiz_questao WHERE id_quiz = ?";
        return jdbcTemplate.query(sql, new Object[]{idQuiz}, rowMapper);
    }

    /**
     * Busca a pontuação de uma questão específica dentro de um quiz.
     * @param idQuestao ID da Questão.
     * @param idQuiz ID do Quiz.
     * @return Optional contendo a pontuação se a associação existir.
     */
    public Optional<Integer> findPontuacao(long idQuestao, long idQuiz) {
        String sql = "SELECT pontuacao_questao FROM quiz_questao WHERE id_questao = ? AND id_quiz = ?";
        try {
            Integer pontuacao = jdbcTemplate.queryForObject(sql, new Object[]{idQuestao, idQuiz}, Integer.class);
            return Optional.ofNullable(pontuacao);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


}