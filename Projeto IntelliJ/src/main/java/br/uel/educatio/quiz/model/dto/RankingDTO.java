package br.uel.educatio.quiz.model.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingDTO {
    private String nomeAluno;
    private Integer pontuacaoTotal; 
    private Integer tentativa;
    private Integer pesoDesempate;
    private Integer posicao;

}