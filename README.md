# GrowLink · Generador de roadmaps de aprendizaje (PTIA, Hito 3)

Sistema que genera un roadmap de aprendizaje de desarrollo backend con un LLM (Gemini, de Google),
anclado a un catálogo cerrado de habilidades y verificado por un Validador de Salida. Si el
validador rechaza la respuesta, se reintenta con los motivos del rechazo (hasta 3 intentos en total);
si todos fallan, se devuelve un error controlado y nunca un roadmap parcial.

## Estructura

| Carpeta | Contenido |
|---|---|
| `datos/` | Catálogo (20 habilidades, 3 metas), esquema JSON de salida, ejemplos y los 20 perfiles de prueba (`perfiles_prueba/`) |
| `backend/` | Proyecto Spring Boot: Validador (reglas R1 a R7), cliente de Gemini, prompt, orquestador con reintento y registro, y endpoint REST |
| `prototipo/` | Interfaz web (HTML, CSS y JavaScript) conectada al endpoint |
| `herramientas/` | Verificador de datos, script de evaluación, servidor simulado, resumen de casos y aplicación de la rúbrica |
| `resultados/` | Resultados de la evaluación (CSV), respuesta por perfil y log de intentos |
| `docs/` | Diagramas de arquitectura y prototipo en papel |

## Requisitos

JDK 21, Maven, Python 3 y una clave de la API de Gemini (gratuita, desde Google AI Studio).
La clave se pasa por variable de entorno y **nunca debe subirse al repositorio**.

## Cómo ejecutar

**1. Backend** (desde la carpeta `backend/`):

```
# PowerShell
$env:GEMINI_API_KEY="tu_clave"
mvn spring-boot:run

# Linux o macOS
export GEMINI_API_KEY="tu_clave"
mvn spring-boot:run
```

El servidor queda en `http://localhost:8080`. Los modelos se configuran en
`backend/src/main/resources/application.properties` (`gemini.modelo`: lista separada por comas;
si el primero no está disponible se usa el siguiente).

**2. Interfaz:** con el backend encendido, abrir `prototipo/index.html` en el navegador,
elegir una meta y pulsar *Generar roadmap*.

**3. Pruebas** (desde `backend/`):

```
mvn test
```

Las pruebas del Validador y de la carga del catálogo no requieren clave. Las pruebas que llaman a
Gemini (`ClienteGeminiTest`, `OrquestadorTest`) solo se ejecutan si existe `GEMINI_API_KEY` y consumen
cuota de la API.

**4. Evaluación con los 20 perfiles** (con el backend encendido, desde la raíz del repositorio):

```
pip install jsonschema
python herramientas/evaluar.py
```

Genera `resultados/resultados.csv`, una respuesta por perfil en `resultados/respuestas/` y un
resumen en consola; el log de cada intento queda en `resultados/intentos.jsonl`. El script espera
13 segundos entre perfiles por el límite de la capa gratuita. `herramientas/mock_server.py` permite
probar el script sin gastar cuota. Para volver a calcular la rúbrica sobre el CSV se usa
`herramientas/aplicar_rubrica.py`; esta debe ejecutarse **después** de `evaluar.py`, porque este
último regenera el CSV.

## API

`POST http://localhost:8080/api/roadmap` con el perfil en JSON:

```json
{"nivel_estudios": "universitario_en_curso", "anios_experiencia": "menos_1",
 "habilidades_actuales": ["java_basico", "git"], "meta": "backend_microservicios"}
```

| Código | Respuesta |
|---|---|
| 200 | `{"roadmap": [{"orden", "habilidad_id", "justificacion"}], "meta": {"intentos", "valido", "tiempoMs", "reglasFalladas"}}` |
| 400 | Perfil incompleto o meta inexistente en el catálogo (no se llama al modelo) |
| 422 | No se obtuvo un roadmap válido tras 3 intentos: `{"error", "intentos", "reglasFalladas"}` |
| 502 | El servicio de IA no respondió |

## Resultados de la evaluación (20 perfiles)

- 100 % de roadmaps válidos y 100 % cumplen el esquema; ningún error controlado.
- 70 % válidos al primer intento; el 30 % restante fue rechazado por la regla R6 y corregido en el segundo intento.
- 0 habilidades fuera del catálogo en las 26 respuestas del modelo.
- Tiempo promedio de 4,1 s (máximo 17,8 s).
- Rúbrica cualitativa: promedio de 4,41 sobre 5.

El análisis completo está en el documento del proyecto.

## Uso de IA

El producto usa un modelo de lenguaje de Google (Gemini) para generar los roadmaps. Durante el
desarrollo se usaron herramientas de IA generativa como apoyo: Claude (Anthropic, 2026) para el
código del backend, el prototipo de interfaz, los scripts de evaluación, y una herramienta de IA. 
El equipo definió el diseño y el alcance, desarrollo y reviso el código y los textos, ejecutó las
pruebas y la evaluación, y adaptó y verificó el resultado.

## Estado (Hito 3)

- Validador con 7 reglas y 8 pruebas unitarias aprobadas.
- Integración con la API de Gemini, endpoint REST, reintento con retroalimentación y log de intentos.
- Prototipo conectado al backend (MVP funcional).
- Evaluación con 20 perfiles de prueba completada.
- Fuera de alcance: entrada en texto libre, actualización incremental del roadmap y persistencia del progreso.