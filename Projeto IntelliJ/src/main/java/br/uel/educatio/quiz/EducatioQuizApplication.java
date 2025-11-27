package br.uel.educatio.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
        
@SpringBootApplication
public class EducatioQuizApplication {

        public static void main(String[] args) {
                SpringApplication.run(EducatioQuizApplication.class, args);
        }

        @PostConstruct
        public void init() {
                TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
                System.out.println("Fuso horário definido para: " + TimeZone.getDefault().getID());
        }

}
