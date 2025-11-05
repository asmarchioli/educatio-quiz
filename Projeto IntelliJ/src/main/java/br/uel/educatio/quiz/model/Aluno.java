package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Aluno extends Usuario {

    private Long id_aluno;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;
}