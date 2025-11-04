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

    // RowMapper para Alternativa (COM A CORREÇÃO DE NULO)
    private static final class AlternativaRowMapper implements RowMapper<Alternativa> {
        @Override
        public Alternativa mapRow(ResultSet rs, int rowNum) throws SQLException {
            Alternativa alt = new Alternativa();
            alt.setId_questao(rs.getLong("id_questao"));
            alt.setNum_alternativa(rs.getLong("num_alternativa"));
            alt.setTexto_alternativa(rs.getString("texto_alternativa"));

            // *** CORREÇÃO APLICADA AQUI ***
            // Verifica se o valor do banco não é nulo antes de pegar o charAt(0)
            String flag = rs.getString("flg_eh_correta");
            if (flag != null && !flag.isEmpty()) {
                alt.setFlg_eh_correta(flag.charAt(0));
            }

            return alt;
        }
    }

    // --- Métodos que SÓ existiam no seu Arquivo 1 ---

    // Busca todas as alternativas de uma questão
    public List<Alternativa> findAlternativasByQuestaoId(Long questaoId) {
        String sql = "SELECT * FROM ALTERNATIVA WHERE id_questao = ?";
        return jdbcTemplate.query(sql, new Object[]{questaoId}, new AlternativaRowMapper());
    }

    // Busca a alternativa correta para uma questão (para correção)
    public Alternativa findAlternativaCorreta(Long questaoId) {
        // Para "Preencher Lacuna", a "alternativa" é a própria resposta correta
        String sql = "SELECT * FROM ALTERNATIVA WHERE id_questao = ? AND flg_eh_correta = 'S'";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{questaoId}, new AlternativaRowMapper());
        } catch (Exception e) {
            return null; // Nenhuma correta encontrada
        }
    }

    // --- Métodos que existiam em AMBOS os arquivos ---

    public List<Alternativa> findByIdQuestao(long idQuestao) {
        String sql = "SELECT * FROM alternativa WHERE id_questao = ? ORDER BY num_alternativa";
        // Usa a nova instância do RowMapper
        return jdbcTemplate.query(sql, new Object[]{idQuestao}, new AlternativaRowMapper());
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
// A chave '}' extra foi removida daqui.