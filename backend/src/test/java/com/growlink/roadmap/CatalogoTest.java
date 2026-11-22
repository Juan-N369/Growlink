package com.growlink.roadmap;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CatalogoTest {

    @Autowired Modelo.Catalogo catalogo;

    @Test
    void cargaCatalogoYMetas() {
        assertNotNull(catalogo.habilidad("java_basico"));
        assertFalse(catalogo.habilidad("poo").prerrequisitos().isEmpty());
        assertFalse(catalogo.meta("backend_junior").habilidadesClave().isEmpty());
    }
}