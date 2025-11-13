package br.uel.educatio.quiz.config.converterStringToEnums;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EscolaridadeConverter implements Converter<String, Escolaridade> {

    @Override
    public Escolaridade convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            return Escolaridade.fromString(source.trim());
        } catch (IllegalArgumentException e) {
            // Aqui você pode decidir lançar erro ou retornar null
            throw new IllegalArgumentException("Valor inválido para Escolaridade: " + source, e);
        }
    }
}
