package com.growlink.roadmap;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ConfiguracionDominio {

    @Bean
    Modelo.Catalogo catalogo(JsonMapper mapper, @Value("${growlink.catalogo.ruta}") String ruta) {
        return mapper.readValue(new File(ruta), Modelo.Catalogo.class);
    }

    @Bean
    Validador validador(Modelo.Catalogo catalogo) {
        return new Validador(catalogo);
    }
}