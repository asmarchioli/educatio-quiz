package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.ProfessorArea;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfessorAreaDAO {

    private final JdbcTemplate jdbcTemplate;

    public ProfessorAreaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ProfessorArea> rowMapper = (rs, rowNum) -> {
        ProfessorArea pa = new ProfessorArea();
        pa.setId_professor(rs.getLong("id_professor"));
        pa.setId_area(rs.getLong("id_area"));
        return pa;
    };

    /**
     * Adiciona uma associação entre um professor e uma área.
     * @param idProfessor ID do Professor.
     * @param idArea ID da Área.
     * @return O número de linhas afetadas (geralmente 1).
     */
    public int addAreaToProfessor(long idProfessor, long idArea) {
        // Usamos INSERT ... ON CONFLICT DO NOTHING para evitar erro se a relação já existir
        String sql = "INSERT INTO professor_area (id_professor, id_area) VALUES (?, ?) ON CONFLICT (id_professor, id_area) DO NOTHING";
        return jdbcTemplate.update(sql, idProfessor, idArea);
    }

    /**
     * Remove uma associação entre um professor e uma área.
     * @param idProfessor ID do Professor.
     * @param idArea ID da Área.
     */
    public void removeAreaFromProfessor(long idProfessor, long idArea) {
        String sql = "DELETE FROM professor_area WHERE id_professor = ? AND id_area = ?";
        jdbcTemplate.update(sql, idProfessor, idArea);
    }

    /**
     * Remove todas as associações de um professor. Útil ao deletar um professor.
     * @param idProfessor ID do Professor.
     */
    public void removeAllAreasFromProfessor(long idProfessor) {
        String sql = "DELETE FROM professor_area WHERE id_professor = ?";
        jdbcTemplate.update(sql, idProfessor);
    }

    /**
     * Encontra todos os IDs das áreas associadas a um professor.
     * @param idProfessor ID do Professor.
     * @return Lista de IDs das Áreas.
     */
    public List<Long> findAreaIdsByProfessorId(long idProfessor) {
        String sql = "SELECT id_area FROM professor_area WHERE id_professor = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{idProfessor}, Long.class);
    }

    /**
     * Encontra todos os IDs dos professores associados a uma área.
     * @param idArea ID da Área.
     * @return Lista de IDs dos Professores.
     */
    public List<Long> findProfessorIdsByAreaId(long idArea) {
        String sql = "SELECT id_professor FROM professor_area WHERE id_area = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{idArea}, Long.class);
    }
}