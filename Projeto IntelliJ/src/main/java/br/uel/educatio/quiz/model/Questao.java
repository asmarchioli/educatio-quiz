package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Questao {
    private Long id_questao;

    @NotBlank(message="O enunciado não pode estar em branco.")
    private String enunciado;

    @NotNull(message = "O tipo da questão é obrigatório")
    private TipoQuestao tipo_questao;

    @NotNull(message = "A visibilidade é obrigatória")
    private Exibicao visibilidade;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;

    @NotNull(message = "O nível de dificuldade é obrigatório.")
    private Dificuldade nivel_dificuldade;

    @NotNull(message = "A área é obrigatória.") //comentei pois a área é obtida do professor depois do Valid
    private Long area;

    // @NotNull
    private Long professor_criador;

    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private int pontuacao;

    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private List<Alternativa> alternativas;

    // Campo transiente para capturar a escolha da alternativa correta (índice 0, 1, 2, 3 ou 4)
    // O nome DEVE bater com o 'name' do radio button group do HTML.
    // [CAMPO TRANSIENTE]
    private Integer mc_correct_choice; 

}
