package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Alternativa;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlternativaDAO {

    private final JdbcTemplate jdbcTemplate;

    public AlternativaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Alternativa> rowMapper = (rs, rowNum) -> {
        Alternativa alt = new Alternativa();
        alt.setId_questao(rs.getLong("id_questao")); // Usa getLong para a FK
        alt.setNum_alternativa(rs.getInt("num_alternativa"));
        alt.setTexto_alternativa(rs.getString("texto_alternativa"));
        String flag = rs.getString("flg_eh_correta");
        if (flag != null && !flag.isEmpty()) {
            alt.setFlg_eh_correta(flag.charAt(0));
        }
        return alt;
    };

    public List<Alternativa> findByIdQuestao(long idQuestao) {
        String sql = "SELECT * FROM alternativa WHERE id_questao = ? ORDER BY num_alternativa";
        return jdbcTemplate.query(sql, new Object[]{idQuestao}, rowMapper);
    }

    public Optional<Alternativa> findById(long idQuestao, int numAlternativa) {
        String sql = "SELECT * FROM alternativa WHERE id_questao = ? AND num_alternativa = ?";
        try {
            Alternativa alt = jdbcTemplate.queryForObject(sql, new Object[]{idQuestao, numAlternativa}, rowMapper);
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