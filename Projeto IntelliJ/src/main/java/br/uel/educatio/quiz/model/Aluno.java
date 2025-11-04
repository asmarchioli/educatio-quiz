package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Aluno {
    private Long id_aluno;

    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(max=120)
    private String nome;

    @NotBlank(message = "O e-mail não pode estar em branco.")
    @Size(max=255)
    @Email
    private String email;

    @NotBlank(message = "A senha não pode estar em branco.")
    @Size(min=8, max=50, message = "A senha deve ter entre 8 e 50 caracteres.")
    private String senha;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;

}
