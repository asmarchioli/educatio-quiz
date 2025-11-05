package br.uel.educatio.quiz.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import br.uel.educatio.quiz.model.enums.TipoQuestao;

@Repository
public class QuestaoDAO {

    private final JdbcTemplate jdbcTemplate;

    public QuestaoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Questao> rowMapper = (rs, rowNum) -> {
        Questao questao = new Questao();
        questao.setId_questao(rs.getLong("id_questao"));
        questao.setEnunciado(rs.getString("enunciado"));

        String tipoQuestao = rs.getString("tipo_questao");
        if (tipoQuestao != null) {
            questao.setTipo_questao(TipoQuestao.fromString(tipoQuestao));
        }

        String visibilidade = rs.getString("visibilidade");
        if (visibilidade != null) {
            questao.setVisibilidade(Exibicao.fromString(visibilidade));
        }

        String escolaridade = rs.getString("nivel_educacional");
        if (escolaridade != null) {
            questao.setNivel_educacional(Escolaridade.fromString(escolaridade));
        }

        String dificuldade = rs.getString("nivel_dificuldade");
        if (dificuldade != null) {
            questao.setNivel_dificuldade(Dificuldade.fromString(dificuldade));
        }

        questao.setArea(rs.getLong("area"));
        questao.setProfessor_criador(rs.getLong("professor_criador"));

        return questao;
    };

    public List<Questao> findAll() {
        String sql = "SELECT * FROM questao ORDER BY id_questao";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Questao> findById(long id) {
        String sql = "SELECT * FROM questao WHERE id_questao = ?";
        try {
            Questao q = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
            return Optional.ofNullable(q);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Questao save(Questao questao) {
        if (questao.getId_questao() == 0) {
            String sql = "INSERT INTO questao (enunciado, tipo_questao, visibilidade, nivel_educacional," +
                    "nivel_dificuldade, area, professor_criador) VALUES (?, CAST(? AS TIPOQUESTAO)," +
                    "CAST(? AS EXIBICAO), CAST(? AS ESCOLARIDADE), CAST(? AS DIFICULDADE), ?, ?) RETURNING id_questao";
            Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                    questao.getEnunciado(),
                    questao.getTipo_questao().getDisplayValue(),
                    questao.getVisibilidade().getDisplayValue(),
                    questao.getNivel_educacional().getDisplayValue(),
                    questao.getNivel_dificuldade().getDisplayValue(),
                    questao.getArea(),
                    questao.getProfessor_criador()
            }, Long.class);
            questao.setId_questao(newId != null ? newId : 0L);
        } else {
            // ATUALIZAR
            String sql = "UPDATE questao SET enunciado = ?, tipo_questao = CAST(? AS TIPOQUESTAO)," +
                    "visibilidade = CAST(? AS EXIBICAO), nivel_educacional = CAST(? AS ESCOLARIDADE)," +
                    "nivel_dificuldade = CAST(? AS DIFICULDADE), area = ?, professor_criador = ? WHERE id_questao = ?";
            jdbcTemplate.update(sql,
                    questao.getEnunciado(),
                    questao.getTipo_questao().getDisplayValue(),
                    questao.getVisibilidade().getDisplayValue(),
                    questao.getNivel_educacional().getDisplayValue(),
                    questao.getNivel_dificuldade().getDisplayValue(),
                    questao.getArea(),
                    questao.getProfessor_criador(),
                    questao.getId_questao());
        }
        return questao;
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM questao WHERE id_questao = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM questao WHERE id_questao = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }

    public List<Questao> findByProfessorId(long id_prof) {
        String sql = "SELECT * FROM questao WHERE professor_criador = ?";
        List<Questao> questoes = jdbcTemplate.query(sql, new Object[]{id_prof}, rowMapper);
        return questoes;
    }

    public List<Questao> findQuestoesDoQuiz(long idQuiz) {
        String sql = "SELECT q.*, qq.pontuacao_questao " +
                     "FROM questao q " +
                     "INNER JOIN quiz_questao qq ON q.id_questao = qq.id_questao " +
                     "WHERE qq.id_quiz = ? " +
                     "ORDER BY q.id_questao";

        return jdbcTemplate.query(sql, new Object[]{idQuiz}, (rs, rowNum) -> {
            Questao questao = new Questao();
            questao.setId_questao(rs.getLong("id_questao"));
            questao.setEnunciado(rs.getString("enunciado"));

            String tipoQuestao = rs.getString("tipo_questao");
            if (tipoQuestao != null) {
                questao.setTipo_questao(TipoQuestao.fromString(tipoQuestao));
            }

            String visibilidade = rs.getString("visibilidade");
            if (visibilidade != null) {
                questao.setVisibilidade(Exibicao.fromString(visibilidade));
            }

            String escolaridade = rs.getString("nivel_educacional");
            if (escolaridade != null) {
                questao.setNivel_educacional(Escolaridade.fromString(escolaridade));
            }

            String dificuldade = rs.getString("nivel_dificuldade");
            if (dificuldade != null) {
                questao.setNivel_dificuldade(Dificuldade.fromString(dificuldade));
            }

            questao.setArea(rs.getLong("area"));
            questao.setProfessor_criador(rs.getLong("professor_criador"));
            questao.setPontuacao(rs.getInt("pontuacao_questao"));

            return questao;
        });
    }
}