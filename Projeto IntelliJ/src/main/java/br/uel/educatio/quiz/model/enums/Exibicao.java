package br.uel.educatio.quiz.model.enums;

import lombok.Getter;

@Getter
public enum Exibicao {
    PUBLICO("Público"),
    PRIVADO("Privado");

    private final String displayValue;

    Exibicao(String displayValue) {
        this.displayValue = displayValue;
    }

    public static Exibicao fromString(String text) {
        if (text == null) return null;
        for (Exibicao e : Exibicao.values()) {
            if (e.displayValue.equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Nenhuma exibição encontrada para: " + text);
    }

    @Override
    public String toString() {
        return displayValue;
    }
}