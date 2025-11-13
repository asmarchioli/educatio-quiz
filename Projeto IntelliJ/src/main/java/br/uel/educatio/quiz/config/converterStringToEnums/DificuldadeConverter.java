package br.uel.educatio.quiz.config.converterStringToEnums;

import br.uel.educatio.quiz.model.enums.Dificuldade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DificuldadeConverter implements Converter<String, Dificuldade> {

    @Override
    public Dificuldade convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            return Dificuldade.fromString(source.trim());
        } catch (IllegalArgumentException e) {
            // Aqui você pode decidir lançar erro ou retornar null
            throw new IllegalArgumentException("Valor inválido para Dificuldade: " + source, e);
        }
    }
}
