package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {

    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail não pode estar em branco.")
    @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
    @Email
    private String email;

    @NotBlank(message = "A senha não pode estar em branco.")
    @Size(min = 8, max = 50, message = "A senha deve ter entre 8 e 50 caracteres.")
    private String senha;
}