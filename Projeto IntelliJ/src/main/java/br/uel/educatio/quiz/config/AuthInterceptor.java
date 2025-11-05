package br.uel.educatio.quiz.config;

import br.uel.educatio.quiz.model.Professor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // Permite acesso a páginas públicas (login, cadastro, assets)
        if (uri.equals("/login") || uri.equals("/cadastro") || uri.startsWith("/css/") || uri.startsWith("/js/") || uri.equals("/cadastro/aluno") || uri.equals("/cadastro/professor")) {
            return true;
        }

        // Se não for pública, verifica se o usuário está logado
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect("/login");
            return false;
        }

        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        request.setAttribute("professor", professor);
        //Isso permite que o professor sempre seja passado entre os breakpoints independente do controller

        // Usuário está logado, permite o acesso
        return true;
    }
}
