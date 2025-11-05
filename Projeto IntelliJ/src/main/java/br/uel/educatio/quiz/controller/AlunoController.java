package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.dao.AlternativaDAO;
import br.uel.educatio.quiz.dao.QuestaoDAO;
import br.uel.educatio.quiz.dao.QuizDAO;
import br.uel.educatio.quiz.model.*;
import br.uel.educatio.quiz.service.QuizService;
import br.uel.educatio.quiz.service.RespostaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    @Autowired
    private QuizService quizService;
    @Autowired
    private RespostaService respostaService;
    @Autowired
    private QuizDAO quizDAO;
    @Autowired
    private QuestaoDAO questaoDAO;
    @Autowired
    private AlternativaDAO alternativaDAO;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }
        model.addAttribute("aluno", aluno);
        return "aluno/home";
    }

    @GetMapping("/quizzes")
    public String listarQuizzes(HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }
        List<Quiz> quizzes = quizService.buscarQuizzesPublicosPorNivel(
            aluno.getNivel_educacional().getDisplayValue()
        );

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("aluno", aluno);
        return "aluno/listar_quizzes";
    }

    @GetMapping("/quizzes/buscar-pin")
    public String buscarPorPin(
            @RequestParam String pin,
            HttpSession session,
            Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }
        Quiz quiz = quizService.buscarPorPin(pin).orElse(null);
        if (quiz == null) {
            model.addAttribute("erro", "PIN inválido ou quiz não encontrado");
            return "redirect:/aluno/quizzes";
        }
        return "redirect:/aluno/quizzes/" + quiz.getId_quiz() + "/realizar";
    }

    @GetMapping("/quizzes/{id}/realizar")
    public String realizarQuiz(@PathVariable long id, HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }

        if (respostaService.alunoJaRealizouQuiz(aluno.getId_aluno(), id)) {
            return "redirect:/aluno/quizzes/" + id + "/resultado";
        }

        Quiz quiz = quizService.buscarPorId(id).orElse(null);
        if (quiz == null) {
            return "redirect:/aluno/quizzes";
        }

        for (Questao questao : quiz.getQuestoes()) {
            List<Alternativa> alternativas = alternativaDAO.findByQuestaoId(questao.getId_questao());
            questao.setAlternativas(alternativas);
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("aluno", aluno);
        return "aluno/realizar_quiz";
    }

    @PostMapping("/quizzes/{id}/submeter")
    public String submeterRespostas(
            @PathVariable long id,
            @RequestParam Map<String, String> respostasMap,
            HttpSession session) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }

        Quiz quiz = quizService.buscarPorId(id).orElse(null);
        if (quiz == null) {
            return "redirect:/aluno/quizzes";
        }

        List<Resposta> respostas = new ArrayList<>();
        List<Integer> pontuacoes = new ArrayList<>();
        List<br.uel.educatio.quiz.model.enums.TipoQuestao> tipos = new ArrayList<>();

        for (Questao questao : quiz.getQuestoes()) {
            String chave = "questao_" + questao.getId_questao();
            String respostaAluno = respostasMap.get(chave);

            if (respostaAluno != null && !respostaAluno.isEmpty()) {
                Resposta resposta = new Resposta();
                resposta.setId_questao(questao.getId_questao());
                if (questao.getTipo_questao() == br.uel.educatio.quiz.model.enums.TipoQuestao.PREENCHER_LACUNA) {
                    resposta.setResposta_aluno_texto(respostaAluno);
                } else {
                    resposta.setResposta_aluno_num(Integer.parseInt(respostaAluno));
                }
                respostas.add(resposta);
                pontuacoes.add(questao.getPontuacao());
                tipos.add(questao.getTipo_questao());
            }
        }
        respostaService.salvarRespostas(aluno.getId_aluno(), id, respostas, pontuacoes, tipos);
        return "redirect:/aluno/quizzes/" + id + "/resultado";
    }

    @GetMapping("/quizzes/{id}/resultado")
    public String verResultado(@PathVariable long id, HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }

        Quiz quiz = quizService.buscarPorId(id).orElse(null);
        if (quiz == null) {
            return "redirect:/aluno/quizzes";
        }

        int pontuacaoTotal = respostaService.calcularPontuacaoTotal(aluno.getId_aluno(), id);
        int acertos = respostaService.contarAcertos(aluno.getId_aluno(), id);
        int totalQuestoes = quizService.contarQuestoes(id);
        int pontuacaoMaxima = quizService.calcularPontuacaoMaxima(id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("pontuacaoTotal", pontuacaoTotal);
        model.addAttribute("acertos", acertos);
        model.addAttribute("totalQuestoes", totalQuestoes);
        model.addAttribute("pontuacaoMaxima", pontuacaoMaxima);
        model.addAttribute("aluno", aluno);
        return "aluno/resultado_quiz";
    }

    @GetMapping("/historico")
    public String verHistorico(HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }
        List<Quiz> quizzes = quizDAO.findQuizzesFeitos(aluno.getId_aluno());
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("aluno", aluno);
        return "aluno/historico";
    }

    @GetMapping("/quizzes/{id}/revisar")
    public String revisarQuiz(@PathVariable long id, HttpSession session, Model model) {
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }

        Quiz quiz = quizService.buscarPorId(id).orElse(null);
        if (quiz == null) {
            return "redirect:/aluno/quizzes";
        }

        List<Resposta> respostas = respostaService.buscarRespostasDoAluno(aluno.getId_aluno(), id);
        for (Questao questao : quiz.getQuestoes()) {
            List<Alternativa> alternativas = alternativaDAO.findByQuestaoId(questao.getId_questao());
            questao.setAlternativas(alternativas);
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("respostas", respostas);
        model.addAttribute("aluno", aluno);
        return "aluno/revisar_quiz";
    }
}