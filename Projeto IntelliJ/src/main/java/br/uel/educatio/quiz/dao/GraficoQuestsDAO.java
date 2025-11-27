package br.uel.educatio.quiz.dao;

import br.uel.educatio.quiz.model.dto.GraficoQuestsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository 
public class GraficoQuestsDAO {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<GraficoQuestsDTO> graficoRowMapper = (rs, rowNum) -> {
      
        String label = "Q" + (rowNum + 1) + " - " + rs.getString("label"); 

       
        int pct = rs.getInt("porcentagem");

        
        String tipo = rs.getString("TIPO_QUESTAO"); 

        String enunciado = rs.getString("ENUNCIADO");

        return new GraficoQuestsDTO(label, pct, tipo, enunciado);
    };

    
    public GraficoQuestsDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    

    public List<GraficoQuestsDTO> buscarDadosParaGrafico(Long id_quiz) {
        String sql = """
            SELECT 
                SUBSTRING(q.ENUNCIADO, 1, 15) || '...' as label,
                q.TIPO_QUESTAO, 
                (SUM(CASE WHEN r.FLG_ACERTOU = 'S' THEN 1 ELSE 0 END) * 100) / COUNT(r.ID_RESPOSTA) as porcentagem,
                q.ENUNCIADO
            FROM RESPOSTA r
            JOIN QUESTAO q ON r.ID_QUESTAO = q.ID_QUESTAO
            WHERE r.ID_QUIZ = ?

            -- <--- Adicionado no GROUP BY (Obrigatório)
            GROUP BY q.ID_QUESTAO, q.ENUNCIADO, q.TIPO_QUESTAO 

            ORDER BY q.ID_QUESTAO
        """;

        return jdbcTemplate.query(sql, graficoRowMapper, id_quiz);
    }
}