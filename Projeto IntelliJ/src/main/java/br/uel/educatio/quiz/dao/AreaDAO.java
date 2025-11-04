package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AreaDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Area> areaRowMapper = new RowMapper<Area>() {
        @Override
        public Area mapRow(ResultSet rs, int rowNum) throws SQLException {
            Area area = new Area();
            area.setIdArea(rs.getLong("id_area"));
            area.setNomeArea(rs.getString("nome_area"));
            return area;
        }
    };

    public void inserir(Area area) {
        String sql = "INSERT INTO area (nome_area) VALUES (?)";
        jdbcTemplate.update(sql, area.getNomeArea());
    }

    public Optional<Area> buscarPorId(Long id) {
        String sql = "SELECT * FROM area WHERE id_area = ?";
        List<Area> areas = jdbcTemplate.query(sql, areaRowMapper, id);
        return areas.isEmpty() ? Optional.empty() : Optional.of(areas.get(0));
    }

    public List<Area> listarTodas() {
        String sql = "SELECT * FROM area ORDER BY nome_area";
        return jdbcTemplate.query(sql, areaRowMapper);
    }

    public void atualizar(Area area) {
        String sql = "UPDATE area SET nome_area = ? WHERE id_area = ?";
        jdbcTemplate.update(sql, area.getNomeArea(), area.getIdArea());
    }

    public void deletar(Long id) {
        String sql = "DELETE FROM area WHERE id_area = ?";
        jdbcTemplate.update(sql, id);
    }
}