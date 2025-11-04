package br.uel.educatio.quiz.dao;

import java.sql.ResultSet; // Importado do Arquivo 2
import java.sql.SQLException; // Importado do Arquivo 2
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

    // --- RowMapper Básico (do Arquivo 1) ---
    // Usado para queries simples na tabela QUESTAO
    private final RowMapper<Questao> rowMapper = (rs, rowNum) -> {
        Questao q = new Questao();
        q.setId_questao(rs.getLong("id_questao"));
        q.setEnunciado(rs.getString("enunciado"));
        q.setTipo_questao(TipoQuestao.fromString(rs.getString("tipo_questao")));
        q.setVisibilidade(Exibicao.fromString(rs.getString("visibilidade")));
        q.setNivel_educacional(Escolaridade.fromString(rs.getString("nivel_educacional")));
        q.setNivel_dificuldade(Dificuldade.fromString(rs.getString("nivel_dificuldade")));
        q.setArea(rs.getLong("area"));
        q.setProfessor_criador(rs.getLong("professor_criador"));
        return q;
    };

    // --- RowMapper com JOIN (do Arquivo 2) ---
    // Usado para a query que busca pontuação da tabela QUIZ_QUESTAO
    private static final class QuestaoRowMapper implements RowMapper<Questao> {
        @Override
        public Questao mapRow(ResultSet rs, int rowNum) throws SQLException {
            Questao questao = new Questao();
            questao.setId_questao(rs.getLong("id_questao"));
            questao.setEnunciado(rs.getString("enunciado"));
            questao.setTipo_questao(TipoQuestao.fromString(rs.getString("tipo_questao")));
            questao.setVisibilidade(Exibicao.fromString(rs.getString("visibilidade")));
            questao.setNivel_educacional(Escolaridade.fromString(rs.getString("nivel_educacional")));
            questao.setNivel_dificuldade(Dificuldade.fromString(rs.getString("nivel_dificuldade")));
            questao.setArea(rs.getLong("area"));
            questao.setProfessor_criador(rs.getLong("professor_criador"));

            // Lógica extra para o campo 'pontuacao_questao' do JOIN
            if (columnExists(rs, "pontuacao_questao")) {
                questao.setPontuacao(rs.getInt("pontuacao_questao"));
            }
            return questao;
        }

        // Helper para checar se a coluna do JOIN existe
        private boolean columnExists(ResultSet rs, String columnName) {
            try {
                rs.findColumn(columnName);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
    }

    // --- Métodos CRUD Básicos (do Arquivo 1) ---
    // (Usam o 'rowMapper' simples)

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

    // --- Método com JOIN (do Arquivo 2) ---
    // (Usa o 'new QuestaoRowMapper()' complexo)

    public List<Questao> findQuestoesByQuizId(Long quizId) {
        String sql = "SELECT q.*, qq.pontuacao_questao " +
                "FROM QUESTAO q " +
                "JOIN QUIZ_QUESTAO qq ON q.id_questao = qq.id_questao " +
                "WHERE qq.id_quiz = ?";
        // Note que este método usa o RowMapper da classe interna
        return jdbcTemplate.query(sql, new Object[]{quizId}, new QuestaoRowMapper());
    }
}