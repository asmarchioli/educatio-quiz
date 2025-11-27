package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.dto.DesempenhoDTO;
import br.uel.educatio.quiz.model.dto.EstatisticaDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DesempenhoAlunoDAO {

    private final JdbcTemplate jdbcTemplate;

    public DesempenhoAlunoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Adicionei parâmetros para Area e Data de Corte
    public List<DesempenhoDTO> buscarHistorico(Long idAluno, Long idArea, LocalDate dataCorte) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                q.TITULO, 
                a.NOME_AREA, 
                a.ID_AREA,
                r.TENTATIVA, 
                MAX(r.DATA_REALIZACAO) as DATA_REALIZACAO,
                SUM(r.PONTUACAO_ALUNO) as NOTA_OBTIDA,
                (SELECT SUM(qq.PONTUACAO_QUESTAO) FROM QUIZ_QUESTAO qq WHERE qq.ID_QUIZ = q.ID_QUIZ) as NOTA_MAXIMA
            FROM RESPOSTA r
            JOIN QUIZ q ON r.ID_QUIZ = q.ID_QUIZ
            JOIN AREA a ON q.AREA = a.ID_AREA
            WHERE r.ID_ALUNO = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(idAluno);

        // Filtro Dinâmico de Área
        if (idArea != null) {
            sql.append(" AND a.ID_AREA = ? ");
            params.add(idArea);
        }

        // Filtro Dinâmico de Data (3 dias, 7 dias, etc)
        if (dataCorte != null) {
            sql.append(" AND r.DATA_REALIZACAO >= ? ");
            params.add(java.sql.Date.valueOf(dataCorte));
        }

        sql.append("""
            GROUP BY q.TITULO, a.NOME_AREA, a.ID_AREA, r.TENTATIVA, q.ID_QUIZ
            ORDER BY DATA_REALIZACAO ASC
        """);

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            DesempenhoDTO dto = new DesempenhoDTO();
            dto.setQuizTitulo(rs.getString("TITULO"));
            dto.setAreaNome(rs.getString("NOME_AREA"));
            dto.setTentativa(rs.getInt("TENTATIVA"));
            dto.setNotaObtida(rs.getInt("NOTA_OBTIDA"));

            // Proteção contra Nota Máxima 0 ou Nula
            int notaMax = rs.getInt("NOTA_MAXIMA");
            dto.setNotaMaxima(notaMax > 0 ? notaMax : 100); // Fallback para 100 se der erro

            dto.setIdArea(rs.getLong("ID_AREA"));
            if (rs.getTimestamp("DATA_REALIZACAO") != null) {
                dto.setDataRealizacao(rs.getTimestamp("DATA_REALIZACAO").toLocalDateTime());
            }
            return dto;
        });
    }

    // Atualizado para filtrar o gráfico de barras pela Área também
    public List<EstatisticaDTO> buscarDesempenhoPorDificuldade(Long idAluno, Long idArea) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                q.NIVEL_DIFICULDADE,
                (CAST(SUM(CASE WHEN r.FLG_ACERTOU = 'S' THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*)) * 100 as MEDIA
            FROM RESPOSTA r
            JOIN QUESTAO q ON r.ID_QUESTAO = q.ID_QUESTAO
            JOIN QUIZ z ON r.ID_QUIZ = z.ID_QUIZ 
            WHERE r.ID_ALUNO = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(idAluno);

        if (idArea != null) {
            sql.append(" AND z.AREA = ? ");
            params.add(idArea);
        }

        sql.append(" GROUP BY q.NIVEL_DIFICULDADE ");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> new EstatisticaDTO(
            rs.getString("NIVEL_DIFICULDADE"),
            rs.getDouble("MEDIA")
        ));
    }
}