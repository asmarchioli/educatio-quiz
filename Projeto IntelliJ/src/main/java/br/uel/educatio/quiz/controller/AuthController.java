package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.Aluno;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String realizarLogin(
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String tipoUsuario,
            HttpSession session,
            Model model) {

        Object usuario = authService.autenticar(email, senha, tipoUsuario);

        if (usuario == null) {
            model.addAttribute("erro", "Email ou senha incorretos");
            return "login";
        }

        if (usuario instanceof Aluno) {
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("tipoUsuario", "aluno");
            return "redirect:/aluno/home";
        } else if (usuario instanceof Professor) {
            session.setAttribute("usuarioLogado", usuario);
            session.setAttribute("tipoUsuario", "professor");
            return "redirect:/professor/home";
        }

        model.addAttribute("erro", "Erro ao processar login");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}