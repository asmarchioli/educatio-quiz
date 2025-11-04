package br.uel.educatio.quiz.service;

import br.uel.educatio.quiz.dao.AlunoDAO;
import br.uel.educatio.quiz.dao.ProfessorDAO;
import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.Professor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AlunoDAO alunoDAO;

    @Autowired
    private ProfessorDAO professorDAO;

    public Object autenticar(String email, String senha, String tipoUsuario) {
        if ("aluno".equalsIgnoreCase(tipoUsuario)) {
            Optional<Aluno> aluno = alunoDAO.findByEmail(email);
            if (aluno.isPresent() && aluno.get().getSenha().equals(senha)) {
                return aluno.get();
            }
        } else if ("professor".equalsIgnoreCase(tipoUsuario)) {
            Optional<Professor> professor = professorDAO.findByEmail(email);
            if (professor.isPresent() && professor.get().getSenha().equals(senha)) {
                return professor.get();
            }
        }
        return null;
    }

    public boolean emailJaCadastrado(String email) {
        return alunoDAO.emailJaExiste(email) || professorDAO.emailJaExiste(email);
    }
}