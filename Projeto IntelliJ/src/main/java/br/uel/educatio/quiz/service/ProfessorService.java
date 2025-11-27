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


    private final ProfessorDAO professorDAO;
    private final ProfessorAreaDAO professorAreaDAO;
    private final AuthService authService; 
    private final QuestaoService questaoService; 
    private final QuizService quizService;

  
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



    @Transactional
    public Professor cadastrar(Professor professor, List<Long> idsAreas) {
        if (authService.emailJaCadastrado(professor.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado no sistema");
        }


        Professor professorSalvo = professorDAO.save(professor);
        Long idProfessor = professorSalvo.getId_professor();

        if (idsAreas != null && !idsAreas.isEmpty()) {
            professorAreaDAO.inserirVarias(idProfessor, idsAreas);
        }
        return professorSalvo;
    }

    @Transactional
    public Professor atualizar(Professor professor, List<Long> novasIdsAreas) {

        Optional<Professor> professorExistente = professorDAO.findById(professor.getId_professor());
        if (professorExistente.isEmpty()) {
            throw new IllegalArgumentException("Professor não encontrado");
        }

        if (!professorExistente.get().getEmail().equals(professor.getEmail())) {
            if (authService.emailJaCadastrado(professor.getEmail())) {
                throw new IllegalArgumentException("Email já cadastrado no sistema");
            }
        }

        Professor professorAtualizado = professorDAO.save(professor);

        if (novasIdsAreas != null) {
            professorAreaDAO.removeAllAreasFromProfessor(professor.getId_professor());
            if (!novasIdsAreas.isEmpty()) {
                professorAreaDAO.inserirVarias(professor.getId_professor(), novasIdsAreas);
            }
        }
        return professorAtualizado;
    }


    @Transactional
    public Professor salvar(Professor professor) throws RuntimeException {
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



    public List<Professor> listarTodos() {
        return professorDAO.findAll(); 
    }

    public Professor buscarPorId(Long id) {
        Optional<Professor> professorOpt = professorDAO.findById(id);
        if (professorOpt.isEmpty()) {
            throw new RuntimeException("Professor não encontrado");
        }

        return professorOpt.get();
    }


    public Optional<Professor> buscarPorEmail(String email) {
        return professorDAO.findByEmail(email);
    }

    public boolean existePorId(Long id) {
        return professorDAO.existsById(id); 
    }



    @Transactional
    public void deletarPorId(Long id) throws RuntimeException {
        if (!professorDAO.existsById(id)) {
            throw new RuntimeException("Professor não encontrado para exclusão.");
        }

        professorAreaDAO.removeAllAreasFromProfessor(id);
        professorDAO.deleteById(id);
    }



    @Transactional
    public void adicionarAreaProfessor(Long idArea, Long idProfessor) throws RuntimeException{

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

    // Retorna List<Area> 
    public List<Area> buscarAreasDoProfessor(Long idProfessor) {
        return professorAreaDAO.buscarAreasDoProfessor(idProfessor);
    }

    public List<Professor> listarProfessoresPorArea(Long idArea) {
        List<Long> idsProfessores = professorAreaDAO.findProfessorIdsByAreaId(idArea);
        return professorDAO.findAllById(idsProfessores);
    }


    public boolean professorPossuiArea(Long idProfessor, Long idArea) {
        return professorAreaDAO.professorPossuiArea(idProfessor, idArea);
    }
}