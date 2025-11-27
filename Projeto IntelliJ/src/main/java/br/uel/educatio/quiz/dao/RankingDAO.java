package br.uel.educatio.quiz.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import br.uel.educatio.quiz.model.dto.RankingDTO;
import br.uel.educatio.quiz.model.enums.Escolaridade;

@Repository
public class RankingDAO {
  private final JdbcTemplate jdbcTemplate;

  public RankingDAO(JdbcTemplate jdbcTemplate) {
      this.jdbcTemplate = jdbcTemplate;
  }
  
  private final RowMapper<RankingDTO> rankingRowMapper = (rs, rowNum) -> {
    String nomeAluno = rs.getString("nome");

    int pontuacaoTotal = rs.getInt("nota_final"); 

    int tentativa = rs.getInt("tentativa");
      
    int pesoDesempate = rs.getInt("peso_desempate");

    int posicao = rs.getInt("rank_calculado");

    return new RankingDTO(nomeAluno, pontuacaoTotal, tentativa, pesoDesempate, posicao);
  };

    
    public List<RankingDTO> buscarRankingDoQuiz(Long id_quiz) {
        String sql = """
            WITH TentativasCalculadas AS (
                SELECT 
                    a.ID_ALUNO, 
                    a.NOME, 
                    r.TENTATIVA,
                    -- Calcula a Nota
                    SUM(r.PONTUACAO_ALUNO) as nota_bruta,

                    -- Calcula o Peso de Desempate (Dificuldade)
                    SUM(CASE 
                        WHEN r.FLG_ACERTOU = 'S' AND q.NIVEL_DIFICULDADE = 'DIFÍCIL' THEN 3
                        WHEN r.FLG_ACERTOU = 'S' AND q.NIVEL_DIFICULDADE = 'MÉDIO' THEN 2
                        ELSE 1 
                    END) as peso_desempate
                FROM RESPOSTA r
                JOIN QUESTAO q ON r.ID_QUESTAO = q.ID_QUESTAO
                JOIN ALUNO a ON r.ID_ALUNO = a.ID_ALUNO
                WHERE r.ID_QUIZ = ?
                GROUP BY a.ID_ALUNO, a.NOME, r.TENTATIVA
            ),
            
            MelhoresTentativas AS (
                -- Escolhe a Melhor tentativa de cada aluno
                -- Critério: Maior Nota >  Menor Tentativa > Maior Dificuldade 
                SELECT DISTINCT ON (ID_ALUNO) *
                FROM TentativasCalculadas
                ORDER BY ID_ALUNO, nota_bruta DESC, TENTATIVA ASC, peso_desempate DESC
            )
            
            SELECT 
                NOME, 
                nota_bruta as nota_final,
                TENTATIVA,
                peso_desempate,

                -- Gera o Ranking Final
                -- Define-se quem fica na frente de quem
                DENSE_RANK() OVER (
                    ORDER BY 
                        nota_bruta DESC,       -- 1º Critério: Nota
                        TENTATIVA ASC,         -- 2º Critério: Menor Tentativa (1ª > 2ª)
                        peso_desempate DESC    -- 3º Critério: Dificuldade
                ) as rank_calculado

            FROM MelhoresTentativas
            ORDER BY rank_calculado ASC
            LIMIT 10
        """;

        return jdbcTemplate.query(sql, rankingRowMapper, id_quiz);
    }

  public List<RankingDTO> buscarRankingPorNome(Long id_quiz, String parteNome) {
        String sql = """
            WITH TentativasCalculadas AS (
                SELECT 
                    a.ID_ALUNO, 
                    a.NOME, 
                    r.TENTATIVA,
                    -- Calcula a Nota
                    SUM(r.PONTUACAO_ALUNO) as nota_bruta,

                    -- Calcula o Peso de Desempate (Dificuldade)
                    SUM(CASE 
                        WHEN r.FLG_ACERTOU = 'S' AND q.NIVEL_DIFICULDADE = 'DIFÍCIL' THEN 3
                        WHEN r.FLG_ACERTOU = 'S' AND q.NIVEL_DIFICULDADE = 'MÉDIO' THEN 2
                        ELSE 1 
                    END) as peso_desempate
                FROM RESPOSTA r
                JOIN QUESTAO q ON r.ID_QUESTAO = q.ID_QUESTAO
                JOIN ALUNO a ON r.ID_ALUNO = a.ID_ALUNO
                WHERE r.ID_QUIZ = ?
                GROUP BY a.ID_ALUNO, a.NOME, r.TENTATIVA
            ),

            MelhoresTentativas AS (
                -- Escolhe a Melhor tentativa de cada aluno
                -- Critério: Maior Nota >  Menor Tentativa > Maior Dificuldade 
                SELECT DISTINCT ON (ID_ALUNO) *
                FROM TentativasCalculadas
                ORDER BY ID_ALUNO, nota_bruta DESC, TENTATIVA ASC, peso_desempate DESC
            )

            SELECT 
                NOME, 
                nota_bruta as nota_final,
                TENTATIVA,
                peso_desempate,

                -- Gera o Ranking Final
                -- Define-se quem fica na frente de quem
                DENSE_RANK() OVER (
                    ORDER BY 
                        nota_bruta DESC,       -- 1º Critério: Nota
                        TENTATIVA ASC,         -- 2º Critério: Menor Tentativa (1ª > 2ª)
                        peso_desempate DESC    -- 3º Critério: Dificuldade
                ) as rank_calculado

            FROM MelhoresTentativas
            WHERE NOME ILIKE ?
            ORDER BY rank_calculado ASC
            LIMIT 10
        """;

    String termoParaBanco = "%" + parteNome + "%";
    return jdbcTemplate.query(sql, rankingRowMapper, id_quiz, termoParaBanco);
  }

}
    