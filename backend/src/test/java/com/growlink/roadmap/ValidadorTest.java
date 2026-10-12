package com.growlink.roadmap;

import com.growlink.roadmap.Modelo.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorTest {

    private final Catalogo catalogo = new Catalogo(
        List.of(
            new Habilidad("java_basico", "Java básico", "basico", List.of()),
            new Habilidad("poo", "POO", "basico", List.of("java_basico")),
            new Habilidad("git", "Git", "basico", List.of()),
            new Habilidad("http_rest_conceptos", "HTTP y REST", "basico", List.of()),
            new Habilidad("linux_cli", "Linux CLI", "basico", List.of()),
            new Habilidad("spring_boot", "Spring Boot", "intermedio", List.of("java_basico", "poo")),
            new Habilidad("apis_rest", "APIs REST", "intermedio", List.of("spring_boot", "http_rest_conceptos")),
            new Habilidad("docker", "Docker", "intermedio", List.of("linux_cli")),
            new Habilidad("microservicios", "Microservicios", "avanzado", List.of("apis_rest", "docker")),
            new Habilidad("mensajeria", "Mensajería", "avanzado", List.of("microservicios"))
        ),
        List.of(new Meta("backend_microservicios", "Backend con microservicios",
            List.of("spring_boot", "apis_rest", "docker", "microservicios", "mensajeria")))
    );

    private final Validador validador = new Validador(catalogo);

    private final Perfil perfil = new Perfil("universitario_en_curso", "menos_1",
            List.of("java_basico", "git"), "backend_microservicios");

    private Paso p(int orden, String id) {
        return new Paso(orden, id, "justificación");
    }

    private List<Paso> correcto() {
        return List.of(p(1, "poo"), p(2, "spring_boot"), p(3, "http_rest_conceptos"),
                p(4, "apis_rest"), p(5, "linux_cli"), p(6, "docker"),
                p(7, "microservicios"), p(8, "mensajeria"));
    }

    private boolean hay(Resultado r, String regla) {
        return r.violaciones().stream().anyMatch(v -> v.startsWith(regla));
    }

    @Test
    void roadmapCorrectoEsValido() {
        Resultado r = validador.validar(correcto(), perfil);
        assertTrue(r.valido(), "Violaciones: " + r.violaciones());
    }

    @Test
    void roadmapVacioViolaR1() {
        assertTrue(hay(validador.validar(List.of(), perfil), "R1"));
    }

    @Test
    void habilidadInventadaViolaR2() {
        List<Paso> r = List.of(p(1, "poo"), p(2, "spring_boot"), p(3, "http_rest_conceptos"),
                p(4, "apis_rest"), p(5, "linux_cli"), p(6, "docker"),
                p(7, "microservicios"), p(8, "blockchain_avanzado"));
        assertTrue(hay(validador.validar(r, perfil), "R2"));
    }

    @Test
    void habilidadRepetidaViolaR3() {
        List<Paso> r = new java.util.ArrayList<>(correcto());
        r.add(p(9, "docker"));
        assertTrue(hay(validador.validar(r, perfil), "R3"));
    }

    @Test
    void habilidadYaDominadaViolaR4() {
        List<Paso> r = new java.util.ArrayList<>(correcto());
        r.add(p(9, "git"));
        assertTrue(hay(validador.validar(r, perfil), "R4"));
    }

    @Test
    void ordenMalNumeradoViolaR5() {
        List<Paso> r = List.of(p(1, "poo"), p(5, "spring_boot"), p(3, "http_rest_conceptos"),
                p(4, "apis_rest"), p(5, "linux_cli"), p(6, "docker"),
                p(7, "microservicios"), p(8, "mensajeria"));
        assertTrue(hay(validador.validar(r, perfil), "R5"));
    }

    @Test
    void prerrequisitoFueraDeOrdenViolaR6() {
        List<Paso> r = List.of(p(1, "spring_boot"), p(2, "poo"), p(3, "http_rest_conceptos"),
                p(4, "apis_rest"), p(5, "linux_cli"), p(6, "docker"),
                p(7, "microservicios"), p(8, "mensajeria"));
        assertTrue(hay(validador.validar(r, perfil), "R6"));
    }

    @Test
    void metaNoCubiertaViolaR7() {
        List<Paso> r = List.of(p(1, "poo"), p(2, "spring_boot"), p(3, "http_rest_conceptos"),
                p(4, "apis_rest"), p(5, "linux_cli"), p(6, "docker"), p(7, "microservicios"));
        assertTrue(hay(validador.validar(r, perfil), "R7"));
    }
}