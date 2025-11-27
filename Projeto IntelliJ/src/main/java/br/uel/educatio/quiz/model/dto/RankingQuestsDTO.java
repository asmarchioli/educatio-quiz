package br.uel.educatio.quiz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingQuestsDTO {
    private String enunciado;
    private Integer qtdRespostas;
    private Integer totalErros;
    private String tipoQuestao;

    // Método auxiliar para calcular a porcentagem de erro na tela
    public String getPorcentagemErro() {
        if (qtdRespostas == null || qtdRespostas == 0) return "0%";
        int porcentagem = (totalErros * 100) / qtdRespostas;
        return porcentagem + "%";
    }
    
    public Integer getTaxaErroNumerica() {
        if (qtdRespostas == null || qtdRespostas == 0) return 0;
        return (totalErros * 100) / qtdRespostas;
    }
    
}