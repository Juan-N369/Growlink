# Rúbrica cualitativa de evaluación de roadmaps

Cada roadmap generado se puntúa manualmente (ambos integrantes) con cuatro criterios en una escala de 1 a 5. El valor de la columna `rubrica` de `resultados/resultados.csv` es el **promedio de los cuatro criterios** (un decimal). Los perfiles con error controlado no tienen roadmap y se dejan sin puntuar. El objetivo del proyecto es un promedio global ≥ 4.

Para puntuar, se compara el roadmap con el perfil (`datos/perfiles_prueba/pXX.json`) y con el catálogo (`datos/catalogo.json`).

| Criterio | 1 (deficiente) | 3 (aceptable) | 5 (excelente) |
|---|---|---|---|
| **1. Coherencia del orden pedagógico** | El orden es arbitrario: habilidades avanzadas aparecen antes de sus fundamentos y el recorrido no se puede seguir. | El orden respeta los prerrequisitos, pero presenta saltos de dificultad o agrupa mal temas afines (p. ej., intercala temas básicos y avanzados sin razón). | Progresión gradual de básico a avanzado, con prerrequisitos siempre previos y temas afines agrupados de forma natural. |
| **2. Pertinencia de las justificaciones** | Las justificaciones son genéricas, repetidas o incorrectas, y podrían aplicarse a cualquier paso. | Las justificaciones son correctas pero genéricas: explican la habilidad, no por qué importa para este perfil y esta meta. | Cada justificación es específica, correcta y vincula el paso con el perfil del usuario o con el paso siguiente. |
| **3. Adecuación al nivel del perfil** | Ignora el perfil: repite lo que la persona ya domina o exige saltos que su nivel no permite. | Evita lo ya dominado, pero el ritmo o la profundidad no se ajustan del todo (demasiado básico o demasiado denso para el perfil). | El punto de partida es exacto: omite lo ya dominado, empieza en el primer paso realmente necesario y el ritmo corresponde al nivel de estudios y experiencia. |
| **4. Cobertura de la meta** | Omite varias habilidades clave de la meta; seguir el roadmap no llevaría a ella. | Cubre las habilidades clave, pero incluye pasos innecesarios o deja la meta sin un cierre claro. | Cubre todas las habilidades clave pendientes, sin pasos superfluos, y el último paso culmina de forma natural en la meta. |

Los valores 2 y 4 se usan cuando el roadmap queda entre dos de los niveles descritos.

## Procedimiento

1. Ejecutar `python herramientas/evaluar.py` con el backend real.
2. Puntuar cada roadmap de `resultados/respuestas/pXX.json` en los cuatro criterios.
3. Registrar el promedio en la columna `rubrica` del CSV (el script conserva ese valor si se vuelve a ejecutar).

## Hoja de registro

| Perfil | Coherencia | Justificaciones | Adecuación | Cobertura | Promedio |
|---|---|---|---|---|---|
| p01 | 4 | 3 | 5 | 5 | 4.25 |
| p02 | 4 | 4 | 5 | 5 | 4.50 |
| p03 | 3 | 4 | 3 | 5 | 3.75 |
| p04 | 3 | 4 | 5 | 5 | 4.25 |
| p05 | 4 | 3 | 5 | 5 | 4.25 |
| p06 | 4 | 3 | 4 | 4 | 3.75 |
| p07 | 4 | 3 | 5 | 5 | 4.25 |
| p08 | 5 | 3 | 5 | 5 | 4.50 |
| p09 | 5 | 4 | 5 | 5 | 4.75 |
| p10 | 4 | 3 | 5 | 5 | 4.25 |
| p11 | 5 | 4 | 5 | 5 | 4.75 |
| p12 | 5 | 5 | 5 | 5 | 5.00 |
| p13 | 4 | 4 | 5 | 5 | 4.50 |
| p14 | 5 | 5 | 5 | 5 | 5.00 |
| p15 | 4 | 3 | 4 | 5 | 4.00 |
| p16 | 4 | 3 | 5 | 5 | 4.25 |
| p17 | 5 | 4 | 5 | 5 | 4.75 |
| p18 | 4 | 4 | 5 | 5 | 4.50 |
| p19 | 4 | 4 | 5 | 5 | 4.50 |
| p20 | 5 | 3 | 5 | 5 | 4.50 |
Promedio por criterio: [4.25, 3.65, 4.8, 4.95]
Promedio global: 4.41