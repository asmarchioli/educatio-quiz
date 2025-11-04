package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestao {

    @NotNull(message = "O ID da questão é obrigatório.")
    private Long id_questao;

    @NotNull(message = "O ID do quiz é obrigatório.")
    private Long id_quiz;

    @NotNull(message = "A pontuação da questão é obrigatória.")
    @PositiveOrZero(message = "A pontuação não pode ser negativa.")
    private int pontuacao_questao;

}