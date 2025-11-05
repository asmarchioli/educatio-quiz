package br.uel.educatio.quiz.config;

import br.uel.educatio.quiz.model.Usuario;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Aluno;
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

        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (usuarioLogado != null && isAuthPage(uri)) {
            String homeUrl = getHomeUrl(usuarioLogado);
            response.sendRedirect(homeUrl);
            return false;
        }

        // Permite acesso a páginas públicas (assets)
        if (isPublicAsset(uri)) {
            return true;
        }

        // Permite acesso a login/cadastro/index SE NÃO ESTIVER LOGADO
        // (O usuário logado já foi tratado no primeiro 'if')
        if (isAuthPage(uri)) {
            return true;
        }

        // Se chegou aqui, a página é protegida.
        // Se não for pública/auth, verifica se o usuário está logado
        if (usuarioLogado == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Se está logado, verifica se tem permissão para a rota
        if (!hasAccessToPath(uri, usuarioLogado)) {
            // TODO: Criar uma página /acesso-negado
            response.sendRedirect("/acesso-negado"); 
            return false;
        }

        // Adiciona atributos na request para serem usados nas views (JSP/Thymeleaf)
        populateRequestAttributes(request, usuarioLogado);

        // Usuário logado e com permissão, permite o acesso
        return true;
    }

    /**
     * Verifica se a URI é uma página de autenticação ou a página inicial.
     */
    private boolean isAuthPage(String uri) {
        return uri.equals("/") || // Adicionado para permitir a home page
               uri.equals("/login") || 
               uri.equals("/cadastro") || 
               uri.equals("/cadastro/aluno") || 
               uri.equals("/cadastro/professor");
    }

    /**
     * Verifica se a URI é um asset estático (CSS, JS, Imagens).
     */
    private boolean isPublicAsset(String uri) {
        return uri.startsWith("/css/") || 
               uri.startsWith("/js/") || 
               uri.startsWith("/images/") ||
               uri.startsWith("/assets/");
    }

    /**
     * Verifica se o usuário (Aluno/Professor) tem acesso à rota (prefixo /aluno/ ou /professor/).
     */
    private boolean hasAccessToPath(String uri, Usuario usuario) {
        if (uri.startsWith("/professor/") && !(usuario instanceof Professor)) {
            return false; // Aluno tentando acessar rota de professor
        }
        if (uri.startsWith("/aluno/") && !(usuario instanceof Aluno)) {
            return false; // Professor tentando acessar rota de aluno
        }
        return true; // Acesso permitido
    }

    /**
     * Retorna a URL da home page com base no tipo de usuário.
     */
    private String getHomeUrl(Usuario usuario) {
        if (usuario instanceof Professor) {
            return "/professor/home";
        } else if (usuario instanceof Aluno) {
            return "/aluno/home";
        }
        return "/"; // Fallback
    }

    /**
     * Popula a request com atributos úteis para as views.
     */
    private void populateRequestAttributes(HttpServletRequest request, Usuario usuario) {
        request.setAttribute("usuario", usuario); // Objeto genérico
        request.setAttribute("usuarioNome", usuario.getNome());
        request.setAttribute("usuarioEmail", usuario.getEmail());

        if (usuario instanceof Professor professor) {
            request.setAttribute("professor", professor); // Objeto específico
            request.setAttribute("tipoUsuario", "PROFESSOR");
            request.setAttribute("isProfessor", true);
            request.setAttribute("isAluno", false);
        } else if (usuario instanceof Aluno aluno) {
            request.setAttribute("aluno", aluno); // Objeto específico
            request.setAttribute("tipoUsuario", "ALUNO");
            request.setAttribute("isProfessor", false);
            request.setAttribute("isAluno", true);
        }
    }
}