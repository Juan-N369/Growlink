package com.growlink.roadmap;

import com.growlink.roadmap.Modelo.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Validador {

    private final Catalogo catalogo;

    public Validador(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    public Resultado validar(List<Paso> roadmap, Perfil perfil) {
        List<String> violaciones = new ArrayList<>();

        // R1: no vacío
        if (roadmap == null || roadmap.isEmpty()) {
            return new Resultado(false, List.of("R1: el roadmap está vacío."));
        }

        Set<String> dominadas = new HashSet<>(perfil.habilidadesActuales());
        Set<String> vistas = new HashSet<>(); // habilidades ya aparecidas en pasos anteriores

        for (int i = 0; i < roadmap.size(); i++) {
            Paso paso = roadmap.get(i);
            String id = paso.habilidadId();

            // R5: orden consecutivo
            if (paso.orden() != i + 1) {
                violaciones.add("R5: el paso en la posición " + (i + 1)
                        + " tiene orden " + paso.orden() + " (debe ser " + (i + 1) + ").");
            }

            // R2: la habilidad existe en el catálogo
            Habilidad habilidad = catalogo.habilidad(id);
            if (habilidad == null) {
                violaciones.add("R2: la habilidad '" + id + "' no existe en el catálogo.");
                continue; // sin habilidad no se pueden evaluar las demás reglas del paso
            }

            // R3: sin repetidas
            if (vistas.contains(id)) {
                violaciones.add("R3: la habilidad '" + id + "' está repetida.");
            }

            // R4: no redundante con el perfil
            if (dominadas.contains(id)) {
                violaciones.add("R4: la habilidad '" + id + "' ya la domina el usuario.");
            }

            // R6: prerrequisitos cubiertos antes
            for (String pre : habilidad.prerrequisitos()) {
                if (!dominadas.contains(pre) && !vistas.contains(pre)) {
                    violaciones.add("R6: '" + id + "' requiere '" + pre
                            + "' antes, y no está en el perfil ni en pasos anteriores.");
                }
            }
            vistas.add(id);
        }

        // R7: cobertura de la meta
        Meta meta = catalogo.meta(perfil.meta());
        if (meta == null) {
            violaciones.add("R7: la meta '" + perfil.meta() + "' no existe en el catálogo.");
        } else {
            for (String clave : meta.habilidadesClave()) {
                if (!dominadas.contains(clave) && !vistas.contains(clave)) {
                    violaciones.add("R7: falta la habilidad clave '" + clave
                            + "' de la meta '" + meta.id() + "'.");
                }
            }
        }

        return new Resultado(violaciones.isEmpty(), violaciones);
    }
}