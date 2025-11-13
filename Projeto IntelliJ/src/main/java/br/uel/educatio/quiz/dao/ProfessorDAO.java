package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Professor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ProfessorDAO {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // MELHOR PRÁTICA: Injeção por construtor (do Arquivo 2)
    public ProfessorDAO(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    // RowMapper (do Arquivo 2, é idêntico ao 1)
    private final RowMapper<Professor> rowMapper = (rs, rowNum) -> {
        Professor professor = new Professor();
        professor.setId_professor(rs.getLong("id_professor"));
        professor.setNome(rs.getString("nome"));
        professor.setEmail(rs.getString("email"));
        professor.setSenha(rs.getString("senha"));
        professor.setInstituicao_ensino(rs.getString("instituicao_ensino"));
        professor.setDescricao_profissional(rs.getString("descricao_profissional"));
        professor.setLattes(rs.getString("lattes"));
        return professor;
    };

    // --- Métodos modernos (mantidos do Arquivo 2) ---

    public List<Professor> findAll() {
        String sql = "SELECT * FROM professor ORDER BY nome";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Professor> findById(long id) {
        String sql = "SELECT * FROM professor WHERE id_professor = ?";
        try {
            Professor professor = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
            return Optional.ofNullable(professor);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Professor save(Professor professor) {
        // Verifica se o ID é nulo ou igual a zero para decidir entre INSERT e UPDATE
        if (professor.getId_professor() == null || professor.getId_professor() == 0) {
            // INSERIR: usa RETURNING para obter o novo ID
            String sql = "INSERT INTO professor (nome, email, senha, instituicao_ensino, descricao_profissional, lattes) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_professor";
            Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                    professor.getNome(),
                    professor.getEmail(),
                    professor.getSenha(),
                    professor.getInstituicao_ensino(),
                    professor.getDescricao_profissional(),
                    professor.getLattes(),
            }, Long.class);
            professor.setId_professor(newId != null ? newId : 0L);
        } else {
            // ATUALIZAR: atualiza os dados existentes
            String sql = "UPDATE professor SET nome = ?, email = ?, senha = ?, instituicao_ensino = ?, " +
                    "descricao_profissional = ?, lattes = ? WHERE id_professor = ?";
            jdbcTemplate.update(sql,
                    professor.getNome(),
                    professor.getEmail(),
                    professor.getSenha(),
                    professor.getInstituicao_ensino(),
                    professor.getDescricao_profissional(),
                    professor.getLattes(),
                    professor.getId_professor());
        }
        return professor;
    }


    public void deleteById(long id) {
        String sql = "DELETE FROM professor WHERE id_professor = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM professor WHERE id_professor = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }

    public Optional<Professor> findByEmail(String email) {
        String sql = "SELECT * FROM professor WHERE email = ?";
        try {
            Professor professor = jdbcTemplate.queryForObject(sql, new Object[]{email}, rowMapper);
            return Optional.ofNullable(professor);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int updatePassword(long idAProfessor, String novaSenha) {
        String sql = "UPDATE professor SET senha = ? WHERE id_professor = ?";
        return jdbcTemplate.update(sql, novaSenha, idAProfessor);
    }

    public List<Professor> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList(); // Retorna lista vazia imediatamente
        }

        // A query SQL usa a sintaxe :ids para o parâmetro nomeado da lista
        String sql = "SELECT * FROM professor WHERE id_professor IN (:ids)";

        // Cria um Map para passar os parâmetros nomeados
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids); // O NamedParameterJdbcTemplate sabe como expandir a lista 'ids'

        // Executa a query
        return namedParameterJdbcTemplate.query(sql, params, rowMapper);
    }

    // --- Método útil (adicionado do Arquivo 1) ---

    public boolean emailJaExiste(String email) {
        String sql = "SELECT COUNT(*) FROM professor WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{email}, Integer.class);
        return count != null && count > 0;
    }
}