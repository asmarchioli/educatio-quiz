package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.*;
import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuestaoDAO {

    private final JdbcTemplate jdbcTemplate;

    public QuestaoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Questao> rowMapper = (rs, rowNum) -> {
        Questao q = new Questao();
        q.setId_questao(rs.getLong("id_questao")); // Usa getLong
        q.setEnunciado(rs.getString("enunciado"));
        q.setTipo_questao(TipoQuestao.fromString(rs.getString("tipo_questao")));
        q.setVisibilidade(Exibicao.fromString(rs.getString("visibilidade")));
        q.setNivel_educacional(Escolaridade.fromString(rs.getString("nivel_educacional")));
        q.setNivel_dificuldade(Dificuldade.fromString(rs.getString("nivel_dificuldade")));
        q.setArea(rs.getLong("area")); // FK para BIGINT usa getLong
        q.setProfessor_criador(rs.getLong("professor_criador")); // FK para BIGINT usa getLong
        return q;
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

    public void deleteById(long id) { // Parâmetro agora é long
        String sql = "DELETE FROM questao WHERE id_questao = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long id) { // Parâmetro agora é long
        String sql = "SELECT COUNT(*) FROM questao WHERE id_questao = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }
}