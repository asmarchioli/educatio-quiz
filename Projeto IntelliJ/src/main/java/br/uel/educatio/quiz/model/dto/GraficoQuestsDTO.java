package br.uel.educatio.quiz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class GraficoQuestsDTO {
    private String label;
    private Integer porcentagemAcerto;
    private String tipoQuestao;
    private String enunciado;
}