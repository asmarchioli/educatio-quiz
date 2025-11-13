package br.uel.educatio.quiz.model.enums;

import lombok.Getter;

@Getter
public enum Dificuldade {
    FACIL("FÁCIL"),
    MEDIO("MÉDIO"),
    DIFICIL("DIFÍCIL");

    private final String displayValue;

    Dificuldade(String displayValue) {
        this.displayValue = displayValue;
    }

    public static Dificuldade fromString(String text) {
        if (text == null) return null;
        for (Dificuldade d : Dificuldade.values()) {
            if (d.displayValue.equalsIgnoreCase(text)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Nenhuma dificuldade encontrada para: " + text);
    }

    @Override
    public String toString() {
        return displayValue;
    }
}