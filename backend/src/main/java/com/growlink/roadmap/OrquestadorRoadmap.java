package com.growlink.roadmap;

import com.growlink.roadmap.ClienteGemini.Mensaje;
import com.growlink.roadmap.Modelo.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrquestadorRoadmap {

    static final int MAX_INTENTOS = 3;
    private static final Logger LOG = LoggerFactory.getLogger(OrquestadorRoadmap.class);

    public record Intento(int numero, String respuestaCruda, List<String> violaciones, String errorApi) {}

    public record Generacion(List<Paso> roadmap, boolean valido, int intentos, long tiempoMs,
                             List<String> reglasFalladas, boolean fallaApi, List<Intento> detalle) {}

    private final ClienteGemini cliente;
    private final PromptRoadmap prompt;
    private final Validador validador;
    private final JsonMapper mapper;
    private final String rutaLog;

    public OrquestadorRoadmap(ClienteGemini cliente, PromptRoadmap prompt, Validador validador,
                              JsonMapper mapper, @Value("${growlink.log.ruta}") String rutaLog) {
        this.cliente = cliente;
        this.prompt = prompt;
        this.validador = validador;
        this.mapper = mapper;
        this.rutaLog = rutaLog;
    }

    public Generacion generar(Perfil perfil) {
        long t0 = System.nanoTime();
        String sistema = prompt.sistema();
        List<Mensaje> conv = new ArrayList<>(List.of(new Mensaje("user", prompt.usuario(perfil))));
        List<Intento> detalle = new ArrayList<>();
        Set<String> reglas = new LinkedHashSet<>();
        List<Paso> roadmap = null;
        boolean valido = false;
        boolean fallaApi = false;

        for (int n = 1; n <= MAX_INTENTOS && !valido && !fallaApi; n++) {
            String texto;
            try {
                texto = cliente.completar(sistema, conv);
            } catch (RuntimeException e) {
                detalle.add(new Intento(n, null, List.of(), e.getMessage()));
                fallaApi = true;
                break;
            }
            List<String> violaciones;
            try {
                roadmap = prompt.parsear(texto);
                Resultado r = validador.validar(roadmap, perfil);
                valido = r.valido();
                violaciones = r.violaciones();
            } catch (RuntimeException e) {
                roadmap = null;
                violaciones = List.of("FORMATO: la respuesta no es un JSON válido con el campo roadmap.");
            }
            detalle.add(new Intento(n, texto, violaciones, null));
            for (String v : violaciones) reglas.add(v.split(":")[0]);
            if (!valido) {
                conv.add(new Mensaje("model", texto));
                conv.add(new Mensaje("user", prompt.correccion(violaciones)));
            }
        }

        long ms = (System.nanoTime() - t0) / 1_000_000;
        Generacion g = new Generacion(valido ? roadmap : null, valido, detalle.size(), ms,
                List.copyOf(reglas), fallaApi, detalle);
        registrar(perfil, g);
        return g;
    }

    private void registrar(Perfil perfil, Generacion g) {
        LOG.info("meta={} intentos={} valido={} ms={} reglas={}",
                perfil.meta(), g.intentos(), g.valido(), g.tiempoMs(), g.reglasFalladas());
        try {
            Path p = Path.of(rutaLog);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            String linea = mapper.writeValueAsString(Map.of("perfil", perfil, "generacion", g));
            Files.writeString(p, linea + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException e) {
            LOG.warn("No se pudo escribir el log de intentos: {}", e.getMessage());
        }
    }
}