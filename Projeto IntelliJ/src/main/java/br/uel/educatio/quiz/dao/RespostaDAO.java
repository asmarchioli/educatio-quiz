package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Resposta;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter; // Importado do Arquivo 2
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement; // Importado do Arquivo 2
import java.sql.SQLException; // Importado do Arquivo 2
import java.sql.Types; // Importado do Arquivo 2
import java.util.List;
import java.util.Optional;

@Repository
public class RespostaDAO {

    private final JdbcTemplate jdbcTemplate;

    public RespostaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- RowMapper CORRIGIDO (Mesclando campos dos dois arquivos) ---
    private final RowMapper<Resposta> rowMapper = (rs, rowNum) -> {
        Resposta r = new Resposta();
        r.setId_resposta(rs.getLong("id_resposta"));
        r.setId_questao(rs.getLong("id_questao"));
        r.setId_quiz(rs.getLong("id_quiz"));
        r.setId_aluno(rs.getLong("id_aluno"));
        r.setTentativa(rs.getInt("tentativa"));
        r.setPontuacao_aluno(rs.getInt("pontuacao_aluno"));

        String flag = rs.getString("flg_acertou");
        if (flag != null && !flag.isEmpty()) {
            r.setFlg_acertou(flag.charAt(0));
        }

        // --- CORREÇÃO (Campos que só existiam no saveBatch) ---
        r.setResposta_aluno_texto(rs.getString("resposta_aluno_texto"));
        int respostaNum = rs.getInt("resposta_aluno_num");
        if (!rs.wasNull()) {
            r.setResposta_aluno_num(respostaNum);
        }
        // --- Fim da Correção ---

        return r;
    };

    // --- Métodos de Busca (do Arquivo 1) ---
    public Optional<Resposta> findById(long id) {
        String sql = "SELECT * FROM resposta WHERE id_resposta = ?";
        try {
            Resposta r = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Resposta> findByIdAlunoAndIdQuiz(long idAluno, long idQuiz) {
        String sql = "SELECT * FROM resposta WHERE id_aluno = ? AND id_quiz = ? ORDER BY id_questao, tentativa";
        return jdbcTemplate.query(sql, new Object[]{idAluno, idQuiz}, rowMapper);
    }

    public Optional<Resposta> findByKeyComposta(long idQuestao, long idQuiz, long idAluno, int tentativa) {
        String sql = "SELECT * FROM resposta WHERE id_questao = ? AND id_quiz = ? AND id_aluno = ? AND tentativa = ?";
        try {
            Resposta r = jdbcTemplate.queryForObject(sql, new Object[]{idQuestao, idQuiz, idAluno, tentativa}, rowMapper);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // --- Método 'save' singular (CORRIGIDO do Arquivo 1) ---
    public Resposta save(Resposta resposta) {
        // SQL CORRIGIDO: Adicionado os campos de resposta do aluno
        String sql = "INSERT INTO resposta (id_questao, id_quiz, id_aluno, tentativa, pontuacao_aluno, flg_acertou, resposta_aluno_texto, resposta_aluno_num)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_resposta";
        Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                resposta.getId_questao(),
                resposta.getId_quiz(),
                resposta.getId_aluno(),
                resposta.getTentativa(),
                resposta.getPontuacao_aluno(),
                String.valueOf(resposta.getFlg_acertou()),
                // --- CORREÇÃO (Campos que só existiam no saveBatch) ---
                resposta.getResposta_aluno_texto(),
                resposta.getResposta_aluno_num()
                // --- Fim da Correção ---
        }, Long.class);
        resposta.setId_resposta(newId != null ? newId : 0L);
        return resposta;
    }

    // --- Método 'saveBatch' (Adicionado do Arquivo 2) ---
    public void saveBatch(List<Resposta> respostas) {
        String sql = "INSERT INTO RESPOSTA (id_questao, id_quiz, id_aluno, tentativa, pontuacao_aluno, flg_acertou, resposta_aluno_texto, resposta_aluno_num)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // TODO: A lógica de tentativa provavelmente deveria vir do Service
        final int TENTATIVA_ATUAL = 1;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Resposta r = respostas.get(i);
                ps.setLong(1, r.getId_questao());
                ps.setLong(2, r.getId_quiz());
                ps.setLong(3, r.getId_aluno());
                ps.setInt(4, TENTATIVA_ATUAL); // Usando a lógica de tentativa
                ps.setInt(5, r.getPontuacao_aluno());
                ps.setString(6, String.valueOf(r.getFlg_acertou()));

                // Lida com os dois tipos de resposta (texto ou número da alternativa)
                if (r.getResposta_aluno_texto() != null) {
                    ps.setString(7, r.getResposta_aluno_texto());
                } else {
                    ps.setNull(7, Types.VARCHAR);
                }

                if (r.getResposta_aluno_num() != null) {
                    ps.setInt(8, r.getResposta_aluno_num());
                } else {
                    ps.setNull(8, Types.INTEGER);
                }
            }

            @Override
            public int getBatchSize() {
                return respostas.size();
            }
        });
    }

    // --- Método Delete (do Arquivo 1) ---
    public void deleteById(long id) {
        String sql = "DELETE FROM resposta WHERE id_resposta = ?";
        jdbcTemplate.update(sql, id);
    }
}