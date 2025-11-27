package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.QuizQuestao;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
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
     * @param id_questao ID da Questão.
     * @param id_quiz ID do Quiz.
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
     * @param id_questao ID da Questão.
     * @param id_quiz ID do Quiz.
     */
    public void removeQuestaoFromQuiz(long id_questao, long id_quiz) {
        System.out.println("id_questao: " + id_questao + " id_quiz: " + id_quiz);
        String sql = "DELETE FROM quiz_questao WHERE id_questao = ? AND id_quiz = ?";
        jdbcTemplate.update(sql, id_questao, id_quiz);
    }

    public void removeQuestaoFromAllQuizzes(long id_questao){
        String sql = "DELETE FROM quiz_questao WHERE id_questao = ?";
        jdbcTemplate.update(sql, id_questao);
    }

    
    /**
     * Remove todas as questões de um quiz. Útil ao deletar um quiz.
     * @param id_quiz ID do Quiz.
     */
    public void removeAllQuestoesFromQuiz(long id_quiz) {
        String sql = "DELETE FROM quiz_questao WHERE id_quiz = ?";
        jdbcTemplate.update(sql, id_quiz);
    }
   
    
    /**
     * Encontra todas as associações (incluindo pontuação) de um quiz específico.
     * @param id_quiz ID do Quiz.
     * @return Lista de objetos QuizQuestao.
     */
    public List<QuizQuestao> findQuestoesByQuizIdFiltrado(long id_quiz, TipoQuestao tipo, String termoBusca) {
        // StringBuilder para poder usar o .append()
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT qq.id_quiz, qq.id_questao, qq.pontuacao_questao ");
        sql.append("FROM quiz_questao as qq ");
        sql.append("LEFT JOIN questao as q ON qq.id_questao = q.id_questao ");
        sql.append("WHERE qq.id_quiz = ? "); // Note o espaço no final e SEM ';'

        List<Object> params = new java.util.ArrayList<>();
        params.add(id_quiz);

        if (tipo != null) {
            sql.append("AND q.tipo_questao = CAST(? AS TIPOQUESTAO) "); 
            params.add(tipo.getDisplayValue());
        }

        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            sql.append("AND LOWER(q.enunciado) LIKE LOWER(?) ");
            params.add("%" + termoBusca.trim() + "%");
        }

        sql.append("ORDER BY q.enunciado");

        return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
    }

    
    /**
     * Busca a pontuação de uma questão específica dentro de um quiz.
     * @param id_questao ID da Questão.
     * @param id_quiz ID do Quiz.
     * @return Optional contendo a pontuação se a associação existir.
     */
    public Optional<Integer> findPontuacao(long id_questao, long id_quiz) {
        String sql = "SELECT pontuacao_questao FROM quiz_questao WHERE id_questao = ? AND id_quiz = ?";
        try {
            Integer pontuacao = jdbcTemplate.queryForObject(sql, new Object[]{id_questao, id_quiz}, Integer.class);
            return Optional.ofNullable(pontuacao);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }    

    public int getPontuacaoQuiz(long id_quiz){
        String sql = "SELECT SUM(pontuacao_questao) FROM quiz_questao WHERE id_quiz = ?";
        Integer pontuacao = jdbcTemplate.queryForObject(sql, new Object[]{id_quiz}, Integer.class);
        return pontuacao != null ? pontuacao : 0;
    }

    public void atualizarPontuacao(long id_questao, long id_quiz, int novaPontuacao) {
        String sql = "UPDATE quiz_questao SET pontuacao_questao = ? WHERE id_questao = ? AND id_quiz = ?";
        jdbcTemplate.update(sql, novaPontuacao, id_questao, id_quiz);
    }

}