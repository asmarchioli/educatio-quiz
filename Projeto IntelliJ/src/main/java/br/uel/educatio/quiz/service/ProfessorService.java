package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.ProfessorAreaDAO;
import br.uel.educatio.quiz.dao.ProfessorDAO;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.Quiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Modificar múltiplos dados

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    private final ProfessorDAO professorDAO;
    private final ProfessorAreaDAO professorAreaDAO;
    private final QuizService quizService;
    private final QuestaoService questaoService;

    public ProfessorService(ProfessorDAO professorDAO, ProfessorAreaDAO professorAreaDAO, QuizService quizService, QuestaoService questaoService) {
        this.professorDAO = professorDAO;
        this.professorAreaDAO = professorAreaDAO;
        this.quizService = quizService;
        this.questaoService = questaoService;
    }

    public List<Professor> listarTodos() {
        return professorDAO.findAll();
    }

    public Optional<Professor> buscarPorId(long id) {
        return professorDAO.findById(id);
    }

    @Transactional // Garante que a operação seja atômica
    public Professor salvar(Professor professor) throws Exception {
        // Validação de e-mail único (considerar mover para AuthService se houver AlunoDAO)
        Optional<Professor> existente = professorDAO.findByEmail(professor.getEmail());
        if (existente.isPresent() && existente.get().getId_professor() != professor.getId_professor()) {
            throw new Exception("E-mail já cadastrado para outro professor.");
        }

        return professorDAO.save(professor);
    }

    @Transactional
    public Professor atualizarPerfilProfessor(long idProfessor, Professor professorAtualizado) throws Exception {
        Optional<Professor> professorOpt = professorDAO.findById(idProfessor);
        if (professorOpt.isEmpty()) {
            throw new Exception("Professor não encontrado para atualização.");
        }

        Professor professorExistente = professorOpt.get();

        professorExistente.setNome(professorAtualizado.getNome());
        professorExistente.setInstituicao_ensino(professorAtualizado.getInstituicao_ensino());
        professorExistente.setDescricao_profissional(professorAtualizado.getDescricao_profissional());
        professorExistente.setLattes(professorAtualizado.getLattes());

        if (!professorExistente.getEmail().equalsIgnoreCase(professorAtualizado.getEmail())) {
            Optional<Professor> emailExistente = professorDAO.findByEmail(professorAtualizado.getEmail());
            if (emailExistente.isPresent()) {
                throw new Exception("Novo e-mail já está em uso por outro professor.");
            }
            professorExistente.setEmail(professorAtualizado.getEmail());
        }

        return professorDAO.save(professorExistente);
    }

    @Transactional
    public void deletarPorId(long id) throws Exception {
        if (!professorDAO.existsById(id)) {
            throw new Exception("Professor não encontrado para exclusão.");
        }
        // TODO: Adicionar lógica para verificar dependências (quizzes, questões) antes de excluir.
        // Exemplo: if (quizDAO.countByProfessor(id) > 0) throw new Exception("Não pode excluir...");

        // Remove associações de área antes de deletar o professor
        professorAreaDAO.removeAllAreasFromProfessor(id);
        professorDAO.deleteById(id);
    }

    public boolean existePorId(long id) {
        return professorDAO.existsById(id);
    }

    // --- Métodos de Gerenciamento de Áreas ---

    @Transactional
    public void adicionarAreaProfessor(long idProfessor, long idArea) throws Exception {
        //if (!professorDAO.existsById(idProfessor)) throw new Exception("Professor não existe");
        //if (!areaDAO.existsById(idArea)) throw new Exception("Área não existe");
        professorAreaDAO.addAreaToProfessor(idProfessor, idArea);
    }

    @Transactional
    public void removerAreaProfessor(long idProfessor, long idArea) {
        professorAreaDAO.removeAreaFromProfessor(idProfessor, idArea);
    }
    public List<Long> listarAreasPorProfessor(long idProfessor) {
        return professorAreaDAO.findAreaIdsByProfessorId(idProfessor);
    }

    public List<Professor> listarProfessoresPorArea(long idArea) {
        List<Long> idsProfessores = professorAreaDAO.findProfessorIdsByAreaId(idArea);
        // Busca os objetos Professor completos a partir dos IDs
        return professorDAO.findAllById(idsProfessores);
    }


    public List<Quiz> listarQuizzesCriados(long idProfessor) {
        return quizService.listarQuizzesPorProfessor(idProfessor);
    }

    public List<Questao> listarQuestoesCriadas(long idProfessor) {
        return questaoService.listarQuestoesPorProfessor(idProfessor);
    }
}