package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.Resposta;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class RespostaDAO {

    private final JdbcTemplate jdbcTemplate;

    public RespostaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Resposta> rowMapper = (rs, rowNum) -> {
        Resposta r = new Resposta();
        r.setId_resposta(rs.getLong("id_resposta"));
        r.setId_questao(rs.getLong("id_questao"));
        r.setId_quiz(rs.getLong("id_quiz"));
        r.setId_aluno(rs.getLong("id_aluno"));
        r.setTentativa(rs.getInt("tentativa"));
        r.setPontuacao_aluno(rs.getInt("pontuacao_aluno"));

        // Correção de bug (não presente no Arquivo 2)
        String flag = rs.getString("flg_acertou");
        if (flag != null && !flag.isEmpty()) {
            r.setFlg_acertou(flag.charAt(0));
        }

        r.setResposta_aluno_texto(rs.getString("resposta_aluno_texto"));
        int respostaNum = rs.getInt("resposta_aluno_num");
        // Correção para números nulos
        if (!rs.wasNull()) {
            r.setResposta_aluno_num(respostaNum);
        }

        return r;
    };

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

    public Resposta save(Resposta resposta) {
        String sql = "INSERT INTO resposta (id_questao, id_quiz, id_aluno, tentativa, pontuacao_aluno, flg_acertou, resposta_aluno_texto, resposta_aluno_num)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_resposta";
        Long newId = jdbcTemplate.queryForObject(sql, new Object[]{
                resposta.getId_questao(),
                resposta.getId_quiz(),
                resposta.getId_aluno(),
                resposta.getTentativa(),
                resposta.getPontuacao_aluno(),
                String.valueOf(resposta.getFlg_acertou()),
                resposta.getResposta_aluno_texto(),
                resposta.getResposta_aluno_num()
        }, Long.class);
        resposta.setId_resposta(newId != null ? newId : 0L);
        return resposta;
    }

    public void saveBatch(List<Resposta> respostas) {
        String sql = "INSERT INTO RESPOSTA (id_questao, id_quiz, id_aluno, tentativa, pontuacao_aluno, flg_acertou, resposta_aluno_texto, resposta_aluno_num)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Resposta r = respostas.get(i);
                ps.setLong(1, r.getId_questao());
                ps.setLong(2, r.getId_quiz());
                ps.setLong(3, r.getId_aluno());
                ps.setInt(4, r.getTentativa());
                ps.setInt(5, r.getPontuacao_aluno());
                ps.setString(6, String.valueOf(r.getFlg_acertou()));

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

    public void deleteById(long id) {
        String sql = "DELETE FROM resposta WHERE id_resposta = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean alunoJaRealizouQuiz(long idAluno, long idQuiz) {
        String sql = "SELECT COUNT(*) FROM resposta WHERE id_aluno = ? AND id_quiz = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{idAluno, idQuiz}, Integer.class);
        return count != null && count > 0;
    }

    public int calcularPontuacaoTotal(long idAluno, long idQuiz) {
        String sql = "SELECT COALESCE(SUM(pontuacao_aluno), 0) FROM resposta WHERE id_aluno = ? AND id_quiz = ?";
        Integer total = jdbcTemplate.queryForObject(sql, new Object[]{idAluno, idQuiz}, Integer.class);
        return total != null ? total : 0;
    }

    public int contarAcertos(long idAluno, long idQuiz) {
        String sql = "SELECT COUNT(*) FROM resposta WHERE id_aluno = ? AND id_quiz = ? AND flg_acertou = 'S'";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{idAluno, idQuiz}, Integer.class);
        return count != null ? count : 0;
    }

    //Busca o número da última tentativa do aluno em um quiz
    public int buscarUltimaTentativa(long idAluno, long idQuiz) {
        String sql = "SELECT COALESCE(MAX(tentativa), 0)" +
                     "FROM resposta WHERE id_aluno = ? AND id_quiz = ?";
        Integer tentativa = jdbcTemplate.queryForObject(sql, new Object[]{idAluno, idQuiz}, Integer.class);
        return tentativa != null ? tentativa : 0;
    }

    //Busca respostas de uma tentativa específica
    public List<Resposta> findByIdAlunoAndIdQuizAndTentativa(long idAluno, long idQuiz, int tentativa) {
        String sql = "SELECT * FROM resposta WHERE id_aluno = ? AND id_quiz = ? AND tentativa = ? ORDER BY id_questao";
        return jdbcTemplate.query(sql, new Object[]{idAluno, idQuiz, tentativa}, rowMapper);
    }

    //Busca todas as tentativas do aluno em um quiz (para histórico)
    public List<Integer> buscarTodasTentativas(long idAluno, long idQuiz) {
        String sql = "SELECT DISTINCT tentativa FROM resposta WHERE id_aluno = ? AND id_quiz = ? ORDER BY tentativa";
        return jdbcTemplate.queryForList(sql, Integer.class, idAluno, idQuiz);
    }

    //Calcula pontuação de uma tentativa específica
    public int calcularPontuacaoTotalPorTentativa(long idAluno, long idQuiz, int tentativa) {
        String sql = "SELECT COALESCE(SUM(pontuacao_aluno), 0) FROM resposta WHERE id_aluno = ? AND id_quiz = ? AND tentativa = ?";
        Integer total = jdbcTemplate.queryForObject(sql, new Object[]{idAluno, idQuiz, tentativa}, Integer.class);
        return total != null ? total : 0;
    }

    //Conta acertos de uma tentativa específica
    public int contarAcertosPorTentativa(long idAluno, long idQuiz, int tentativa) {
        String sql = "SELECT COUNT(*) FROM resposta WHERE id_aluno = ? AND id_quiz = ? AND tentativa = ? AND flg_acertou = 'S'";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{idAluno, idQuiz, tentativa}, Integer.class);
        return count != null ? count : 0;
    }
}