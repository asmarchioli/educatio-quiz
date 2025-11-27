package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.dto.RankingQuestsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RankingQuestsDAO {

    private final JdbcTemplate jdbcTemplate;

    public RankingQuestsDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RankingQuestsDTO> rankingQuestsRowMapper = (rs, rowNum) -> {
        String enunciado = rs.getString("ENUNCIADO");

        String tipoQuestao = rs.getString("TIPO_QUESTAO"); 

    
        Integer qtdRespostas = rs.getInt("qtd_respostas"); 
        Integer qtdErros = rs.getInt("qtd_erros");

        return new RankingQuestsDTO(enunciado, qtdRespostas, qtdErros, tipoQuestao);
    };

    public List<RankingQuestsDTO> buscarRankingQuests(Long id_quiz) {
        String sql = """
            SELECT 
                q.ENUNCIADO,
                q.TIPO_QUESTAO,  -- <--- Adicionado aqui
                COUNT(r.ID_RESPOSTA) as qtd_respostas,
                SUM(CASE WHEN r.FLG_ACERTOU = 'N' THEN 1 ELSE 0 END) as qtd_erros
            FROM RESPOSTA r
            JOIN QUESTAO q ON r.ID_QUESTAO = q.ID_QUESTAO
            WHERE r.ID_QUIZ = ?

            -- <--- Adicionado no GROUP BY (Obrigatório em SQL)
            GROUP BY q.ID_QUESTAO, q.ENUNCIADO, q.TIPO_QUESTAO 

            HAVING COUNT(r.ID_RESPOSTA) > 0 
            ORDER BY qtd_erros DESC 
        """;

        return jdbcTemplate.query(sql, rankingQuestsRowMapper, id_quiz);
    }
}