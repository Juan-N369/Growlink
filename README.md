# GrowLink · Generador de roadmaps de aprendizaje (PTIA, Hito 2)

Sistema que genera un roadmap de aprendizaje de desarrollo backend con un LLM (Claude),
anclado a un catálogo cerrado de habilidades y verificado por un Validador de Salida.

## Estructura

| Carpeta | Contenido |
|---|---|
| `datos/` | Catálogo, esquema JSON de salida, y ejemplos de perfil y roadmap |
| `herramientas/` | Script en Python que verifica la consistencia de los datos |
| `backend/` | Proyecto Spring Boot: Validador de Salida (reglas R1 a R7) y sus pruebas |
| `prototipo/` | Prototipo de interfaz navegable (HTML, CSS y JavaScript) |
| `docs/` | Diagramas de arquitectura y prototipo en papel |

## Cómo ejecutar

Requisitos: JDK 17 o superior, Maven y Python 3.

```
# Verificar datos (opcional: pip install jsonschema)
python herramientas/verificar_datos.py

# Pruebas del Validador
cd backend
mvn test
```

Prototipo: abrir `prototipo/index.html` en el navegador. Los resultados son predeterminados.

## Estado (Hito 2)

- Validador con 7 reglas y 8 pruebas unitarias aprobadas.
- Prototipo navegable con datos simulados.
- Pendiente (Hito 3): integración con la API de Claude, endpoint REST y evaluación con perfiles de prueba.