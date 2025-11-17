package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Exibicao;

import org.springframework.stereotype.Controller; // <-- ADICIONADO
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping; // <-- ADICIONADO
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.uel.educatio.quiz.model.Area;
import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.model.QuizQuestao;
import br.uel.educatio.quiz.service.QuestaoService;
import br.uel.educatio.quiz.service.QuizService;
import br.uel.educatio.quiz.service.ProfessorService;
import br.uel.educatio.quiz.service.AreaService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession; // <-- ADICIONADO

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;


@Controller // <-- ADICIONADO (ESSENCIAL)
@RequestMapping("/quiz") // <-- ADICIONADO (Define o prefixo da URL para esta classe)
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
             quiz = new Quiz(); //Criar novo quiz
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("professor", professor);
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("dificuldades", Dificuldade.values());
        model.addAttribute("exibicoes", Exibicao.values());
        model.addAttribute("areas", areas);

        return "professor/criar_quiz";
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

                // 1. BUSCA O NOME DO PROFESSOR E ÁREA (Para exibição)
                Professor criador = profService.buscarPorId(quiz.getProfessor_criador());
                if (criador != null) {
                    // Injeta o nome no Model Quiz para exibição no card
                    quiz.setProfessor_criador_nome(criador.getNome()); 
                }
                Area area = areaService.buscarPorId(quiz.getArea()); 
                if (area != null) {
                    // Injeta o nome da área no Model Quiz para exibição no card
                    quiz.setNome_area(area.getNomeArea()); 
                }

                // 2. Contagem de Questões
                List<QuizQuestao> questoes_quiz = quizService.buscarPorIdQuestoes_Quiz(id_quiz);
                quiz.setNum_questoes(questoes_quiz.size());
            }

            // Adiciona a lista completa e corrigida para o Thymeleaf (com nome correto 'quizzes')
            model.addAttribute("quizzes", quizzes); 

            return "professor/meus_quizzes"; 
        } catch (RuntimeException e) {
            // Lógica de erro
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/professor/home"; 
        }
    }
        //Vindo de prof/home.html e o editar de editar_quiz.html
        // @GetMapping({"/novo", "/editar/{id}"})
        // public String mostrarFormularioQuiz(@PathVariable(required = false) Long id_quiz, Model model, HttpSession session) {
        //     Quiz quiz;

        //     Professor professor = (Professor) session.getAttribute("usuarioLogado");
        //     Long id_prof = professor.getId_professor();
            
        //     List<Area> areas = profService.buscarAreasDoProfessor(id_prof);

        //     if (id != null) {
        //         // MODO EDIÇÃO
        //         quiz = quizService.buscarPorId(id_quiz).else(Null); 
        //     } else {
        //         // MODO CRIAÇÃO
        //             quiz = new Quiz();
        //             // System.out.println("Questão: " + questao.getEnunciado());

        //             // for (int i = 0; i < 5; i++) {
        //             //     alternativas.add(new Alternativa());
        //             // }

        //             // O objeto 'questao' precisa ser ligado à lista de alternativas
        //             // É importante garantir que esta lista seja setada na 'questao'
        //             // questao.setAlternativas(alternativas); // <- IMPORTANTE: SETAR A LISTA NA QUESTÃO
        //             // System.out.println("Alternativas: " + questao.getAlternativas().size());
        //         // Inicializa a lista de alternativas no objeto vazio (opcional, mas recomendado)
        //         // Se usar 5 slots fixos no HTML, talvez seja melhor preencher 5 objetos Alternativa aqui.
        //     }

        //     // Passa os ENUMs e a Questão para o formulário
        //     model.addAttribute("quiz", quiz);
        //     // model.addAttribute("alternativas", alternativas);
        //     model.addAttribute("tiposQuestao", TipoQuestao.values());
        //     model.addAttribute("escolaridades", Escolaridade.values());
        //     model.addAttribute("dificuldades", Dificuldade.values());
        //     model.addAttribute("exibicoes", Exibicao.values());
        //     model.addAttribute("areasProfessor", areas);
        //     model.addAttribute("professor", professor);
        //     // model.addAttribute("professor", id_prof); // Simula o ID do professor logado

        //     return "professor/editar_quiz"; // Nome do arquivo HTML
        // }


    @PostMapping({"/salvar", "/atualizar"})
    public String salvar_quiz(@Valid @ModelAttribute Quiz quiz, Model model, HttpSession session, RedirectAttributes ra){
        System.out.println("Quiz: " + quiz.getProfessor_criador());
        System.out.println("Quiz: " + quiz.getPin_acesso());
        
        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        Long id_prof = professor.getId_professor();
        
        // ra.addAttribute("id_prof", id_prof);
        
        Quiz quizSalvo;
        
        try {
            quizSalvo = quizService.salvar(quiz, id_prof);
            System.out.println("Quiz salvo com sucesso!");
            
            return "redirect:/quiz/editar/questoes/" + quizSalvo.getId_quiz();

        } catch (RuntimeException e){
            System.out.println(e.getMessage());
            return "professor/criar_quiz";
        }

    }

    
    @GetMapping("/editar/questoes/{id_quiz}")
    public String editar_questoes_quiz(@PathVariable("id_quiz") Long id_quiz, Model model, HttpSession session, RedirectAttributes ra){
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        
        Quiz quiz = quizService.buscarPorId(id_quiz).get();
        List<Questao> questoes = new ArrayList<>();
        List<QuizQuestao> questoes_quiz = quizService.buscarPorIdQuestoes_Quiz(id_quiz);
        
        for (QuizQuestao qq : questoes_quiz){
            Questao questao = questaoService.buscarQuestao(qq.getId_questao());
            
            questoes.add(questao);
        }

        System.out.println("Quiz: " + quiz.getTitulo());
        System.out.println("Questões: " + questoes.size());
        
        model.addAttribute("quiz", quiz);
        model.addAttribute("questoes", questoes);
        model.addAttribute("professor", professor);
        return "professor/editar_quiz";
    }

    
    @GetMapping("/listar/quiz/areas/{id}")
    public String listarAreasDoQuiz(@PathVariable("id") Long id_quiz, Model model){
        model.addAttribute("Areas", quizService.listarQuizAreas(id_quiz));
        return "pageFictícia";
    }

    //Usada para adicionar questões ao quiz
     @GetMapping("/banco_questoes")
     public String acessarBancoQuestoes(@RequestParam(value = "quizId", required = false) Long quizId, Model model, HttpSession session) {

        Professor professor = (Professor) session.getAttribute("usuarioLogado");

        System.out.println("Quiz ID: " + quizId);
        // 1. Buscar todas as questões (ou apenas as do professor)

        List<Questao> questoes = questaoService.listarQuestoesPorProf(professor.getId_professor());

        // 2. Adicionar as questões e o professor ao Model
        model.addAttribute("questoes", questoes);
        model.addAttribute("professor", professor);
        model.addAttribute("quizId", quizId);

        return "professor/banco_questoes";
    }


    @PostMapping("/excluir/{id}")
    public String deletarQuiz(@PathVariable("id") Long id_quiz, Model model, HttpSession session, RedirectAttributes ra) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        
        try {
            quizService.deletarQuiz(id_quiz);
            System.out.println("Quiz deletado com sucesso!");
            ra.addFlashAttribute("mensagem", "Quiz deletado com sucesso!");
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            System.out.println(e.getMessage());
        }
        return "redirect:/quiz/professor_criador/buscarQuizzes/" + id_prof;
    }

    @GetMapping("/listar")
    public String listar(Model model){
        model.addAttribute("Quizzes", quizService.listarTodos());
        return "pageFictícia";
    }

    @GetMapping("/buscar/pin")
    public String buscarPorPin(@RequestParam String pin, Model model, RedirectAttributes ra){
        try {
            model.addAttribute("Quizzes", quizService.buscarPorPin(pin));
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }

    @GetMapping("/buscar/{id}")
    public String buscarPorId(@PathVariable("id") long id, Model model, RedirectAttributes ra){
        try {
            model.addAttribute("Quizzes", quizService.buscarPorId(id));
            return "ficticio"; //"form"
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ficticio";
        }
    }


    /**
     * Recebe o ID da questão e o ID do quiz do formulário POST
     * na tela de banco_questoes e adiciona a questão ao quiz.
     */
    @PostMapping("/adicionar/questao")
    public String adicionarQuestaoAoQuiz(@RequestParam("id_questao") Long idQuestao, @RequestParam("id_quiz") Long idQuiz, Model model, RedirectAttributes ra){
        System.out.println("Quiz ID hehe: " + idQuiz);
        Quiz quiz = quizService.buscarPorId(idQuiz).get();
        try {
            // Chamada ao Service para persistir a relação Questao-Quiz.
            // VOCÊ DEVE IMPLEMENTAR O MÉTODO 'adicionarQuestao' no QuizService
            // para criar e salvar a entidade QuizQuestao.
            quiz.setNum_questoes(quiz.getNum_questoes() + 1);
            quizService.adicionarQuestaoAoQuiz(idQuestao, idQuiz); 
            
            String mensagem = "Questão adicionada ao quiz com sucesso!";
            
            System.out.println(mensagem);
            ra.addFlashAttribute("mensagemSucesso", "mensagem");

            // Redireciona de volta para a tela de edição do quiz.
            return "redirect:/quiz/editar/questoes/" + idQuiz;

        } catch (RuntimeException e) {
            ra.addFlashAttribute("mensagemErro", "Erro ao adicionar questão ao quiz: " + e.getMessage());

            System.out.println("Erro ao adicionar questão ao quiz: " + e.getMessage());
            // Se houver erro, redireciona de volta para o banco de questões (mantendo o quizId na URL)
            return "redirect:/quiz/banco_questoes?quizId=" + idQuiz;
        }
    }
    
    @PostMapping("/excluir/questao")
    public String deletarQuestaoDoQuiz(@RequestParam("id_questao") Long id_questao, @RequestParam("id_quiz") Long id_quiz, Model model, HttpSession session, RedirectAttributes ra) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        System.out.println("chegou heehehe");
        try {
            quizService.deletarQuestaoDoQuiz(id_questao, id_quiz);
            System.out.println("Questao deletada do quiz com sucesso!");
            ra.addFlashAttribute("mensagem", "Questao deletada do quiz com sucesso!");
        } catch (RuntimeException e){
            ra.addFlashAttribute("error", e.getMessage());
            System.out.println(e.getMessage());
        }
        return "redirect:/quiz/editar/questoes/" + id_quiz;
    }
    
    // @PostMapping("salvar")
    // public String criar(@Valid @ModelAttribute Quiz quiz, Model model, HttpSession session, RedirectAttributes ra){
    //     try {
    //         Professor professor = (Professor) session.getAttribute("usuarioLogado");
            
    //         quiz.setProfessor_criador(professor.getId_professor());
    //         // quiz.setArea(professor.getArea()); // Pega a primeira área do professor
    //         quizService.criar(quiz);
            
    //         // ra.addFlashAttribute("mensagem", "Quiz criado com sucesso!");
    //         return "editar_quiz"; //"form"
    //     } catch (RuntimeException e){
    //         System.out.println(e.getMessage());
    //         // ra.addFlashAttribute("error", "Houve algum erro na criação do quiz!");
    //         return "redirect:/professor/criar_quiz";
    //     }
    // }
}