package com.growlink.roadmap;

import com.growlink.roadmap.Modelo.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RoadmapController {

    public record MetaRespuesta(int intentos, boolean valido, long tiempoMs, List<String> reglasFalladas) {}

    public record Exito(List<Paso> roadmap, MetaRespuesta meta) {}

    public record ErrorRespuesta(String error, int intentos, List<String> reglasFalladas) {}

    private final OrquestadorRoadmap orquestador;
    private final Catalogo catalogo;

    public RoadmapController(OrquestadorRoadmap orquestador, Catalogo catalogo) {
        this.orquestador = orquestador;
        this.catalogo = catalogo;
    }

    @PostMapping("/roadmap")
    public ResponseEntity<?> generar(@RequestBody Perfil perfil) {
        if (perfil == null || perfil.habilidadesActuales() == null || catalogo.meta(perfil.meta()) == null) {
            return ResponseEntity.badRequest().body(new ErrorRespuesta(
                    "Perfil inválido: faltan campos o la meta no existe en el catálogo", 0, List.of()));
        }
        var g = orquestador.generar(perfil);
        if (g.valido()) {
            return ResponseEntity.ok(new Exito(g.roadmap(),
                    new MetaRespuesta(g.intentos(), true, g.tiempoMs(), g.reglasFalladas())));
        }
        int status = g.fallaApi() ? 502 : 422;
        String msg = g.fallaApi()
                ? "El servicio de IA no respondió"
                : "No se obtuvo un roadmap válido tras " + g.intentos() + " intentos";
        return ResponseEntity.status(status)
                .body(new ErrorRespuesta(msg, g.intentos(), g.reglasFalladas()));
    }
}