package br.uel.educatio.quiz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DesempenhoDTO {
    private String quizTitulo;
    private String areaNome;
    private int tentativa;
    private int notaObtida;
    private int notaMaxima;
    private Long idArea;
    private LocalDateTime dataRealizacao;

    public double getPorcentagem() {
        if (notaMaxima == 0) return 0.0;
        return ((double) notaObtida / notaMaxima) * 100.0;
    }

    // Retorna data formatada para o gráfico (Ex: "12/11 - 14:30")
    public String getLabelData() {
        if (dataRealizacao == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        return dataRealizacao.format(formatter);
    }

    // Label composto para o gráfico (Ex: "12/11 - Matemática")
    public String getLabelGrafico() {
        return getLabelData() + " - " + quizTitulo;
    }
}