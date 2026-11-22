package com.growlink.roadmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.growlink.roadmap.Modelo.*;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PromptRoadmap {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Salida(List<Paso> roadmap) {}

    private final JsonMapper mapper;
    private final Catalogo catalogo;

    public PromptRoadmap(JsonMapper mapper, Catalogo catalogo) {
        this.mapper = mapper;
        this.catalogo = catalogo;
    }

    public String sistema() {
        return """
            Eres un planificador de rutas de aprendizaje de desarrollo backend.
            Genera un roadmap usando SOLO habilidades del catálogo.

            CATÁLOGO (JSON):
            %s

            REGLAS (todas obligatorias):
            R1: el roadmap no puede estar vacío.
            R2: usa únicamente habilidad_id que existan en el catálogo.
            R3: no repitas habilidades.
            R4: no incluyas habilidades que el usuario ya domina (habilidades_actuales).
            R5: el campo orden es consecutivo, empezando en 1.
            R6: cada habilidad debe tener todos sus prerrequisitos en habilidades_actuales o en pasos anteriores.
            R7: incluye todas las habilidades_clave de la meta que el usuario aún no domine.

            FORMATO: responde únicamente con JSON, sin texto adicional ni markdown:
            {"roadmap":[{"orden":1,"habilidad_id":"...","justificacion":"..."}]}
            La justificación es una frase breve, adaptada al perfil, que explica por qué va en ese paso.
            """.formatted(mapper.writeValueAsString(catalogo));
    }

    public String usuario(Perfil perfil) {
        return "Perfil del usuario (JSON): " + mapper.writeValueAsString(perfil)
                + "\nGenera el roadmap.";
    }

    public List<Paso> parsear(String texto) {
        int i = texto.indexOf('{'), j = texto.lastIndexOf('}');
        if (i < 0 || j < i) throw new IllegalArgumentException("La respuesta no contiene JSON");
        return mapper.readValue(texto.substring(i, j + 1), Salida.class).roadmap();
    }
}