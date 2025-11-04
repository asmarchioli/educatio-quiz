package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlunoDAO;
import br.uel.educatio.quiz.dao.ProfessorDAO;
import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.Professor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private final AlunoDAO alunoDAO;
    private final ProfessorDAO professorDAO;

    public AuthService(AlunoDAO alunoDAO, ProfessorDAO professorDAO) {
        this.alunoDAO = alunoDAO;
        this.professorDAO = professorDAO;
    }

    public void cadastrarAluno(Aluno aluno) throws Exception {
        if (alunoDAO.findByEmail(aluno.getEmail()).isPresent() ||
                professorDAO.findByEmail(aluno.getEmail()).isPresent()) {
            throw new Exception("E-mail já cadastrado!");
        }

        alunoDAO.save(aluno);
    }

    public void cadastrarProfessor(Professor professor) throws Exception {
        if (alunoDAO.findByEmail(professor.getEmail()).isPresent() ||
                professorDAO.findByEmail(professor.getEmail()).isPresent()) {
            throw new Exception("E-mail já cadastrado!");
        }

        professorDAO.save(professor);
    }

    public Optional<?> autenticarUsuario(String email, String senha) {
        Optional<Aluno> aluno = alunoDAO.findByEmail(email);
        if (aluno.isPresent() && aluno.get().getSenha().equals(senha)) {
            return aluno;
        }

        Optional<Professor> professor = professorDAO.findByEmail(email);
        if (professor.isPresent() && professor.get().getSenha().equals(senha)) {
            return professor;
        }

        return Optional.empty();
    }

    public void trocarSenha(long idUsuario, String tipoUsuario, String senhaAntiga, String senhaNova) throws Exception {
        if (senhaNova == null || senhaNova.trim().length() < 8) {
            throw new IllegalArgumentException("Nova senha inválida. Deve ter pelo menos 8 caracteres.");
        }

        if ("ALUNO".equalsIgnoreCase(tipoUsuario)) {
            Optional<Aluno> alunoOpt = alunoDAO.findById(idUsuario);
            if (alunoOpt.isEmpty()) {
                throw new Exception("Usuário Aluno não encontrado com o ID fornecido.");
            }
            Aluno aluno = alunoOpt.get();

            if (!aluno.getSenha().equals(senhaAntiga)) {
                throw new Exception("Senha antiga incorreta para o Aluno.");
            }

            alunoDAO.updatePassword(idUsuario, senhaNova);

        } else if ("PROFESSOR".equalsIgnoreCase(tipoUsuario)) {
            Optional<Professor> profOpt = professorDAO.findById(idUsuario);
            if (profOpt.isEmpty()) {
                throw new Exception("Usuário Professor não encontrado com o ID fornecido.");
            }
            Professor professor = profOpt.get();

            if (!professor.getSenha().equals(senhaAntiga)) {
                throw new Exception("Senha antiga incorreta para o Professor.");
            }

            professorDAO.updatePassword(idUsuario, senhaNova);

        } else {
            throw new IllegalArgumentException("Tipo de usuário inválido especificado: " + tipoUsuario);
        }
    }
}
