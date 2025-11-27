package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AlunoDAO {

    private final JdbcTemplate jdbcTemplate;


    public AlunoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private final RowMapper<Aluno> rowMapper = (rs, rowNum) ->  {
        Aluno aluno = new Aluno();
        aluno.setId_aluno(rs.getLong("id_aluno"));
        aluno.setNome(rs.getString("nome"));
        aluno.setEmail(rs.getString("email"));
        aluno.setSenha(rs.getString("senha"));


        String escolaridade = rs.getString("nivel_educacional"); 
        if(escolaridade != null) {
            aluno.setNivel_educacional(Escolaridade.fromString(escolaridade));
        }
        return aluno;
    };


    public List<Aluno> findAll() {
        String sql = "SELECT * FROM aluno ORDER BY nome";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Aluno> findById(long id) {
        String sql = "SELECT * FROM aluno WHERE id_aluno = ?";
        try {
            Aluno aluno = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
            return Optional.ofNullable(aluno);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Aluno save(Aluno aluno) {
        // Verifica se o ID é nulo ou igual a zero para decidir entre inserção e atualização
        if (aluno.getId_aluno() == null || aluno.getId_aluno() == 0) {
            // Lógica de Inserir
            String sql = "INSERT INTO aluno (nome, email, senha, nivel_educacional) " +
                    "VALUES (?, ?, ?, CAST(? AS ESCOLARIDADE)) RETURNING id_aluno";
            Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                    aluno.getNome(),
                    aluno.getEmail(),
                    aluno.getSenha(),
                    aluno.getNivel_educacional() != null ? aluno.getNivel_educacional().getDisplayValue() : null
            }, Long.class);
            aluno.setId_aluno(newId != null ? newId : 0L);
        } else {
            // Lógica de Atualizar
            String sql = "UPDATE aluno SET nome = ?, email = ?, senha = ?, nivel_educacional = CAST(? AS ESCOLARIDADE) " +
                    "WHERE id_aluno = ?";
            jdbcTemplate.update(sql,
                    aluno.getNome(),
                    aluno.getEmail(),
                    aluno.getSenha(),
                    aluno.getNivel_educacional() != null ? aluno.getNivel_educacional().getDisplayValue() : null,
                    aluno.getId_aluno());
        }
        return aluno;
    }


    public void deleteById(long id) {
        String sql = "DELETE FROM aluno WHERE id_aluno = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM aluno WHERE id_aluno = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }

    public Optional<Aluno> findByEmail(String email) {
        String sql = "SELECT * FROM aluno WHERE email = ?";
        try {
            Aluno aluno = jdbcTemplate.queryForObject(sql, new Object[]{email}, rowMapper);
            return Optional.ofNullable(aluno);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Aluno> findByNomeContainingIgnoreCase(String nome) {
        String sql = "SELECT * FROM aluno WHERE nome ILIKE ?"; // ILIKE é o LIKE case-insensitive do PostgreSQL
        return jdbcTemplate.query(sql, new Object[]{"%" + nome + "%"}, rowMapper);
    }

    public int updatePassword(long idAluno, String novaSenha) {
        String sql = "UPDATE aluno SET senha = ? WHERE id_aluno = ?";
        return jdbcTemplate.update(sql, novaSenha, idAluno);
    }

   

    public boolean emailJaExiste(String email) {
        String sql = "SELECT COUNT(*) FROM aluno WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{email}, Integer.class);
        return count != null && count > 0;
    }
}