package br.uel.educatio.quiz.config.converterStringToEnums;

import br.uel.educatio.quiz.model.enums.TipoQuestao;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TipoQuestaoConverter implements Converter<String, TipoQuestao> {

    @Override
    public TipoQuestao convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        try {
            return TipoQuestao.fromString(source.trim());
        } catch (IllegalArgumentException e) {
            // Aqui você pode decidir lançar erro ou retornar null
            throw new IllegalArgumentException("Valor inválido para TipoQuestao: " + source, e);
        }
    }
}
