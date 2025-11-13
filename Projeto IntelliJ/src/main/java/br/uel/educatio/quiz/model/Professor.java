package br.uel.educatio.quiz.model;

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
public class Professor extends Usuario {

    private Long id_professor;

    @NotBlank(message = "A instituição de ensino é obrigatória.")
    @Size(max = 200, message = "A instituição deve ter no máximo 200 caracteres.")
    private String instituicao_ensino;

    @NotBlank(message = "A descrição profissional é obrigatória.")
    private String descricao_profissional;

    @Size(max = 50, message = "O link do Lattes deve ter no máximo 50 caracteres.")
    private String lattes;
}