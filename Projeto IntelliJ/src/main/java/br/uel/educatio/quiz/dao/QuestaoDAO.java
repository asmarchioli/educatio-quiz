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

import java.util.ArrayList;

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
            try {
                questao.setTipo_questao(TipoQuestao.fromString(tipoQuestao));
            } catch (IllegalArgumentException e) {
                
                try {
                    questao.setTipo_questao(TipoQuestao.valueOf(tipoQuestao));
                } catch (Exception ex) {
                    questao.setTipo_questao(null); 
                }
            }
        }

        String visibilidade = rs.getString("visibilidade");
        if (visibilidade != null) {
            try {
                questao.setVisibilidade(Exibicao.fromString(visibilidade));
            } catch (Exception e) {
                questao.setVisibilidade(null);
            }
        }

        String escolaridade = rs.getString("nivel_educacional");
        if (escolaridade != null) {
            try {
                questao.setNivel_educacional(Escolaridade.fromString(escolaridade));
            } catch (Exception e) {
                questao.setNivel_educacional(null);
            }
        }
        
        String dificuldade = rs.getString("nivel_dificuldade");
        if (dificuldade != null) {
            try {
                questao.setNivel_dificuldade(Dificuldade.fromString(dificuldade));
            } catch (Exception e) {
                questao.setNivel_dificuldade(null);
            }
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

 

    // Método para buscar questões públicas compatíveis com o quiz
    public List<Questao> findPublicasCompativeis(Long idArea, String nivelEducacional, TipoQuestao tipo, String termoBusca) {
        StringBuilder sql = new StringBuilder();

        // Base da Query
        sql.append("SELECT * FROM questao WHERE visibilidade = 'Público' ");
        sql.append("AND area = ? AND nivel_educacional = CAST(? as ESCOLARIDADE) ");

        List<Object> params = new ArrayList<>();
        params.add(idArea);
        params.add(nivelEducacional);
        

        // Filtro de Tipo ---
        if (tipo != null) {

            sql.append("AND tipo_questao = CAST(? as TIPOQUESTAO) "); 
            params.add(tipo.getDisplayValue()); 
        }

        // Filtro de Texto (Enunciado)
        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            sql.append("AND LOWER(enunciado) LIKE LOWER(?) ");
            params.add("%" + termoBusca.trim() + "%");
        }

        sql.append("ORDER BY enunciado");

        return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
    }

    // Método para buscar questões por filtros (usado em banco_questoes.html)
    public List<Questao> findByFiltros(Long idProf, TipoQuestao tipo, String termoBusca) {
        // SQL base: sempre filtra pelo professor
        StringBuilder sql = new StringBuilder("SELECT * FROM questao WHERE professor_criador = ?");
        List<Object> params = new java.util.ArrayList<>();
        params.add(idProf);

        // Filtro opcional por Tipo
        if (tipo != null) {
            sql.append(" AND tipo_questao = CAST(? AS TIPOQUESTAO)"); 
            params.add(tipo.getDisplayValue());
        }

        // Filtro opcional por Termo de Busca (Enunciado) - Case Insensitive
        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            sql.append(" AND LOWER(enunciado) LIKE LOWER(?)");
            params.add("%" + termoBusca.trim() + "%");
        }
        

        sql.append(" ORDER BY enunciado");

        // Executa a query com os parâmetros dinâmicos
        return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
    }

    public List<Questao> findAllByFiltros(Long area, Dificuldade dificuldade,  TipoQuestao tipo, String termoBusca){
        StringBuilder sql = new StringBuilder("SELECT * FROM questao WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        
        if (area != null){
            sql.append(" AND area = ?");
            params.add(area);
        }

        if (dificuldade != null){
            sql.append(" AND nivel_dificuldade = CAST(? AS DIFICULDADE)");
        }
        
        if (tipo != null) {
            sql.append(" AND tipo_questao = CAST(? AS TIPOQUESTAO)"); 
            params.add(tipo.getDisplayValue());
        }

        // Filtro opcional por Termo de Busca (Enunciado) - Case Insensitive
        if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            sql.append(" AND LOWER(enunciado) LIKE LOWER(?)");
            params.add("%" + termoBusca.trim() + "%");
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
    }
    

    //Usado em criar_questao.html
    public Questao save(Questao questao) {
        if (questao.getId_questao() == null || questao.getId_questao() == 0) {
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
            // Atualizar
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

    public void deleteById(long id){
        jdbcTemplate.update("DELETE FROM RESPOSTA WHERE id_questao = ?", id);
        jdbcTemplate.update("DELETE FROM QUIZ_QUESTAO WHERE id_questao = ?", id);
        jdbcTemplate.update("DELETE FROM ALTERNATIVA WHERE id_questao = ?", id);

        String sql = "DELETE FROM questao WHERE id_questao = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM questao WHERE id_questao = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }

    public List<Questao> findByArea(long id_area){
        String sql = "SELECT * FROM questao WHERE area = ?";
        return jdbcTemplate.query(sql, new Object[]{id_area}, rowMapper);
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