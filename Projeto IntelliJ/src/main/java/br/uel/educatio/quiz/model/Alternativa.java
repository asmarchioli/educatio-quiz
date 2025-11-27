package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alternativa {

    private Long id_questao;


    private Long num_alternativa;

    @NotBlank(message = "O texto da alternativa não pode estar em branco.")
    @Size(max = 255, message = "O texto da alternativa deve ter no máximo 255 caracteres.")
    private String texto_alternativa;

    @NotNull(message = "É obrigatório indicar se a alternativa é correta ('S' ou 'N').")
    @Pattern(regexp = "[SN]", message = "O indicador de correção deve ser 'S' ou 'N'.")
    private char flg_eh_correta;
}