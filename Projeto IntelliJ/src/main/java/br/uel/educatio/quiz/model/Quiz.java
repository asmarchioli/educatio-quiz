package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    private long id_quiz;

    @NotBlank(message = "O título do quiz é obrigatório.")
    @Size(max = 255, message = "O título deve ter no máximo 255 caracteres.")
    private String titulo;

    @NotBlank(message = "O PIN de acesso é obrigatório.") // É necessário?
    @Size(max = 50, message = "O PIN de acesso deve ter no máximo 50 caracteres.")
    private String pin_acesso;

    private String descricao;

    @NotNull(message = "A visibilidade é obrigatória.")
    private Exibicao visibilidade;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;

    @NotNull(message = "O professor criador é obrigatório.")
    private long professor_criador;

    @NotNull(message = "A área é obrigatória.")
    private long area;

    @NotNull(message = "A data de criação é obrigatória.")
    @FutureOrPresent(message = "A data de criação não pode ser no passado.")
    private LocalDate data_criacao;

}