package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Alternativa;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AlternativaDAO {

    private final JdbcTemplate jdbcTemplate;

    public AlternativaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final class AlternativaRowMapper implements RowMapper<Alternativa> {
        @Override
        public Alternativa mapRow(ResultSet rs, int rowNum) throws SQLException {
            Alternativa alt = new Alternativa();
            alt.setId_questao(rs.getLong("id_questao"));
            alt.setNum_alternativa(rs.getLong("num_alternativa"));
            alt.setTexto_alternativa(rs.getString("texto_alternativa"));

            // A correção de bug
            String flag = rs.getString("flg_eh_correta");
            if (flag != null && !flag.isEmpty()) {
                alt.setFlg_eh_correta(flag.charAt(0));
            }

            return alt;
        }
    }

    public List<Alternativa> findByQuestaoId(long idQuestao) {
        String sql = "SELECT * FROM alternativa WHERE id_questao = ? ORDER BY num_alternativa";
        // Usa o RowMapper seguro
        return jdbcTemplate.query(sql, new Object[]{idQuestao}, new AlternativaRowMapper());
    }


    public Optional<Alternativa> findAlternativaCorreta(long idQuestao) {
        //String sql = "SELECT * FROM alternativa WHERE id_questao = ? AND flg_eh_correta = 'S'";
        String sql = "SELECT a.* FROM alternativa a " +
            "JOIN questao q ON a.id_questao = q.id_questao " +
            "WHERE a.id_questao = ? AND (UPPER(a.flg_eh_correta) = 'S' " +
            "OR q.tipo_questao = 'Preencher Lacuna')";
        // Usa o RowMapper seguro
        List<Alternativa> alternativas = jdbcTemplate.query(sql, new Object[]{idQuestao}, new AlternativaRowMapper());
        return alternativas.isEmpty() ? Optional.empty() : Optional.of(alternativas.get(0));
    }

 
    public Optional<Alternativa> findById(long idQuestao, int numAlternativa) {
        String sql = "SELECT * FROM alternativa WHERE id_questao = ? AND num_alternativa = ?";
        try {
            Alternativa alt = jdbcTemplate.queryForObject(sql, new Object[]{idQuestao, numAlternativa}, new AlternativaRowMapper());
            return Optional.ofNullable(alt);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int save(Alternativa alternativa) {
        String sql = "INSERT INTO alternativa (id_questao, num_alternativa, texto_alternativa, flg_eh_correta) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                alternativa.getId_questao(),
                alternativa.getNum_alternativa(),
                alternativa.getTexto_alternativa(),
                String.valueOf(alternativa.getFlg_eh_correta())
        );
    }

    public int update(Alternativa alternativa) {
        String sql = "UPDATE alternativa SET texto_alternativa = ?, flg_eh_correta = ? WHERE id_questao = ? AND num_alternativa = ?";
        return jdbcTemplate.update(sql,
                alternativa.getTexto_alternativa(),
                String.valueOf(alternativa.getFlg_eh_correta()),
                alternativa.getId_questao(),
                alternativa.getNum_alternativa()
        );
    }

public void deleteById(long idQuestao, int numAlternativa) {
        String sql = "DELETE FROM alternativa WHERE id_questao = ? AND num_alternativa = ?";
        jdbcTemplate.update(sql, idQuestao, numAlternativa);
    }

    public void deleteByIdQuestao(long idQuestao) {
        String sql = "DELETE FROM alternativa WHERE id_questao = ?";
        jdbcTemplate.update(sql, idQuestao);
    }
}