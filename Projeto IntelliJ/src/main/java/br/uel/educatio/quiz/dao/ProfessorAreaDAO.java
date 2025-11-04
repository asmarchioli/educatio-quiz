package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Area; // Importado do Arquivo 1
import br.uel.educatio.quiz.model.ProfessorArea;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet; // Importado do Arquivo 1
import java.sql.SQLException; // Importado do Arquivo 1
import java.util.List;

@Repository
public class ProfessorAreaDAO {

    private final JdbcTemplate jdbcTemplate;

    // MELHOR PRÁTICA: Injeção por construtor (do Arquivo 2)
    public ProfessorAreaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper para ProfessorArea (lambda do Arquivo 2)
    private final RowMapper<ProfessorArea> professorAreaRowMapper = (rs, rowNum) -> {
        ProfessorArea pa = new ProfessorArea();
        pa.setId_professor(rs.getLong("id_professor"));
        pa.setId_area(rs.getLong("id_area"));
        return pa;
    };

    // RowMapper para Area (Adicionado do Arquivo 1)
    private final RowMapper<Area> areaRowMapper = new RowMapper<Area>() {
        @Override
        public Area mapRow(ResultSet rs, int rowNum) throws SQLException {
            Area area = new Area();
            area.setIdArea(rs.getLong("id_area"));
            area.setNomeArea(rs.getString("nome_area"));
            return area;
        }
    };

    // --- Métodos do Arquivo 2 (Nomes modernos e 'ON CONFLICT') ---

    public int addAreaToProfessor(long idProfessor, long idArea) {
        // Usa INSERT ... ON CONFLICT DO NOTHING para evitar erro se a relação já existir
        String sql = "INSERT INTO professor_area (id_professor, id_area) VALUES (?, ?) ON CONFLICT (id_professor, id_area) DO NOTHING";
        return jdbcTemplate.update(sql, idProfessor, idArea);
    }

    public void removeAreaFromProfessor(long idProfessor, long idArea) {
        String sql = "DELETE FROM professor_area WHERE id_professor = ? AND id_area = ?";
        jdbcTemplate.update(sql, idProfessor, idArea);
    }

    public void removeAllAreasFromProfessor(long idProfessor) {
        String sql = "DELETE FROM professor_area WHERE id_professor = ?";
        jdbcTemplate.update(sql, idProfessor);
    }

    // Retorna List<String> (Nomes das áreas)
    public List<String> findAreasByProfessorId(long idProfessor) {
        String sql = "SELECT a.nome_area FROM area a " +
                "JOIN professor_area p ON a.id_area = p.id_area " +
                "WHERE p.id_professor = ?";
        return jdbcTemplate.queryForList(sql, String.class, new Object[]{idProfessor});
    }

    public List<Long> findProfessorIdsByAreaId(long idArea) {
        String sql = "SELECT id_professor FROM professor_area WHERE id_area = ?";
        return jdbcTemplate.queryForList(sql, Long.class, new Object[]{idArea});
    }

    // --- Métodos Adicionados do Arquivo 1 (Funcionalidades extras) ---

    public void inserirVarias(Long idProfessor, List<Long> idsAreas) {
        String sql = "INSERT INTO professor_area (id_professor, id_area) VALUES (?, ?)";
        // TODO: Considerar usar batchUpdate para melhor performance
        for (Long idArea : idsAreas) {
            jdbcTemplate.update(sql, idProfessor, idArea);
        }
    }

    // Retorna List<Area> (Objetos completos)
    public List<Area> buscarAreasDoProfessor(Long idProfessor) {
        String sql = "SELECT a.* FROM area a " +
                "INNER JOIN professor_area pa ON a.id_area = pa.id_area " +
                "WHERE pa.id_professor = ? " +
                "ORDER BY a.nome_area";
        return jdbcTemplate.query(sql, areaRowMapper, idProfessor);
    }

    // Retorna List<Long> (IDs das áreas)
    public List<Long> buscarIdsAreasDoProfessor(Long idProfessor) {
        String sql = "SELECT id_area FROM professor_area WHERE id_professor = ?";
        return jdbcTemplate.queryForList(sql, Long.class, idProfessor);
    }

    public boolean professorPossuiArea(Long idProfessor, Long idArea) {
        String sql = "SELECT COUNT(*) FROM professor_area WHERE id_professor = ? AND id_area = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idProfessor, idArea);
        return count != null && count > 0;
    }
}