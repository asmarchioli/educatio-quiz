package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlunoDAO;
import br.uel.educatio.quiz.dao.QuizDAO; // Importado do Arquivo 2
import br.uel.educatio.quiz.dao.RespostaDAO; // Importado do Arquivo 2
import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.Quiz; // Importado do Arquivo 2
import br.uel.educatio.quiz.model.Resposta; // Importado do Arquivo 2
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    // --- Dependências Mescladas ---
    private final AlunoDAO alunoDAO;
    private final AuthService authService; // Do Arquivo 1
    private final QuizDAO quizDAO; // Do Arquivo 2
    private final RespostaDAO respostaDAO; // Do Arquivo 2

    // Construtor Mesclado (com todas as dependências)
    @Autowired
    public AlunoService(AlunoDAO alunoDAO, AuthService authService,
                        QuizDAO quizDAO, RespostaDAO respostaDAO) {
        this.alunoDAO = alunoDAO;
        this.authService = authService;
        this.quizDAO = quizDAO;
        this.respostaDAO = respostaDAO;
    }

    // --- Métodos de Gerenciamento de Perfil (do Arquivo 1) ---

    @Transactional
    public Aluno cadastrar(Aluno aluno) {
        if (authService.emailJaCadastrado(aluno.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado no sistema");
        }
        return alunoDAO.save(aluno);
    }

    public Optional<Aluno> buscarPorId(Long id) {
        return alunoDAO.findById(id);
    }

    public Optional<Aluno> buscarPorEmail(String email) {
        return alunoDAO.findByEmail(email);
    }

    public List<Aluno> listarTodos() {
        return alunoDAO.findAll();
    }

    @Transactional
    public Aluno atualizar(Aluno aluno) {
        Optional<Aluno> alunoExistente = alunoDAO.findById(aluno.getId_aluno());
        if (alunoExistente.isEmpty()) {
            throw new IllegalArgumentException("Aluno não encontrado");
        }

        if (!alunoExistente.get().getEmail().equals(aluno.getEmail())) {
            if (authService.emailJaCadastrado(aluno.getEmail())) {
                throw new IllegalArgumentException("Email já cadastrado no sistema");
            }
        }
        return alunoDAO.save(aluno);
    }

    @Transactional
    public void deletar(Long id) {
        alunoDAO.deleteById(id);
    }

    public List<Aluno> buscarPorNome(String nome) {
        return alunoDAO.findByNomeContainingIgnoreCase(nome);
    }

    public boolean emailJaExiste(String email) {
        return alunoDAO.emailJaExiste(email);
    }

    // --- Métodos de Histórico de Quiz (Adicionados do Arquivo 2) ---

    public List<Quiz> buscarHistoricoQuizzes(long id_aluno) {
        return quizDAO.findQuizzesFeitos(id_aluno);
    }

    public List<Resposta> buscarRespostasQuiz(long id_aluno, long id_quiz) {
        return respostaDAO.findByIdAlunoAndIdQuiz(id_aluno, id_quiz);
    }
}