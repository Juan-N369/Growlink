package com.growlink.roadmap;

import static org.junit.jupiter.api.Assertions.*;

import com.growlink.roadmap.Modelo.Perfil;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class OrquestadorTest {

    @Autowired OrquestadorRoadmap orquestador;
    @Autowired JsonMapper mapper;

    @Test
    void generaYRegistraIntentos() {
        Perfil p = mapper.readValue(new File("../datos/perfil_ejemplo.json"), Perfil.class);
        var g = orquestador.generar(p);
        System.out.println(g);
        assertTrue(g.intentos() >= 1);
        assertTrue(new File("../resultados/intentos.jsonl").exists());
    }
}