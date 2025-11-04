package br.uel.educatio.quiz.model.enums;

import lombok.Getter;

@Getter
public enum Escolaridade {
    FUNDAMENTAL_I("Fundamental I"),
    FUNDAMENTAL_II("Fundamental II"),
    ENSINO_MEDIO("Ensino Médio"),
    GRADUACAO("Graduação"),
    POS_GRADUACAO("Pós-Graduação");

    private final String displayValue;

    Escolaridade(String displayValue) {
        this.displayValue = displayValue;
    }

    public static Escolaridade fromString(String text) {
        if (text == null) return null;
        for (Escolaridade b : Escolaridade.values()) {
            if (b.displayValue.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Nenhuma escolaridade encontrada para: " + text);
    }

    @Override
    public String toString() {
        return displayValue;
    }
}