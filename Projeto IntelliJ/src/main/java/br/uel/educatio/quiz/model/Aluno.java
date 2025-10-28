package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Aluno {
    private long id_aluno;

    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(max=120)
    private String nome;

    @NotBlank(message = "O e-mail não pode estar em branco.")
    @Size(max=255)
    @Email
    private String email;

    @NotBlank(message = "A senha não pode estar em branco.")
    @Size(min=8, max=50)
    private String senha;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;

}
