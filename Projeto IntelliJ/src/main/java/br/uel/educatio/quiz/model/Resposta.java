package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resposta {

    private long id_resposta;

    @NotNull(message = "O ID da questão é obrigatório.")
    private long id_questao;

    @NotNull(message = "O ID do quiz é obrigatório.")
    private long id_quiz;

    @NotNull(message = "O ID do aluno é obrigatório.")
    private long id_aluno;

    @NotNull(message = "O número da tentativa é obrigatório.")
    @PositiveOrZero(message = "O número da tentativa não pode ser negativo.")
    private int tentativa;

    @NotNull(message = "A pontuação do aluno é obrigatória.")
    @PositiveOrZero(message = "A pontuação não pode ser negativa.")
    private int pontuacao_aluno;

    @NotNull(message = "É obrigatório indicar se o aluno acertou ('S' ou 'N').")
    @Pattern(regexp = "[SNsn]", message = "O indicador de acerto deve ser 'S' ou 'N'.")
    private char flg_acertou;

}