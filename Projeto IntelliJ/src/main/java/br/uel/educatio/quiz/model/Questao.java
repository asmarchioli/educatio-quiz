package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Questao {
    private long id_questao;

    @NotBlank(message="O enunciado não pode estar em branco.")
    private String enunciado;

    @NotNull(message = "O tipo da questão é obrigatório")
    private TipoQuestao tipo_questao;

    @NotNull(message = "A visibilidade é obrigatória")
    private Exibicao visibilidade;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;

    @NotNull(message = "O nível de dificuldade é obrigatório")
    private Dificuldade nivel_dificuldade;

    @NotNull
    private long area;

    @NotNull
    private long professor_criador;

}
