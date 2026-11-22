package com.growlink.roadmap;

import static org.junit.jupiter.api.Assertions.*;

import com.growlink.roadmap.ClienteGemini.Mensaje;
import com.growlink.roadmap.Modelo.*;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class ClienteGeminiTest {

    @Autowired ClienteGemini cliente;
    @Autowired PromptRoadmap prompt;
    @Autowired JsonMapper mapper;

    @Test
    void generaRoadmapParseable() {
        Perfil p = mapper.readValue(new File("../datos/perfil_ejemplo.json"), Perfil.class);
        String texto = cliente.completar(prompt.sistema(),
                List.of(new Mensaje("user", prompt.usuario(p))));
        System.out.println(texto);
        assertFalse(prompt.parsear(texto).isEmpty());
    }
}