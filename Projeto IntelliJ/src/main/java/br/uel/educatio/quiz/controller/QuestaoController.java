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
import br.uel.educatio.quiz.service.QuizService;
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

    @Autowired
    private QuizService quizService;


    @GetMapping({"/novo", "/editar/{id_questao}"})
    public String mostrarFormularioQuestao(@PathVariable(required = false) Long id_questao, Model model, HttpSession session) {
        Questao questao;

        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();
        List<Area> areas = profService.buscarAreasDoProfessor(id_prof);
        List<Alternativa> alternativas;

        // Modo Edição
        if (id_questao != null) {
            questao = questaoService.buscarQuestao(id_questao); 
            alternativas = questaoService.listarAlternativas(questao.getId_questao());

                // Filtra alternativas vazias (exceto para V/F que precisa das 2)
            if (questao.getTipo_questao() != TipoQuestao.VERDADEIRO_OU_FALSO) {
                    alternativas.removeIf(alternativa -> 
                        alternativa.getTexto_alternativa() == null || 
                        alternativa.getTexto_alternativa().trim().isEmpty()
                    );
                }

            alternativas.forEach(a -> System.out.println(a.getTexto_alternativa()));
            alternativas.forEach(a -> System.out.println(a.getFlg_eh_correta()));
            questao.setAlternativas(alternativas);

            
        }
        //Modo Criação
        else { 
            questao = new Questao();
            alternativas = new ArrayList<>();

            // Para V/F, inicializa com 2 alternativas
            // Mas deixa vazio para outros tipos
            questao.setAlternativas(alternativas);
        }

        model.addAttribute("questao", questao);
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("escolaridades", Escolaridade.values());
        model.addAttribute("dificuldades", Dificuldade.values());
        model.addAttribute("exibicoes", Exibicao.values());
        model.addAttribute("areasProfessor", areas);
        model.addAttribute("professor", professor);

        return "professor/editar_questao";
    }

    
    // Usada em professor/criar_questao.html
    @PostMapping({"/salvar", "/atualizar"})
    public String salvar_questao(@Valid @ModelAttribute Questao questao, Model model, HttpSession session, RedirectAttributes ra){
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();


        if (questao.getTipo_questao() == TipoQuestao.VERDADEIRO_OU_FALSO) {
            
            // Garante que sempre há 2 alternativas para V/F
            if (questao.getAlternativas() == null || questao.getAlternativas().isEmpty()) {
                List<Alternativa> altVF = new ArrayList<>();

                Alternativa verdadeiro = new Alternativa();
                verdadeiro.setTexto_alternativa("Verdadeiro");
                verdadeiro.setNum_alternativa(1L);
                verdadeiro.setFlg_eh_correta('N'); 

                Alternativa falso = new Alternativa();
                falso.setTexto_alternativa("Falso");
                falso.setNum_alternativa(2L);
                falso.setFlg_eh_correta('N'); 

                altVF.add(verdadeiro);
                altVF.add(falso);
                questao.setAlternativas(altVF);
            }

            // Garante que texto está correto
            if (questao.getAlternativas().size() >= 2) {
                questao.getAlternativas().get(0).setTexto_alternativa("Verdadeiro");
                questao.getAlternativas().get(1).setTexto_alternativa("Falso");
            }
            
        } else {
            if (questao.getAlternativas() != null) {
                questao.getAlternativas().removeIf(alt -> 
                    alt == null ||
                    alt.getTexto_alternativa() == null || 
                    alt.getTexto_alternativa().trim().isEmpty() ||
                    alt.getTexto_alternativa().trim().equals(",")
                );
            }
        }

        System.out.println("Tipo: " + questao.getTipo_questao());
        System.out.println("Alternativas após filtragem: " + questao.getAlternativas().size());
        questao.getAlternativas().forEach(alt -> 
            System.out.println("Texto: [" + alt.getTexto_alternativa() + "] - Correta: " + alt.getFlg_eh_correta())
        );

        ra.addAttribute("id_prof", id_prof);

        try {
            questaoService.salvar(questao, id_prof);
            ra.addFlashAttribute("success", "Questão salva com sucesso!");
            System.out.println("Questão salva com sucesso!");
        } catch (RuntimeException e){
            System.out.println(e.getMessage());
            ra.addFlashAttribute("error", "Erro ao salvar: " + e.getMessage());
        }

        return "redirect:/questao/listar/questoes?id_prof=" + id_prof;
    }
    
    
    // Usada em professor/home.html e em criar_quiz.html 
    @GetMapping("/listar/questoes")
    public String listarQuestoesPorProfessor(
            @RequestParam(value = "id_prof") Long id_prof,
            @RequestParam(value = "quizId", required = false) Long quizId,
            @RequestParam(value = "modoBusca", required = false, defaultValue = "MEUS") String modoBusca,
            @RequestParam(value = "filtroTipo", required = false) String filtroTipo, 
            @RequestParam(value = "termoBusca", required = false) String termoBusca, 
            Model model
    ) {
        List<Questao> questoes;

        if (quizId != null) {
            // Chama o serviço atualizado passando o filtroTipo
            questoes = quizService.buscarQuestaoParaQuiz(id_prof, quizId, modoBusca, filtroTipo, termoBusca);
            questoes.forEach(q -> System.out.println(q.getEnunciado()));
        } else {
            // Fluxo normal
            questoes = questaoService.buscarQuestoesPorFiltro(id_prof, filtroTipo, termoBusca);
        }

        model.addAttribute("questoes", questoes);
        model.addAttribute("quizId", quizId);

        // Mantém os filtros selecionados na tela
        model.addAttribute("modoBuscaSelecionado", modoBusca);
        model.addAttribute("filtroTipoSelecionado", filtroTipo);
        model.addAttribute("termoBuscaDigitado", termoBusca);
        model.addAttribute("tiposQuestao", TipoQuestao.values());

        return "professor/banco_questoes";
    }

    // Usada pelo botão 'Detalhes' no banco_questoes.html
    @GetMapping("/buscar")
    public String buscarQuestao(@RequestParam("id_questao") Long id_questao,
                                @RequestParam(value = "quizId", required = false) Long quizId,
                                Model model, RedirectAttributes ra) {
        try{
            System.out.println("QuizId: " + quizId);
            System.out.println("QuestãoId: " + id_questao);
            Questao questao = questaoService.buscarQuestao(id_questao);
            Professor profCriador = profService.buscarPorId(questao.getProfessor_criador());
            Area questaoArea = areaService.buscarPorId(questao.getArea());
            
            // Carrega ENUMs para os Dropdowns da view de detalhes
            model.addAttribute("escolaridades", Escolaridade.values());
            model.addAttribute("dificuldades", Dificuldade.values());
            model.addAttribute("tiposQuestao", TipoQuestao.values());
            model.addAttribute("exibicoes", Exibicao.values());
            
            model.addAttribute("questao", questao);
            model.addAttribute("profCriador", profCriador);
            model.addAttribute("questaoAreaNome", questaoArea.getNomeArea());
            model.addAttribute("alternativas", questaoService.listarAlternativas(id_questao));
            model.addAttribute("quizId", quizId);

            return "detalhe_questao"; 
        } catch(Exception e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/professor/banco_questoes";
        }
    }

    // Usado pelo formulário no banco_de_questoes.html)
    @PostMapping("/excluir")
    public String excluirQuestao(@RequestParam("id_questao") Long id_questao, RedirectAttributes ra, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("usuarioLogado");
        Long id_prof = professor.getId_professor();

        System.out.println("Questão ID: " + id_questao);
        try {
            questaoService.deletarQuestaoDoBanco(id_questao); 
            ra.addFlashAttribute("success", "Questão excluída com sucesso!");
            System.out.println("Questão excluída com sucesso!");
            
        } catch(Exception e) {
            ra.addFlashAttribute("error", "Erro ao excluir questão: " + e.getMessage());
            System.out.println(e.getMessage());
        }
        
        ra.addFlashAttribute("id_prof", id_prof );
        return "redirect:/questao/listar/questoes?id_prof=" + id_prof;
    }


    @GetMapping("/listarTodasQuestoes")
    public String listarTodasQuestoes(
            @RequestParam(value = "area", required = false) Long area,
            @RequestParam(value = "dificuldade", required = false) String dificuldade,
            @RequestParam(value = "filtroTipo", required = false) String filtroTipo, 
            @RequestParam(value = "termoBusca", required = false) String termoBusca,
            Model model) {
        
        List<Questao> questoes = questaoService.listarTodasQuestoesComFiltro(
            area, 
            dificuldade,
            filtroTipo, 
            termoBusca
        );
    
        model.addAttribute("questoes", questoes);
        model.addAttribute("tiposQuestao", TipoQuestao.values());
        model.addAttribute("dificuldades", Dificuldade.values()); 

        // Mantém os filtros selecionados
        model.addAttribute("filtroTipoSelecionado", filtroTipo);
        model.addAttribute("areaFiltroSelecionada", area);
        model.addAttribute("dificuldadeFiltroSelecionada", dificuldade); 
        model.addAttribute("termoBuscaDigitado", termoBusca);
        return "professor/banco_questoes_geral";
    }

}



