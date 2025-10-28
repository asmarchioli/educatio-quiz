package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorArea {

    @NotNull(message = "O ID do professor é obrigatório.")
    private long id_professor;

    @NotNull(message = "O ID da área é obrigatório.")
    private long id_area;

}