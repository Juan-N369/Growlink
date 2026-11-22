package com.growlink.roadmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
public class ClienteGemini {

    /** role: "user" o "model" */
    public record Mensaje(String role, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parte(String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contenido(List<Parte> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidato(Contenido content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(List<Candidato> candidates) {}

    private final RestClient http;
    private final String modelo;

    public ClienteGemini(@Value("${gemini.api-key}") String apiKey,
                         @Value("${gemini.modelo}") String modelo) {
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
        this.modelo = modelo;
    }

    public String completar(String sistema, List<Mensaje> mensajes) {
        List<Map<String, Object>> contents = mensajes.stream()
                .map(m -> Map.<String, Object>of("role", m.role(),
                        "parts", List.of(Map.of("text", m.content()))))
                .toList();
        Map<String, Object> cuerpo = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", sistema))),
                "contents", contents,
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "maxOutputTokens", 8192));
        Respuesta r = llamar(cuerpo);
        if (r == null || r.candidates() == null || r.candidates().isEmpty()
                || r.candidates().get(0).content() == null) {
            throw new IllegalStateException("Respuesta vacía de Gemini");
        }
        return r.candidates().get(0).content().parts().stream()
                .map(Parte::text)
                .collect(Collectors.joining());
    }

    private Respuesta llamar(Map<String, Object> cuerpo) {
    for (int intento = 1; ; intento++) {
        try {
            return http.post()
                    .uri("/v1beta/models/{modelo}:generateContent", modelo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Respuesta.class);
        } catch (HttpServerErrorException | HttpClientErrorException.TooManyRequests e) {
            if (intento == 4) throw e;
            try {
                Thread.sleep(3000L * intento);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    }
}
}