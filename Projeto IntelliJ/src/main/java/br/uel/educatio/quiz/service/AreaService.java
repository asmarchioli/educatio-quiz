package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AreaDAO;
import br.uel.educatio.quiz.model.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AreaService {

    @Autowired
    private AreaDAO areaDAO;

    public void cadastrar(Area area) {
        areaDAO.inserir(area);
    }

    public Optional<Area> buscarPorId(Long id) {
        return areaDAO.buscarPorId(id);
    }

    public List<Area> listarTodas() {
        return areaDAO.listarTodas();
    }

    public void atualizar(Area area) {
        Optional<Area> areaExistente = areaDAO.buscarPorId(area.getIdArea());
        if (areaExistente.isEmpty()) {
            throw new IllegalArgumentException("Área não encontrada");
        }
        areaDAO.atualizar(area);
    }

    public void deletar(Long id) {
        areaDAO.deletar(id);
    }
}