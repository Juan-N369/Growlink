package com.growlink.roadmap;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Modelo {
    
    private Modelo() {}

    public record Habilidad(String id, String nombre, String nivel, List<String> prerrequisitos) {}

    public record Meta(String id, String nombre,
                   @JsonProperty("habilidades_clave") List<String> habilidadesClave) {}

    public record Perfil(@JsonProperty("nivel_estudios") String nivelEstudios,
                        @JsonProperty("anios_experiencia") String aniosExperiencia,
                        @JsonProperty("habilidades_actuales") List<String> habilidadesActuales,
                        String meta) {}

    public record Paso(int orden,
                    @JsonProperty("habilidad_id") String habilidadId,
                    String justificacion) {}

    public record Resultado(boolean valido, List<String> violaciones) {}

    public record Catalogo(List<Habilidad> habilidades, List<Meta> metas) {
        public Habilidad habilidad(String id) {
            return habilidades.stream().filter(h -> h.id().equals(id)).findFirst().orElse(null);
        }

        public Meta meta(String id) {
            return metas.stream().filter(m -> m.id().equals(id)).findFirst().orElse(null);
        }
    }
}