package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService  authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("usuarioLogado") != null) {
            return "redirect:/home";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("senha") String senha,
                        HttpSession session, Model model) { // Adicionado Model aqui

        Optional<?> usuarioAutenticadoOpt = authService.autenticarUsuario(email, senha);

        if (usuarioAutenticadoOpt.isPresent()) {
            // Pega o objeto de dentro do Optional
            Object usuario = usuarioAutenticadoOpt.get();

            // Verifica o TIPO do objeto encontrado
            if (usuario instanceof Aluno alunoLogado) {
                // Se for um Aluno, pega o nome dele
                session.setAttribute("usuarioLogado", alunoLogado.getNome());
                session.setAttribute("tipoUsuario", "ALUNO"); // Guarda o tipo na sessão
                session.setAttribute("idUsuario", alunoLogado.getId_aluno()); // Guarda o ID na sessão
                // Redireciona para a página principal do aluno (ajuste se necessário)
                return "redirect:/home"; // Ou a URL da dashboard do aluno

            } else if (usuario instanceof Professor professorLogado) {
                // Se for um Professor, pega o nome dele
                session.setAttribute("usuarioLogado", professorLogado.getNome());
                session.setAttribute("tipoUsuario", "PROFESSOR"); // Guarda o tipo na sessão
                session.setAttribute("idUsuario", professorLogado.getId_professor()); // Guarda o ID na sessão
                // Redireciona para a página principal do professor
                return "redirect:/home"; // Ou a URL da dashboard do professor
            } else {
                // Caso inesperado (não deve acontecer se AuthService retornar Aluno ou Professor)
                model.addAttribute("erro", "Tipo de usuário desconhecido após login.");
                return "login";
            }

        } else {
            // Se o Optional estiver vazio (login falhou)
            model.addAttribute("erro", "E-mail ou senha inválidos!");
            return "login"; // Volta para a página de login
        }
    }
}
