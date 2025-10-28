package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class QuizDAO {

    private final JdbcTemplate jdbcTemplate;

    public QuizDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Quiz> rowMapper = (rs, rowNum) -> {
        Quiz quiz = new Quiz();
        quiz.setId_quiz(rs.getLong("id_quiz"));
        quiz.setTitulo(rs.getString("titulo"));
        quiz.setPin_acesso(rs.getString("pin_acesso"));
        quiz.setDescricao(rs.getString("descricao"));
        quiz.setVisibilidade(Exibicao.fromString(rs.getString("visibilidade")));
        quiz.setNivel_educacional(Escolaridade.fromString(rs.getString("nivel_educacional")));
        quiz.setProfessor_criador(rs.getLong("professor_criador"));
        quiz.setArea(rs.getLong("area"));
        Date dataCriacaoSql = rs.getDate("data_criacao");
        if (dataCriacaoSql != null) {
            quiz.setData_criacao(dataCriacaoSql.toLocalDate());
        }
        return quiz;
    };

    public List<Quiz> findAll() {
        String sql = "SELECT * FROM quiz ORDER BY titulo";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Quiz> findById(long id) {
        String sql = "SELECT * FROM quiz WHERE id_quiz = ?";
        try {
            Quiz quiz = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
            return Optional.ofNullable(quiz);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Quiz save(Quiz quiz) {
        if (quiz.getId_quiz() == 0) {
            // INSERIR
            String sql = "INSERT INTO quiz (titulo, pin_acesso, descricao, visibilidade, nivel_educacional," +
                    "professor_criador, area, data_criacao) VALUES" +
                    "(?, ?, ?, CAST(? AS EXIBICAO), CAST(? AS ESCOLARIDADE), ?, ?, ?) RETURNING id_quiz";
            Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                    quiz.getTitulo(),
                    quiz.getPin_acesso(),
                    quiz.getDescricao(),
                    quiz.getVisibilidade().getDisplayValue(),
                    quiz.getNivel_educacional().getDisplayValue(),
                    quiz.getProfessor_criador(),
                    quiz.getArea(),
                    quiz.getData_criacao()
            }, Long.class);
            quiz.setId_quiz(newId != null ? newId : 0L);
        } else {
            // ATUALIZAR
            String sql = "UPDATE quiz SET titulo = ?, pin_acesso = ?, descricao = ?," +
                    "visibilidade = CAST(? AS EXIBICAO),nivel_educacional = CAST(? AS ESCOLARIDADE)," +
                    "professor_criador = ?, area = ?, data_criacao = ? WHERE id_quiz = ?";
            jdbcTemplate.update(sql,
                    quiz.getTitulo(),
                    quiz.getPin_acesso(),
                    quiz.getDescricao(),
                    quiz.getVisibilidade().getDisplayValue(),
                    quiz.getNivel_educacional().getDisplayValue(),
                    quiz.getProfessor_criador(),
                    quiz.getArea(),
                    quiz.getData_criacao(),
                    quiz.getId_quiz());
        }
        return quiz;
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM quiz WHERE id_quiz = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM quiz WHERE id_quiz = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }


    public Optional<Quiz> findByPinAcesso(String pin) {
        String sql = "SELECT * FROM quiz WHERE pin_acesso = ?";
        try {
            Quiz quiz = jdbcTemplate.queryForObject(sql, new Object[]{pin}, rowMapper);
            return Optional.ofNullable(quiz);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Quiz> findByProfessorCriador(long professorId) {
        String sql = "SELECT * FROM quiz WHERE professor_criador = ? ORDER BY data_criacao DESC";
        return jdbcTemplate.query(sql, new Object[]{professorId}, rowMapper);


    }
}