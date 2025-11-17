package br.uel.educatio.quiz.controller;

import br.uel.educatio.quiz.model.enums.Escolaridade;
import br.uel.educatio.quiz.model.enums.TipoQuestao;
import br.uel.educatio.quiz.model.enums.Dificuldade;
import br.uel.educatio.quiz.model.enums.Exibicao;
import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Quiz;
import br.uel.educatio.quiz.model.Alternativa;
import br.uel.educatio.quiz.model.Area;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.uel.educatio.quiz.model.Professor;
import br.uel.educatio.quiz.model.Questao;
import br.uel.educatio.quiz.service.AreaService;
import br.uel.educatio.quiz.service.QuestaoService;
import br.uel.educatio.quiz.service.ProfessorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
    
import java.util.Optional;
import java.lang.Long;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/questao")
public class QuestaoController {
    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private ProfessorService profService;
   
    @Autowired
    private AreaService areaService;

  
    // --- FUNÇÃO DE BUSCA E LISTAGEM (Detalhes) - JÁ FUNCIONA ---
    @GetMapping({"/novo", "/editar/{id_questao}"})
    public String mostrarFormularioQuestao(@PathVariable(required = false) Long id_questao, Model model, HttpSession session) {
        Questao questao;
        
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        List<Area> areas = profService.buscarAreasDoProfessor(id_prof);
        List<Alternativa> alternativas = new ArrayList<>();
            
        if (id_questao != null) {
            // MODO EDIÇÃO
            questao = questaoService.buscarQuestao(id_questao); 
            alternativas = questaoService.listarAlternativas(questao.getId_questao());
            
            // ** GARANTIA **: Se a questão existir, mas tiver menos de 5 alternativas, 
            // adicione alternativas vazias para preencher os 5 slots do formulário.
            while (alternativas.size() < 5) {
                alternativas.add(new Alternativa());
            }
             questao.setAlternativas(alternativas); // <-- IMPORTANTE
             
        } else {
            // MODO CRIAÇÃO
                questao = new Questao();
                System.out.println("Questão: " + questao.getEnunciado());
            
                for (int i = 0; i < 5; i++) {
                    alternativas.add(new Alternativa());
                }

                // O objeto 'questao' precisa ser ligado à lista de alternativas
                // É importante garantir que esta lista seja setada na 'questao'
                questao.setAlternativas(alternativas); // <- IMPORTANTE: SETAR A LISTA NA QUESTÃO
                System.out.println("Alternativas: " + questao.getAlternativas().size());
            // Inicializa a lista de alternativas no objeto vazio (opcional, mas recomendado)
            // Se usar 5 slots fixos no HTML, talvez seja melhor preencher 5 objetos Alternativa aqui.
        }

        // Passa os ENUMs e a Questão para o formulário
        model.addAttribute("questao", questao);
        model.addAttribute("alternativas", alternativas);
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("dificuldades", Dificuldade.values());
        model.addAttribute("exibicoes", Exibicao.values());
        model.addAttribute("areasProfessor", areas);
        model.addAttribute("professor", professor);
        // model.addAttribute("professor", id_prof); // Simula o ID do professor logado

        return "professor/editar_questao"; // Nome do arquivo HTML
    }

    
    
    // Usada em professor/home.html e em criar_quiz.html 
    @GetMapping("/listar/questoes/{id_prof}")
    public String listarQuestoesPorProfessor(
            @PathVariable("id_prof") Long id_prof,
            Model model
    ) {
        List<Questao> questoes = questaoService.listarQuestoesPorProf(id_prof);
        
        // questoes.forEach(q -> {
        //     List<Alternativa> alternativas = questaoService.listarAlternativas(q.getId_questao());

        //     //Remove alternativas com texto vazio ou nulo
        //     if (alternativas != null) {
        //         alternativas.removeIf(alternativa -> 
        //             alternativa.getTexto_alternativa() == null || 
        //             alternativa.getTexto_alternativa().trim().isEmpty()
        //         );
        //     }

        //     alternativas.forEach(a -> System.out.println(a.getTexto_alternativa()));
        //     q.setAlternativas(alternativas);
        // });

        model.addAttribute("questoes", questoes);
        return "professor/banco_questoes";
    }

    
    // Usada pelo botão 'Detalhes' no banco_questoes.html (GET)
    @GetMapping("/buscar/{id}")
    public String buscarQuestao(@PathVariable("id") Long id_questao, @RequestParam(value = "quizId", required = false) Long quizId, Model model, RedirectAttributes ra) {
        try{
            System.out.println("QuizId: " + quizId);
            System.out.println("QuestãoId: " + id_questao);
            Questao questao = questaoService.buscarQuestao(id_questao);
            Professor profCriador = profService.buscarPorId(questao.getProfessor_criador());
            Area questaoArea = areaService.buscarPorId(questao.getArea());
            
            // Carrega ENUMs para os Dropdowns da view de detalhes (opcional, mas bom para consistência)
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("dificuldades", Dificuldade.values());
            model.addAttribute("tiposQuestao", TipoQuestao.values());
            model.addAttribute("exibicoes", Exibicao.values());
            
            model.addAttribute("questao", questao);
            model.addAttribute("profCriador", profCriador);
            model.addAttribute("questaoAreaNome", questaoArea.getNomeArea());
            model.addAttribute("alternativas", questaoService.listarAlternativas(id_questao));
            model.addAttribute("quizId", quizId);

            // Retorna o template de detalhes
            return "detalhe_questao"; 
        } catch(Exception e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/professor/banco_questoes";
        }
    }

    // --- FUNÇÃO DE CRIAÇÃO E SALVAMENTO (POST) - JÁ FUNCIONA ---

    // Usada em professor/criar_questao.html
    @PostMapping({"/salvar", "/atualizar"})
    public String salvar_questao(@Valid @ModelAttribute Questao questao, Model model, HttpSession session, RedirectAttributes ra){
        System.out.println("Questão: " + questao.getProfessor_criador());
        
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        ra.addAttribute("id_prof", id_prof);
        
        try {
            questaoService.salvar(questao, id_prof);
            System.out.println("Questão salva com sucesso!");
            
        } catch (RuntimeException e){
            System.out.println(e.getMessage());
        }
        
        return "redirect:/questao/listar/questoes/" + id_prof;
    }

    // --- FUNÇÕES DE EDIÇÃO E ATUALIZAÇÃO (NOVAS) ---

    // 1. Mapeamento para abrir a página de Edição (GET)
    // // O HTML do botão 'Editar' aponta para /questao/editar/{id}
    // @GetMapping("/editar/{id}") 
    // public String editarQuestao(@PathVariable("id") Long id_questao, Model model, RedirectAttributes ra) {
    //     try {
    //         Questao questao = questaoService.buscarQuestao(id_questao);

    //         // Carrega ENUMs para os Dropdowns
    //         model.addAttribute("escolaridades", Escolaridade.values());
    //         model.addAttribute("dificuldades", Dificuldade.values());
    //         model.addAttribute("tiposQuestao", TipoQuestao.values());
    //         model.addAttribute("exibicoes", Exibicao.values());

    //         // Carrega a questão e suas alternativas para pré-preencher o formulário
    //         model.addAttribute("questao", questao);
    //         model.addAttribute("alternativas", questaoService.listarAlternativas(id_questao)); 

    //         // Retorna o template de edição
    //         return "professor/editar_questao"; 
    //     } catch(Exception e) {
    //         ra.addFlashAttribute("error", e.getMessage());
    //         return "redirect:/professor/banco_questoes";
    //     }
    // }

    // // 2. Mapeamento para salvar as alterações da Edição (POST)
    // @PostMapping("/atualizar")
    // public String atualizarQuestao(@Valid @ModelAttribute Questao questao, RedirectAttributes ra) {
    //      try {
    //         questaoService.atualizar(questao); // Assumindo que você tem um método atualizar no service
    //         ra.addFlashAttribute("success", "Questão atualizada com sucesso!");
    //         return "redirect:/professor/banco_questoes";
    //     } catch (RuntimeException e){
    //         ra.addFlashAttribute("error", "Erro ao atualizar a questão: " + e.getMessage());
    //         return "redirect:/questao/editar/" + questao.getId_questao(); // Volta para a página de edição com erro
    //     }
    // }

    // --- FUNÇÃO DE EXCLUSÃO (NOVA) ---

    // Mapeamento para excluir (POST - usado pelo formulário no banco_de_questoes.html)
    @PostMapping("/excluir")
    public String excluirQuestao(@RequestParam("id_questao") Long id_questao, RedirectAttributes ra, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        System.out.println("chegou opa");
        
        try {
            questaoService.deletarQuestaoDoBanco(id_questao); // Assumindo que você tem um método excluir no service
            // ra.addFlashAttribute("success", "Questão excluída com sucesso!");
            System.out.println("deu bom");
            
        } catch(Exception e) {
            ra.addFlashAttribute("error", "Erro ao excluir questão: " + e.getMessage());
            System.out.println(e.getMessage());
        }
        // ra.addFlashAttribute("id_prof", id_prof );
        return "redirect:/questao/listar/questoes/" + id_prof;
    }


    @GetMapping("/listar/{id}")
    public String listarTodasQuestoes(@PathVariable("id") Long id, Model model) {
        model.addAttribute("listaQuestoes", questaoService.listarQuestoes());
        return "paginaficticia";
    }

}

//Antigo código:
// @Controller
// @RequestMapping("/questao")
// public class QuestaoController {
//     @Autowired
//     private QuestaoService questaoService;

//     // Usada em professor/home.html e em criar_quiz.html 
//     @GetMapping("/listar/questoes/{id}")
//     public String listarQuestoesPorProfessor(
//             @PathVariable("id") Long id_prof, // Note que mudei o nome da variável local para 'id_prof'
//             Model model
//     ) {
//         List<Questao> questoes = questaoService.listarQuestoesPorProf(id_prof);
//         questoes.forEach(q -> {
//             List<Alternativa> alternativas = questaoService.listarAlternativas(q.getId_questao());

//             //Remove alternativas com texto vazio ou nulo
//             if (alternativas != null) {
//                 alternativas.removeIf(alternativa -> 
//                     alternativa.getTexto_alternativa() == null || 
//                     alternativa.getTexto_alternativa().trim().isEmpty()
//                 );
//             }

//             alternativas.forEach(a -> System.out.println(a.getTexto_alternativa()));
//             q.setAlternativas(alternativas);
//         });

//         model.addAttribute("questoes", questoes);
//         return "professor/banco_questoes";
//     }

//     // Usada em professor/criar_questao.html
//     @PostMapping("salvar")
//         public String criar(@Valid @ModelAttribute Questao questao, Model model, HttpSession session,                 RedirectAttributes ra){
//             try {
//                 Professor professor = (Professor) session.getAttribute("usuarioLogado");
//                 questaoService.criar(questao, professor);
//                 //a area da questao vai ser atribuida automaticamente no @modelAttribute

//                 return "/professor/banco_questoes"; //"form"
//             } catch (RuntimeException e){
//                 System.out.println(e.getMessage());
//                 ra.addFlashAttribute("error", "Houve algum erro na criação do questao!");
//                 return "redirect:/professor/criar_questao";
//             }
//         }


//     @GetMapping("/listar/{id}")
//     public String listarTodasQuestoes(@PathVariable("id") Long id, Model model) {
//         model.addAttribute("listaQuestoes", questaoService.listarQuestoes());
//         return "paginaficticia";
//     }

//     @GetMapping("/buscar/{id}")
//         public String buscarQuestao(@PathVariable("id") Long id_questao, Model model, RedirectAttributes ra) {
//             try{
//                 Questao questao = questaoService.buscarQuestao(id_questao);

//                 model.addAttribute("questao", questao);
//                 model.addAttribute("alternativas", questaoService.listarAlternativas(id_questao));
//                 return "detalhe_questao";
//             } catch(Exception e){
//                 ra.addFlashAttribute("error", e.getMessage());
//                 return "redirect:/professor/banco_questoes";
//             }
//         }
// }


