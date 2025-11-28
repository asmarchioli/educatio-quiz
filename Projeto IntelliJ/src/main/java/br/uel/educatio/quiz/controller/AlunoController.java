package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.enums.TipoQuestao;
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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import br.uel.educatio.quiz.model.dto.DesempenhoDTO;
import br.uel.educatio.quiz.model.dto.EstatisticaDTO;
import br.uel.educatio.quiz.dao.DesempenhoAlunoDAO;
import br.uel.educatio.quiz.service.AreaService;
import br.uel.educatio.quiz.model.dto.RankingDTO;

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
    @Autowired
    private DesempenhoAlunoDAO desempenhoDAO;
    @Autowired
    private AreaService areaService;
    

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
            return "redirect:/login";  //o man, essas verificações de se o aluno/professor está logado são necessárias? O interceptor já faz isso, não? E essa IA do Replit é boa em ->>> VERIFICAR
        }
        List<Quiz> quizzes = quizService.buscarQuizzesPublicosPorNivel(
            aluno.getNivel_educacional().getDisplayValue()
        );

        for (Quiz quiz : quizzes) {
            try {
                Area areaObj = areaService.buscarPorId(quiz.getArea());
                quiz.setNome_area(areaObj.getNomeArea());
            } catch (Exception e) {
                quiz.setNome_area("Área Desconhecida");
            }
        }

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

        /*if (respostaService.alunoJaRealizouQuiz(aluno.getId_aluno(), id)) {
            return "redirect:/aluno/quizzes/" + id + "/resultado";
        }*/                             // ALUNO PODE REALIZAR O QUIZ MAIS DE UMA VEZ!! 

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

        int ultimaTentativa = respostaService.buscarUltimaTentativa(aluno.getId_aluno(), id);
        int pontuacaoTotal = respostaService.calcularPontuacaoTotal(aluno.getId_aluno(), id, ultimaTentativa);
        int acertos = respostaService.contarAcertos(aluno.getId_aluno(), id, ultimaTentativa);
        int totalQuestoes = quizService.contarQuestoes(id);
        int pontuacaoMaxima = quizService.calcularPontuacaoMaxima(id);

        //Busca todas as tentativas do aluno no quiz (para o histórico)
        List<Integer> tentativas = respostaService.buscarTodasTentativas(aluno.getId_aluno(), id);

        List<RankingDTO> ranking = quizService.buscarRankingQuiz(id);
        model.addAttribute("ranking", ranking);

        model.addAttribute("quiz", quiz);
        model.addAttribute("pontuacaoTotal", pontuacaoTotal);
        model.addAttribute("acertos", acertos);
        model.addAttribute("totalQuestoes", totalQuestoes);
        model.addAttribute("pontuacaoMaxima", pontuacaoMaxima);
        
        model.addAttribute("ultimaTentativa", ultimaTentativa);
        model.addAttribute("tentativas", tentativas);
        
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
    public String revisarQuiz(
            @PathVariable long id, 
            @RequestParam(required = false, defaultValue = "0") int tentativa,
            HttpSession session,
            Model model) {
        
        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) {
            return "redirect:/login";
        }

        Quiz quiz = quizService.buscarPorId(id).orElse(null);
        if (quiz == null) {
            return "redirect:/aluno/quizzes";
        }

        int tentativaParaRevisar = tentativa;
        if (tentativaParaRevisar == 0) {
            tentativaParaRevisar = respostaService.buscarUltimaTentativa(aluno.getId_aluno(), id);
        }

        List<Resposta> respostas = respostaService.buscarRespostasDoAlunoPorTentativa(aluno.getId_aluno(), id, tentativaParaRevisar);
        //List<Resposta> respostas = respostaService.buscarRespostasDoAluno(aluno.getId_aluno(), id);
        
        for (Questao questao : quiz.getQuestoes()) {
            List<Alternativa> alternativas = alternativaDAO.findByQuestaoId(questao.getId_questao());
            questao.setAlternativas(alternativas);
        }

        List<Integer> tentativas = respostaService.buscarTodasTentativas(aluno.getId_aluno(), id);

        model.addAttribute("quiz", quiz);
        model.addAttribute("respostas", respostas);

        model.addAttribute("tentativaAtual", tentativaParaRevisar);
        model.addAttribute("tentativas", tentativas);
        
        model.addAttribute("aluno", aluno);
        return "aluno/revisar_quiz";
    }

    @GetMapping("/desempenho")
    public String verDesempenho(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false, defaultValue = "all") String periodo,
            HttpSession session, 
            Model model) {

        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) return "redirect:/login";

        // 1. Lógica de Data para o carregamento inicial da página
        LocalDate dataCorte = null;
        if ("3d".equals(periodo)) dataCorte = LocalDate.now().minusDays(3);
        else if ("7d".equals(periodo)) dataCorte = LocalDate.now().minusWeeks(1);
        else if ("30d".equals(periodo)) dataCorte = LocalDate.now().minusMonths(1);

        // 2. Buscas no DAO
        List<DesempenhoDTO> historico = desempenhoDAO.buscarHistorico(aluno.getId_aluno(), areaId, dataCorte);
        List<EstatisticaDTO> statsDificuldade = desempenhoDAO.buscarDesempenhoPorDificuldade(aluno.getId_aluno(), areaId);

        // 3. Média Geral
        double totalPontosObtidos = historico.stream().mapToDouble(DesempenhoDTO::getNotaObtida).sum();
        double totalPontosPossiveis = historico.stream().mapToDouble(DesempenhoDTO::getNotaMaxima).sum();
        double mediaGeral = 0.0;
        if (totalPontosPossiveis > 0) {
            mediaGeral = (totalPontosObtidos / totalPontosPossiveis) * 100.0;
        }
        if (mediaGeral > 100.0) mediaGeral = 100.0; 

        model.addAttribute("aluno", aluno);
        model.addAttribute("areas", areaService.listarTodas());

        // Mantém os filtros selecionados na tela
        model.addAttribute("areaSelecionada", areaId);
        model.addAttribute("periodoSelecionado", periodo);

        model.addAttribute("totalQuizzes", historico.size());
        model.addAttribute("mediaGeral", String.format("%.1f", mediaGeral));

        // Dados JSON para o JS renderizar inicialmente
        model.addAttribute("dadosGraficoLinha", historico);
        model.addAttribute("dadosGraficoDificuldade", statsDificuldade);

        return "aluno/desempenho";
    }

    @GetMapping("/api/desempenho-dados")
    @ResponseBody
    public Map<String, Object> getDadosDesempenho(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false, defaultValue = "all") String periodo,
            HttpSession session) {

        Aluno aluno = (Aluno) session.getAttribute("usuarioLogado");
        if (aluno == null) return null;

        // 1. Lógica de Data
        LocalDate dataCorte = null;
        if ("3d".equals(periodo)) dataCorte = LocalDate.now().minusDays(3);
        else if ("7d".equals(periodo)) dataCorte = LocalDate.now().minusWeeks(1);
        else if ("30d".equals(periodo)) dataCorte = LocalDate.now().minusMonths(1);

        // 2. Busca Dados
        List<DesempenhoDTO> historico = desempenhoDAO.buscarHistorico(aluno.getId_aluno(), areaId, dataCorte);
        List<EstatisticaDTO> statsDificuldade = desempenhoDAO.buscarDesempenhoPorDificuldade(aluno.getId_aluno(), areaId);

        // 3. Recalcula KPIs
        double totalPontosObtidos = historico.stream().mapToDouble(DesempenhoDTO::getNotaObtida).sum();
        double totalPontosPossiveis = historico.stream().mapToDouble(DesempenhoDTO::getNotaMaxima).sum();
        double mediaGeral = 0.0;
        if (totalPontosPossiveis > 0) {
            mediaGeral = (totalPontosObtidos / totalPontosPossiveis) * 100.0;
        }
        if (mediaGeral > 100.0) mediaGeral = 100.0;

        // 4. Monta o JSON de resposta
        Map<String, Object> response = new HashMap<>();
        response.put("totalQuizzes", historico.size());
        // Formata com ponto para garantir compatibilidade com JS
        response.put("mediaGeral", String.format("%.1f", mediaGeral).replace(",", ".")); 
        response.put("historico", historico);
        response.put("dificuldade", statsDificuldade);

        return response;
    }

}