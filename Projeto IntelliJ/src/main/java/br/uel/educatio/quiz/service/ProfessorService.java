package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.ProfessorAreaDAO;
import br.uel.educatio.quiz.dao.ProfessorDAO;
import br.uel.educatio.quiz.model.Area; // Importado do Arquivo 1
import br.uel.educatio.quiz.model.Professor;
import org.springframework.beans.factory.annotation.Autowired; // Importado do Arquivo 1
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    // --- Dependências Mescladas ---
    private final ProfessorDAO professorDAO;
    private final ProfessorAreaDAO professorAreaDAO;
    private final AuthService authService; // Adicionado do Arquivo 1
    private final QuestaoService questaoService; // Do Arquivo 2

    // (QuizService foi mantido no construtor para consistência)
    private final QuizService quizService;

    // Construtor Mesclado (Base do Arquivo 2 + AuthService do Arquivo 1)
    @Autowired
    public ProfessorService(ProfessorDAO professorDAO, ProfessorAreaDAO professorAreaDAO,
                            AuthService authService, QuestaoService questaoService,
                            QuizService quizService) {
        this.professorDAO = professorDAO;
        this.professorAreaDAO = professorAreaDAO;
        this.authService = authService;
        this.questaoService = questaoService;
        this.quizService = quizService;
    }

    // --- Métodos do Arquivo 1 (Salvar Professor + Áreas) ---
    // (Atualizados para usar métodos DAO modernos)

    @Transactional
    public Professor cadastrar(Professor professor, List<Long> idsAreas) {
        if (authService.emailJaCadastrado(professor.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado no sistema");
        }

        // Usa o DAO moderno 'save' (do Arquivo 2)
        Professor professorSalvo = professorDAO.save(professor);
        Long idProfessor = professorSalvo.getId_professor();

        if (idsAreas != null && !idsAreas.isEmpty()) {
            professorAreaDAO.inserirVarias(idProfessor, idsAreas);
        }
        return professorSalvo;
    }

    @Transactional
    public Professor atualizar(Professor professor, List<Long> novasIdsAreas) {
        // Usa o DAO moderno 'findById' (do Arquivo 2)
        Optional<Professor> professorExistente = professorDAO.findById(professor.getId_professor());
        if (professorExistente.isEmpty()) {
            throw new IllegalArgumentException("Professor não encontrado");
        }

        if (!professorExistente.get().getEmail().equals(professor.getEmail())) {
            if (authService.emailJaCadastrado(professor.getEmail())) {
                throw new IllegalArgumentException("Email já cadastrado no sistema");
            }
        }

        // Usa o DAO moderno 'save' (do Arquivo 2)
        Professor professorAtualizado = professorDAO.save(professor);

        if (novasIdsAreas != null) {
            professorAreaDAO.removeAllAreasFromProfessor(professor.getId_professor());
            if (!novasIdsAreas.isEmpty()) {
                professorAreaDAO.inserirVarias(professor.getId_professor(), novasIdsAreas);
            }
        }
        return professorAtualizado;
    }

    // --- Métodos do Arquivo 2 (Salvar/Atualizar Perfil Básico) ---

    @Transactional
    public Professor salvar(Professor professor) throws RuntimeException {
        // Validação de e-mail (note: esta só checa 'professor',
        // a do 'cadastrar' checa 'authService' que é mais completa)
        Optional<Professor> existente = professorDAO.findByEmail(professor.getEmail());
        if (existente.isPresent() && existente.get().getId_professor() != professor.getId_professor()) {
            throw new RuntimeException("E-mail já cadastrado para outro professor.");
        }
        return professorDAO.save(professor);
    }

    @Transactional
    public Professor atualizarPerfilProfessor(Long idProfessor, Professor professorAtualizado) throws RuntimeException {
        Optional<Professor> professorOpt = professorDAO.findById(idProfessor);
        if (professorOpt.isEmpty()) {
            throw new RuntimeException("Professor não encontrado para atualização.");
        }

        Professor professorExistente = professorOpt.get();

        professorExistente.setNome(professorAtualizado.getNome());
        professorExistente.setInstituicao_ensino(professorAtualizado.getInstituicao_ensino());
        professorExistente.setDescricao_profissional(professorAtualizado.getDescricao_profissional());
        professorExistente.setLattes(professorAtualizado.getLattes());

        if (!professorExistente.getEmail().equalsIgnoreCase(professorAtualizado.getEmail())) {
            Optional<Professor> emailExistente = professorDAO.findByEmail(professorAtualizado.getEmail());
            if (emailExistente.isPresent()) {
                throw new RuntimeException("Novo e-mail já está em uso por outro professor.");
            }
            professorExistente.setEmail(professorAtualizado.getEmail());
        }

        return professorDAO.save(professorExistente);
    }

    // --- Métodos de Busca Mesclados (de ambos os arquivos) ---

    public List<Professor> listarTodos() {
        return professorDAO.findAll(); // Método DAO do Arquivo 2
    }

    public Optional<Professor> buscarPorId(Long id) {
        return professorDAO.findById(id); // Método DAO do Arquivo 2
    }

    // Adicionado do Arquivo 1 (mas usando DAO do Arquivo 2)
    public Optional<Professor> buscarPorEmail(String email) {
        return professorDAO.findByEmail(email);
    }

    public boolean existePorId(Long id) {
        return professorDAO.existsById(id); // Do Arquivo 2
    }

    // --- Métodos de Exclusão (Lógica do Arquivo 2 é mais segura) ---

    @Transactional
    public void deletarPorId(Long id) throws RuntimeException {
        if (!professorDAO.existsById(id)) {
            throw new RuntimeException("Professor não encontrado para exclusão.");
        }
        // TODO: Adicionar lógica para verificar dependências (quizzes, questões) antes de excluir.

        // (Lógica de exclusão de áreas - assumindo 'removeAllAreasFromProfessor' do DAO 2)
        professorAreaDAO.removeAllAreasFromProfessor(id);
        professorDAO.deleteById(id);
    }

    // --- Métodos de Áreas Mesclados (de ambos os arquivos) ---

    @Transactional
    public void adicionarAreaProfessor(Long idArea, Long idProfessor) throws RuntimeException{
        // if (!professorDAO.existsById(idProfessor)) throw new RuntimeException("Professor não existe");
        // if (!areaDAO.existsById(idArea)) throw new RuntimeException("Área não existe");
        professorAreaDAO.addAreaToProfessor(idProfessor, idArea);
        
    }

    @Transactional
    public void removerAreaProfessor(Long idProfessor, Long idArea) {
        professorAreaDAO.removeAreaFromProfessor(idProfessor, idArea);
    }

    // Retorna List<String>
    public List<String> listarAreasPorProfessor(Long idProfessor) {
        return professorAreaDAO.findAreasByProfessorId(idProfessor);
    }

    // Retorna List<Area> (Adicionado do Arquivo 1)
    public List<Area> buscarAreasDoProfessor(Long idProfessor) {
        return professorAreaDAO.buscarAreasDoProfessor(idProfessor);
    }

    public List<Professor> listarProfessoresPorArea(Long idArea) {
        List<Long> idsProfessores = professorAreaDAO.findProfessorIdsByAreaId(idArea);
        return professorDAO.findAllById(idsProfessores);
    }

    // Adicionado do Arquivo 1
    public boolean professorPossuiArea(Long idProfessor, Long idArea) {
        return professorAreaDAO.professorPossuiArea(idProfessor, idArea);
    }
}