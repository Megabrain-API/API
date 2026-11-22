package org.example.controllers.profControllers;

import org.example.entity.profEntity.Questao;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class AutoriaQuestao {
    private AutoriaQuestao() {
    }

    static final class Filtro {
        private final Long matricula;
        private final String rotulo;

        private Filtro(Long matricula, String rotulo) {
            this.matricula = matricula;
            this.rotulo = rotulo;
        }

        boolean aceita(Questao questao) {
            return matricula == null || Objects.equals(matricula, questao.getMatriculaAutor());
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    static List<Filtro> filtros(List<Questao> questoes, Long matriculaLogada) {
        List<Filtro> opcoes = new ArrayList<>();
        opcoes.add(new Filtro(null, "Todos os professores"));
        if (matriculaLogada != null) {
            opcoes.add(new Filtro(matriculaLogada, "Minhas questões"));
        }

        Map<Long, String> autores = new TreeMap<>();
        for (Questao questao : questoes) {
            Long matricula = questao.getMatriculaAutor();
            String nome = questao.getNomeAutor();
            if (matricula != null && nome != null && !nome.isBlank()) {
                autores.putIfAbsent(matricula, nome.trim());
            }
        }
        Collator ordem = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        autores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(ordem))
                .map(autor -> new Filtro(autor.getKey(), autor.getValue()))
                .forEach(opcoes::add);
        return opcoes;
    }

    static String formatar(Questao questao) {
        String nome = questao.getNomeAutor();
        return nome == null || nome.isBlank() ? "Autor não registrado" : "Criado por " + nome;
    }
}
