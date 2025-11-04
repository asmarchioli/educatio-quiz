package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Professor {
    private Long id_professor;

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


    @NotBlank(message = "A instituição de ensino é obrigatória.")
    @Size(max = 200, message = "A instituição deve ter no máximo 200 caracteres.")
    private String instituicao_ensino;

    @NotBlank(message = "A descrição profissional é obrigatória.")
    private String descricao_profissional;

    @Size(max = 50, message = "O link do Lattes deve ter no máximo 50 caracteres.")
    @URL
    private String lattes;

    @NotNull(message = "A área de atuação é obrigatória.")
    private Long area; // Correspondente a AREA INT NOT NULL (ID da área)

}
