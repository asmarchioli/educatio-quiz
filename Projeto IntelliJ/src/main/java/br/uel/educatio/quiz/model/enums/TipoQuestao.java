package br.uel.educatio.quiz.model.enums;

import lombok.Getter;

@Getter
public enum TipoQuestao {
    VERDADEIRO_OU_FALSO("Verdadeiro ou Falso"),
    PREENCHER_LACUNA("Preencher Lacuna"),
    MULTIPLA_ESCOLHA("Multipla Escolha");

    private final String displayValue;

    TipoQuestao(String displayValue) {
        this.displayValue = displayValue;
    }

    public static TipoQuestao fromString(String text) {
        for (TipoQuestao b : TipoQuestao.values()) {
            if (text == null) return null;
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Nenhum tipo de questão encontrado para: " + text);
    }

    @Override
    public String toString() {
        return displayValue;
    }
}