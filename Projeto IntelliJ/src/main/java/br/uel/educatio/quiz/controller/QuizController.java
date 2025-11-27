package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Exibicao;

import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.uel.educatio.quiz.model.Area;
import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.QuizQuestao;
import br.uel.educatio.quiz.model.dto.RankingDTO;
import br.uel.educatio.quiz.model.dto.RankingQuestsDTO;
import br.uel.educatio.quiz.model.dto.GraficoQuestsDTO;

import br.uel.educatio.quiz.service.QuestaoService;
import br.uel.educatio.quiz.service.QuizService;
import br.uel.educatio.quiz.service.ProfessorService;
import br.uel.educatio.quiz.service.AreaService;


import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession; 

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;


@Controller 
@RequestMapping("/quiz") 
public class QuizController {
    private final QuizService quizService;
    private final ProfessorService profService;
    private final AreaService areaService;
    private final QuestaoService questaoService;


    public QuizController(QuizService QuizService, ProfessorService profService, AreaService areaService, QuestaoService questaoService) {
        this.quizService = QuizService;
        this.profService = profService;
        this.areaService = areaService;
        this.questaoService = questaoService;
    }

    // Usada em professor/home.html e em editar_quiz.html
    @GetMapping({"/criar", "/modificar/{id_quiz}"})
    public String forms_criar_quiz(@PathVariable(required=false) Long id_quiz , HttpSession session, Model model) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        Quiz quiz;
        Long id_prof = professor.getId_professor();
        List<Area> areas = profService.buscarAreasDoProfessor(id_prof);
        
        if (id_quiz != null) { //Atualizar informações do quiz
            quiz = quizService.buscarQuizPorId(id_quiz).get();
            System.out.println("Quiz: " + quiz.getTitulo());
         } else {
             quiz = new Quiz(); 
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("professor", professor);
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("dificuldades", Dificuldade.values());
        model.addAttribute("exibicoes", Exibicao.values());
        model.addAttribute("areas", areas);

        return "professor/informacoes_quiz";
    }

    
    //Usada em professor/home.html (meus quizzes)
    @GetMapping("/professor_criador/buscarQuizzes/{id}")
    public String buscarQuizzesPorIdProfessor(@PathVariable("id") Long id_prof, Model model, RedirectAttributes ra) {
        try {
            List<Quiz> quizzes = quizService.buscarPorProfessorCriador(id_prof);

            if (quizzes.isEmpty()) {
                model.addAttribute("quizzes", quizzes); 
                return "professor/meus_quizzes"; 
            }

            for (Quiz quiz : quizzes) { // Itera sobre a lista
                Long id_quiz = quiz.getId_quiz();

                
                Professor criador = profService.buscarPorId(quiz.getProfessor_criador());
                if (criador != null) {
                    quiz.setProfessor_criador_nome(criador.getNome()); 
                }
                Area area = areaService.buscarPorId(quiz.getArea()); 
                if (area != null) {
                    quiz.setNome_area(area.getNomeArea()); 
                }

                quiz.setQtd_resolucoes(quizService.numeroResolucoesQuiz(id_quiz));
                
                List<QuizQuestao> questoes_quiz = quizService.buscarPorIdQuestoes_Quiz_Filtrado(id_quiz, null, null);
                quiz.setNum_questoes(questoes_quiz.size());
            }

            model.addAttribute("quizzes", quizzes); 

            return "professor/meus_quizzes"; 
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/professor/home"; 
        }
    }


    
    @GetMapping("/relatorios")
    public String acessarRelatorios(@RequestParam(value = "id_quiz") Long quizId, Model model, RedirectAttributes ra){
        System.out.println("Quiz ID: " + quizId);
        
        Quiz quiz = quizService.buscarPorId(quizId).get();

        if (quiz == null){
            ra.addFlashAttribute("error", "Quiz não encontrado!");
        }
        
        System.out.println("Quiz: " + quiz.getTitulo());
        model.addAttribute("quiz", quiz);
        return "professor/relatorios";
    }


    
    @PostMapping({"/salvar", "/atualizar"})
    public String salvar_quiz(@Valid @ModelAttribute Quiz quiz, Model model, HttpSession session, RedirectAttributes ra){
        System.out.println("Quiz: " + quiz.getProfessor_criador());
        System.out.println("Quiz: " + quiz.getPin_acesso());
        
        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        Long id_prof = professor.getId_professor();
        
        ra.addAttribute("id_prof", id_prof);
        
        Quiz quizSalvo;
        
        try {
            quizSalvo = quizService.salvar(quiz, id_prof);
            System.out.println("Quiz salvo com sucesso!");
            ra.addFlashAttribute("success", "Quiz salvo com sucesso!");
            
            return "redirect:/quiz/editar/questoes?id_quiz=" + quizSalvo.getId_quiz();

        } catch (RuntimeException e){
            ra.addFlashAttribute("error", "Erro ao salvar: " + e.getMessage());
            System.out.println(e.getMessage());
            return "professor/criar_quiz";
        }

    }

    
    @GetMapping("/relatorios/desempenho")
    public String acessarDesempenhoAlunos(@RequestParam(value = "id_quiz") Long id_quiz,
                                          @RequestParam(value = "termoBusca", required = false) String termoBusca,
                                          Model model, 
                                          RedirectAttributes ra)
    {    
        System.out.println("Quiz ID: " + id_quiz);

        Quiz quiz = quizService.buscarPorId(id_quiz).get();

        List<QuizQuestao> questoes_quiz = quizService.buscarPorIdQuestoes_Quiz_Filtrado(id_quiz, null, null);
        quiz.setNum_questoes(questoes_quiz.size());

        List<RankingDTO> ranking;

        if (termoBusca != null && !termoBusca.isEmpty()) {
            // Se o usuário digitou algo, busca pelo nome
            System.out.println("Termo de busca: " + termoBusca);
            ranking = quizService.buscarRankingQuizPorNome(id_quiz, termoBusca);
        } else {
            System.out.println("Termo de busca vazio");
            
            // Se está vazio, traz todo o ranking
            ranking = quizService.buscarRankingQuiz(id_quiz);
        }

        ranking.forEach(r -> System.out.println(r.getNomeAluno()));
    
        
        if (quiz == null){
            ra.addFlashAttribute("error", "Quiz não encontrado!");
        }

        System.out.println("Quiz: " + quiz.getTitulo());
        model.addAttribute("pontuacaoMaxima", quizService.pontuacaoMaximaQuiz(id_quiz));
        model.addAttribute("termoBuscaDigitado", termoBusca);
        model.addAttribute("ranking", ranking);
        model.addAttribute("quiz", quiz);
        return "professor/relatorio_alunos";
    }


    
    @GetMapping("/relatorios/questoes")
    public String acessarRankingQuestoes(@RequestParam(value = "id_quiz") Long id_quiz, Model model, RedirectAttributes ra)
    {    
        // 1. Busca o Quiz para o cabeçalho
        Quiz quiz = quizService.buscarPorId(id_quiz).get();
        
        // 2. Busca as questões difíceis
        List<RankingQuestsDTO> questoes = quizService.buscarRankingQuests(id_quiz);
        
        System.out.println("Questões: " + questoes.size());
        questoes.forEach(q -> System.out.println(q.getQtdRespostas()));
        model.addAttribute("questoes", questoes);
        model.addAttribute("quiz", quiz);
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        
        return "professor/relatorio_questoes";
    }

    
    @GetMapping("/relatorios/grafico")
    public String acessarGraficoDesempenho(@RequestParam(value = "id_quiz") Long id_quiz, Model model, RedirectAttributes ra)
    {    
        System.out.println("Quiz ID: " + id_quiz);

        Quiz quiz = quizService.buscarPorId(id_quiz).get();

        if (quiz == null){
            ra.addFlashAttribute("error", "Quiz não encontrado!");
        }

        List<GraficoQuestsDTO> dadosGrafico = quizService.DadosGraficoQuests(id_quiz);

        System.out.println("Dados do gráfico: " + dadosGrafico.size());
        dadosGrafico.forEach(d -> System.out.println(d.getLabel()));
        
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("quiz", quiz);
        model.addAttribute("dados", dadosGrafico);
        return "professor/grafico_questoes";
    }

    
    
    @GetMapping("/editar/questoes")
    public String editar_questoes_quiz(@RequestParam("id_quiz") Long id_quiz,
                                       @RequestParam(value = "filtroTipo", required = false) String filtroTipo, 
                                       @RequestParam(value = "termoBusca", required = false) String termoBusca, 
                                       @RequestParam(value = "quizId", required = false) Long quizId,
                                       Model model, HttpSession session, 
                                       RedirectAttributes ra){
        
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        
        Quiz quiz = quizService.buscarPorId(id_quiz).get();
        List<Questao> questoes = new ArrayList<>();
        
        List<QuizQuestao> questoes_quiz = quizService.buscarPorIdQuestoes_Quiz_Filtrado(id_quiz, filtroTipo, termoBusca);
        
        for (QuizQuestao qq : questoes_quiz){
            Questao questao = questaoService.buscarQuestao(qq.getId_questao());
            questao.setPontuacao(qq.getPontuacao_questao());
            
            questoes.add(questao);
        }
        int totalPontos = quizService.calcularPontuacaoMaxima(id_quiz);
        
        System.out.println("Quiz: " + quiz.getTitulo());
        System.out.println("Questões: " + questoes.size());

        model.addAttribute("totalPontos", totalPontos);
        model.addAttribute("filtroTipoSelecionado", filtroTipo);
        model.addAttribute("termoBuscaDigitado", termoBusca);
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("quiz", quiz);
        model.addAttribute("questoes", questoes);
        model.addAttribute("professor", professor);
        return "professor/editar_quiz";
    }

    
    //Usada para adicionar questões ao quiz
     @GetMapping("/banco_questoes")
     public String acessarBancoQuestoes(@RequestParam(value = "quizId", required = false) Long quizId, Model model, HttpSession session) {

        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        System.out.println("Quiz ID no banco: " + quizId);
        System.out.println("Professor ID banco acesso: " + professor.getId_professor());
    
        Quiz quiz = quizService.buscarPorId(quizId).get();
        List<Questao> questoes = questaoService.listarQuestoesPorProf(professor.getId_professor());
         
            
        if (quiz != null){
             List<Questao> questoesFiltradas = questoes.stream()
                 .filter(q -> 
                     q.getArea().equals(quiz.getArea()) && 
                     q.getNivel_educacional() == quiz.getNivel_educacional()
                 )
                 .toList();

            model.addAttribute("questoes", questoesFiltradas);
        }    
        else{
            model.addAttribute("questoes", questoes);
        }

        model.addAttribute("professor", professor);
        model.addAttribute("quizId", quizId);
        model.addAttribute("tiposQuestao", TipoQuestao.values());
         
        return "professor/banco_questoes";
    }

    

    @PostMapping("/excluir")
    public String deletarQuiz(@RequestParam("id_quiz") Long id_quiz, Model model, HttpSession session, RedirectAttributes ra) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        
        try {
            quizService.deletarQuiz(id_quiz);
            System.out.println("Quiz deletado com sucesso!");
            ra.addFlashAttribute("success", "Quiz deletado com sucesso!");
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            System.out.println(e.getMessage());
        }
        return "redirect:/quiz/professor_criador/buscarQuizzes/" + id_prof;
    }


    @PostMapping("/adicionar/questao") 
    public String adicionarQuestaoAoQuiz(@RequestParam("id_questao") Long id_questao, 
                                         @RequestParam("id_quiz") Long id_quiz,
                                         Model model, 
                                         RedirectAttributes ra){

        // Busca o quiz para validações
        Quiz quiz = quizService.buscarPorId(id_quiz).orElseThrow(() -> new RuntimeException("Quiz não encontrado"));

        try {
            // Valida se a nova pontuação cabe no total de 100
            // Passa-se'null' no 3º parâmetro porque é uma INSERÇÃO, não edição
            quizService.validarPontuacao(id_quiz, 1, null); 

            //Incrementa contador (visual)
            quiz.setNum_questoes(quiz.getNum_questoes() + 1);

            quizService.adicionarQuestaoAoQuiz(id_questao, id_quiz, 1); 

            String mensagem = "Questão adicionada ao Quiz com sucesso!";
            ra.addFlashAttribute("success", mensagem);

            // Sucesso: Volta para a edição do quiz
            return "redirect:/quiz/editar/questoes?id_quiz=" + id_quiz;

        } catch (RuntimeException e) {
            // Erro (ex: passou de 100 pontos): Manda mensagem de erro
            ra.addFlashAttribute("error", "Erro ao adicionar: " + e.getMessage());
            System.out.println("Erro: " + e.getMessage());

            // Falha: Fica no banco de questões para tentar de novo
            return "redirect:/quiz/banco_questoes?quizId=" + id_quiz;
        }
    }

    
    @PostMapping("/atualizar/pontuacao")
    public String atualizarPontuacao(@RequestParam("id_questao") Long id_questao, 
                                     @RequestParam("id_quiz") Long id_quiz, 
                                     @RequestParam("pontuacao") int pontuacao,
                                     RedirectAttributes ra) {
        try {
            quizService.validarPontuacao(id_quiz, pontuacao, id_questao);
            quizService.atualizarPontuacaoQuestao(id_questao, id_quiz, pontuacao);
            ra.addFlashAttribute("success", "Pontuação atualizada!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        // Volta para a tela de edição
        return "redirect:/quiz/editar/questoes?id_quiz=" + id_quiz;
    }

    
    @PostMapping("/excluir/questao")
    public String deletarQuestaoDoQuiz(@RequestParam("id_questao") Long id_questao, @RequestParam("id_quiz") Long id_quiz, Model model, HttpSession session, RedirectAttributes ra) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        
        try {
            quizService.deletarQuestaoDoQuiz(id_questao, id_quiz);
            System.out.println("Questao deletada do quiz com sucesso!");
            ra.addFlashAttribute("success", "Questao deletada do quiz com sucesso!");
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            System.out.println(e.getMessage());
        }
        
        return "redirect:/quiz/editar/questoes?id_quiz=" + id_quiz;
    }
}