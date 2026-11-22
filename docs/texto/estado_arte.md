# Trabajos relacionados (estado del arte)

Este apartado revisa seis antecedentes indexados, dos por cada uno de los tres problemas planteados en la introducción: (1) generación de rutas de aprendizaje personalizadas, (2) confiabilidad y validez de las salidas de un modelo de lenguaje y (3) actualización incremental de una ruta ya generada. Para cada trabajo se describe el problema, el método (cómo), las métricas reportadas (para qué) y su relación con GrowLink. Todas las cifras fueron contrastadas con el texto original de cada artículo.

## Problema 1. Generación de rutas de aprendizaje personalizadas

### 1.1 Ng y Fung (2024)

**Problema.** Los sistemas tradicionales de planificación de rutas de aprendizaje personalizadas carecen de adaptabilidad, interactividad y transparencia.

**Cómo.** Los autores diseñan tres tipos de instrucciones (*prompts*) para los modelos LLaMA-2-70B y GPT-4: una evaluación inicial que solicita los tres conceptos siguientes que el estudiante debe aprender, una instrucción de aclaración en diálogo de varios turnos y una instrucción explicativa que pide justificar el orden recomendado. Se compara contra un baseline con instrucciones sin personalizar.

**Para qué (métricas reportadas).** Con GPT-4, la exactitud pasa de 75,8 % a 88,3 %, la satisfacción de usuario (escala 1–5) de 3,7 a 4,4 y la calidad de las rutas (escala 1–5) de 3,9 a 4,7. Con LLaMA-2-70B, la exactitud pasa de 72,4 % a 85,6 %, la satisfacción de 3,5 a 4,2 y la calidad de 3,7 a 4,5. En un seguimiento de tres meses, la mejora en las calificaciones fue de 12,3 % para el baseline con LLaMA-2-70B, 18,7 % para el método propuesto con LLaMA-2-70B y 21,4 % con GPT-4.

**Relación con GrowLink.** Respalda el uso de un LLM como generador de rutas condicionado por el perfil y la exigencia de justificar cada paso, como hace el campo `justificacion` del contrato de GrowLink. **Limitación:** el trabajo es un preprint de arXiv sin revisión por pares, no reporta el tamaño de su conjunto de datos y parte de la evaluación cualitativa la realiza GPT-4; por ello sus cifras se toman como indicativas y no como evidencia concluyente.

### 1.2 Liu et al. (2026) — PersonaPath

**Problema.** Los benchmarks existentes de recomendación educativa se centran en ejercicios y no evalúan que un modelo construya una ruta orientada a una meta respetando prerrequisitos y el estado de dominio del estudiante.

**Cómo.** Los autores construyen un benchmark con 2 000 perfiles sintéticos de estudiantes y un grafo jerárquico de conocimiento de 347 libros de texto, 1 751 unidades y 4 092 conceptos, conectados por relaciones de prerrequisito. Dado un perfil y una unidad meta, el modelo selecciona conceptos paso a paso, y un simulador basado en teoría de respuesta al ítem actualiza el dominio del estudiante tras cada selección (protocolo de ciclo cerrado). Se evalúan diez LLM abiertos con y sin cadena de pensamiento, y una variante con recuperación (RAG).

**Para qué (métricas reportadas).** Se miden validez (violaciones de prerrequisitos y alucinaciones de entidades inexistentes), adaptatividad y eficiencia. El mejor modelo (DeepSeek-V3.1) alcanza 90,9 % en validez, pero 44,3 % en adaptatividad y 29,5 % de aprobación final en educación básica (14,6 % en educación superior). Ningún modelo supera 44,7 % en adaptatividad. Al omitir la información de dominio del perfil, la adaptatividad de Qwen3-30B-A3B con cadena de pensamiento cae 26,1 puntos porcentuales. Con RAG, la validez de Qwen3-4B pasa de 39,7 % a 73,8 %.

**Relación con GrowLink.** Mide exactamente las propiedades que GrowLink verifica con sus reglas: ausencia de entidades fuera del conjunto permitido (R2) y prerrequisitos cubiertos antes de cada paso (R6). Evidencia que los LLM razonan razonablemente bien sobre la estructura, pero fallan en adaptarse al individuo, lo que justifica tanto el perfil estructurado como la validación posterior de la salida.

## Problema 2. Confiabilidad y validez de las salidas del modelo

### 2.1 Ayala y Béchard (2024)

**Problema.** Al generar flujos de trabajo en formato JSON a partir de lenguaje natural, el modelo inventa pasos y tablas inexistentes, lo que hace inejecutable la salida.

**Cómo.** Los autores entrenan un recuperador (*retriever*) a partir de all-mpnet-base-v2 (110 millones de parámetros) con pérdida contrastiva y muestreo de negativos aleatorio, BM25 y ANCE. Indexan los pasos y tablas válidos con FAISS y anteponen los candidatos recuperados al *prompt* de un modelo StarCoderBase de 7 mil millones de parámetros ajustado (*fine-tuning*) con LoRA, de modo que el modelo pueda copiar elementos que sí existen.

**Para qué (métricas reportadas).** En el conjunto de evaluación humana (157 ejemplos), la proporción de pasos alucinados baja de 13,7 % a 1,9 % y la de tablas alucinadas de 20,6 % a 4,2 %. En cinco conjuntos fuera de dominio, las tablas alucinadas bajan de 42,8 % a 6,6 % en promedio. El recuperador alcanza Recall@15 de 0,743 para pasos y Recall@10 de 0,766 para tablas.

**Relación con GrowLink.** Es el antecedente más cercano al problema de anclaje: GrowLink también restringe la salida a un conjunto cerrado (el catálogo de habilidades). Difiere en que no entrena ni recupera, sino que entrega el catálogo completo en el contexto y verifica el resultado con un validador determinista.

### 2.2 Chen, Lin, Schärli y Zhou (2024) — Self-Debugging

**Problema.** Generar un programa correcto en un solo intento es difícil, y los modelos corrigen mal sus errores sin retroalimentación externa.

**Cómo.** Sin ajuste fino, un modelo preentrenado recibe pocos ejemplos (*few-shot*) para depurar su propia salida: genera el programa, este se ejecuta y el modelo recibe el resultado de la ejecución o una explicación en lenguaje natural del código, con lo que produce una versión corregida. El ciclo se repite de manera iterativa, reutilizando las predicciones fallidas y los mensajes de retroalimentación.

**Para qué (métricas reportadas).** En Spider (texto a SQL), donde no hay pruebas unitarias, la depuración con explicación del código mejora el baseline entre 2 % y 3 % y la exactitud en los problemas más difíciles en 9 %. En TransCoder y MBPP, donde sí hay pruebas unitarias, mejora la exactitud hasta 12 %. Además, iguala o supera a baselines que generan más de diez veces más candidatos.

**Relación con GrowLink.** Fundamenta el mecanismo de reintento: cuando el Validador de Salida detecta violaciones (R1–R7), estas se reenvían al modelo junto con el mismo *prompt*, de forma análoga a la retroalimentación de ejecución en Self-Debugging.

## Problema 3. Actualización incremental de una ruta ya generada

La revisión realizada no identificó un trabajo que aborde de forma específica la actualización de una ruta de aprendizaje generada por un LLM preservando el progreso ya completado. Los antecedentes disponibles abordan la revisión de planes ante nueva información, que es el mecanismo técnico subyacente; por eso se citan como base, y la ausencia de trabajos directos se reconoce como una brecha de la literatura.

### 3.1 Yao et al. (2023) — ReAct

**Problema.** El razonamiento (cadena de pensamiento) y la acción se habían estudiado por separado, de modo que un plan generado no se ajustaba cuando la información del entorno cambiaba.

**Cómo.** El modelo intercala trazas de razonamiento y acciones: el razonamiento permite inducir, seguir y actualizar planes de acción y manejar excepciones, mientras que las acciones consultan fuentes externas o el entorno y devuelven observaciones que alimentan el siguiente paso de razonamiento. Se usa con solo uno o dos ejemplos en el *prompt*.

**Para qué (métricas reportadas).** En ALFWorld y WebShop, supera a métodos de imitación y de aprendizaje por refuerzo por 34 % y 10 % de tasa de éxito absoluta, respectivamente. En HotpotQA y Fever reduce las alucinaciones y la propagación de errores frente a la cadena de pensamiento.

**Relación con GrowLink.** Aporta el principio de revisar un plan a partir de nuevas observaciones, aplicable cuando el perfil del usuario cambia y el roadmap debe recalcularse.

### 3.2 Wang et al. (2023) — DEPS

**Problema.** En entornos abiertos, los planes largos generados por un LLM fallan y deben corregirse a partir de la retroalimentación de la ejecución.

**Cómo.** El método *Describe, Explain, Plan and Select* (DEPS) incorpora un describidor de la ejecución, un explicador que interpreta en lenguaje natural la causa de cada fallo, un planificador que vuelve a planificar con esa explicación y un selector entrenable que ordena los subobjetivos paralelos según los pasos estimados para completarlos.

**Para qué (métricas reportadas).** En 71 tareas de Minecraft, es el primer agente que completa de forma robusta más de 70 tareas y casi duplica el desempeño global frente a los métodos de comparación. También muestra efectividad en ALFWorld y en manipulación de objetos sobre mesa.

**Relación con GrowLink.** Muestra que explicar la causa de un fallo y replanificar a partir de ella mejora el resultado, lo cual sustenta el paso de reenviar los motivos de rechazo del validador y, en trabajo futuro, la revisión incremental del roadmap. Complementa lo observado en PersonaPath (apartado 1.2), donde la planificación paso a paso con actualización del estado del estudiante obtuvo mejor adaptatividad que la generación de la ruta completa en una sola pasada.

## Síntesis

| Problema | Antecedente | Aporte principal | Cifra de referencia |
|---|---|---|---|
| 1 | Ng y Fung (2024) | LLM con *prompts* personalizados y justificación | Exactitud 75,8 % → 88,3 % (GPT-4) |
| 1 | Liu et al. (2026) | Benchmark de planificación con prerrequisitos | Validez 90,9 % frente a adaptatividad 44,3 % |
| 2 | Ayala y Béchard (2024) | RAG para reducir alucinación en salida estructurada | Pasos alucinados 13,7 % → 1,9 % |
| 2 | Chen et al. (2024) | Corrección iterativa con retroalimentación | Hasta +12 % de exactitud |
| 3 | Yao et al. (2023) | Revisión del plan con observaciones | +34 % y +10 % de éxito absoluto |
| 3 | Wang et al. (2023) | Replanificación con explicación del fallo | Más de 70 de 71 tareas |

## Referencias

Ayala, O., y Béchard, P. (2024). Reducing hallucination in structured outputs via retrieval-augmented generation. En *Proceedings of the 2024 Conference of the North American Chapter of the Association for Computational Linguistics: Human Language Technologies (Volume 6: Industry Track)* (pp. 228–238). Association for Computational Linguistics. https://doi.org/10.18653/v1/2024.naacl-industry.19

Chen, X., Lin, M., Schärli, N., y Zhou, D. (2024). Teaching large language models to self-debug. En *The Twelfth International Conference on Learning Representations (ICLR 2024)*. https://arxiv.org/abs/2304.05128

Liu, Y., Liu, Z., Zhang, T., Cheng, Z., Guo, Y., Chen, K., Zhang, M., Wang, Y., y Wang, H. (2026). PersonaPath: Towards knowledge-centric personalized learning path planning. En *Proceedings of the 15th International Joint Conference on Natural Language Processing and the 5th Conference of the Asia-Pacific Chapter of the Association for Computational Linguistics* (pp. 3387–3423). Association for Computational Linguistics. https://aclanthology.org/2026.aacl-long.154.pdf

Ng, C., y Fung, Y. (2024). *Educational personalized learning path planning with large language models*. arXiv. https://arxiv.org/abs/2407.11773

Wang, Z., Cai, S., Chen, G., Liu, A., Ma, X., y Liang, Y. (2023). Describe, explain, plan and select: Interactive planning with large language models enables open-world multi-task agents. En *Advances in Neural Information Processing Systems (NeurIPS 2023)*. https://arxiv.org/abs/2302.01560

Yao, S., Zhao, J., Yu, D., Du, N., Shafran, I., Narasimhan, K., y Cao, Y. (2023). ReAct: Synergizing reasoning and acting in language models. En *The Eleventh International Conference on Learning Representations (ICLR 2023)*. https://arxiv.org/abs/2210.03629
