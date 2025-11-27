package br.uel.educatio.quiz.model;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    private Long id_quiz;

    @NotBlank(message = "O título do quiz é obrigatório.")
    @Size(max = 255, message = "O título deve ter no máximo 255 caracteres.")
    private String titulo;


    @Size(max = 50, message = "O PIN de acesso deve ter no máximo 50 caracteres.")
    private String pin_acesso;

    private String descricao;

    @NotNull(message = "A visibilidade é obrigatória.")
    private Exibicao visibilidade;

    @NotNull(message = "O nível educacional é obrigatório.")
    private Escolaridade nivel_educacional;

    private Long professor_criador;

    @NotNull(message = "A área é obrigatória.")
    private Long area;

   
    private LocalDate data_criacao;

    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private List<Questao> questoes;

    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private int num_questoes;
    
    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private String nome_area;
    
    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private String professor_criador_nome;

    //CAMPO TRANSIENTE (não tem no banco, serve apenas para view)
    private int qtd_resolucoes;

}