package br.uel.educatio.quiz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Area {

    private Long idArea;

    @NotBlank(message = "O nome da área não pode estar em branco.")
    @Size(max = 75, message = "O nome da área deve ter no máximo 75 caracteres.")
    private String nomeArea;

}
