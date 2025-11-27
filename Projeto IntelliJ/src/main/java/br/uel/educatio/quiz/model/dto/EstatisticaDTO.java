package br.uel.educatio.quiz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstatisticaDTO {
    private String label; //Pode ser Nome Área ou Dificuldade
    private double valor; //Porcentagem média de acerto
}