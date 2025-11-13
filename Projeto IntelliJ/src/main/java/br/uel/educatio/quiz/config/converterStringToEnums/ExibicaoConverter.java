package br.uel.educatio.quiz.config.converterStringToEnums;

import br.uel.educatio.quiz.model.enums.Exibicao;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ExibicaoConverter implements Converter<String,Exibicao> {

    @Override
    public Exibicao convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            return Exibicao.fromString(source.trim());
        } catch (IllegalArgumentException e) {
            // Aqui você pode decidir lançar erro ou retornar null
            throw new IllegalArgumentException("Valor inválido para Exibicao: " + source, e);
        }
    }
}
