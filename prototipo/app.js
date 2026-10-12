/* GrowLink · Prototipo navegable. Desarrollado con asistencia de Claude (Anthropic)
  a partir del diseño y la descripción del equipo; revisado y probado por el equipo. */

// ---- Datos copiados de catalogo.json: id: [nombre, nivel, prerrequisitos] ----
const HAB = {
  java_basico:["Java básico","basico",[]],
  poo:["Programación orientada a objetos","basico",["java_basico"]],
  git:["Control de versiones con Git","basico",[]],
  sql_basico:["SQL básico","basico",[]],
  estructuras_datos:["Estructuras de datos","basico",["java_basico"]],
  linux_cli:["Línea de comandos en Linux","basico",[]],
  http_rest_conceptos:["Conceptos de HTTP y REST","basico",[]],
  spring_boot:["Spring Boot","intermedio",["java_basico","poo"]],
  apis_rest:["Construcción de APIs REST","intermedio",["spring_boot","http_rest_conceptos"]],
  jpa_hibernate:["Persistencia con JPA/Hibernate","intermedio",["sql_basico","spring_boot"]],
  testing_unitario:["Pruebas unitarias","intermedio",["java_basico"]],
  docker:["Contenedores con Docker","intermedio",["linux_cli"]],
  seguridad_basica:["Seguridad básica en APIs","intermedio",["apis_rest"]],
  maven_gradle:["Maven o Gradle","intermedio",["java_basico"]],
  microservicios:["Arquitectura de microservicios","avanzado",["apis_rest","docker"]],
  mensajeria:["Mensajería asíncrona","avanzado",["microservicios"]],
  ci_cd:["Integración y despliegue continuo","avanzado",["git","docker","testing_unitario"]],
  kubernetes:["Orquestación con Kubernetes","avanzado",["docker"]],
  observabilidad:["Observabilidad y monitoreo","avanzado",["microservicios"]],
  cloud_basico:["Fundamentos de nube","avanzado",["docker"]]
};
const NIVEL = {basico:"Básico", intermedio:"Intermedio", avanzado:"Avanzado"};
const METAS = {
  backend_junior:"Desarrollador backend junior",
  backend_microservicios:"Backend con microservicios",
  backend_cloud:"Backend orientado a nube"
};
// Roadmaps predeterminados (perfil de ejemplo: ya domina java_basico y git)
const ROADMAPS = {
  backend_junior:[
    ["poo","Base necesaria para trabajar con Spring Boot."],
    ["sql_basico","Fundamento para manejar datos en cualquier backend."],
    ["http_rest_conceptos","Fundamento de las APIs que vas a construir."],
    ["spring_boot","Framework principal del backend en Java."],
    ["apis_rest","Habilidad clave de la meta: exponer servicios a los clientes."]
  ],
  backend_microservicios:[
    ["poo","Base necesaria para Spring Boot."],
    ["spring_boot","Framework principal del backend."],
    ["http_rest_conceptos","Fundamento de las APIs."],
    ["apis_rest","Requisito directo de microservicios."],
    ["linux_cli","Requisito para trabajar con Docker."],
    ["docker","Los microservicios se empaquetan en contenedores."],
    ["microservicios","Meta principal del usuario."],
    ["mensajeria","Comunicación asíncrona entre servicios."]
  ],
  backend_cloud:[
    ["linux_cli","Base para trabajar con contenedores y servidores."],
    ["docker","Requisito de Kubernetes y del despliegue en la nube."],
    ["testing_unitario","Requisito para automatizar pruebas en CI/CD."],
    ["ci_cd","Habilidad clave: automatizar integración y despliegue."],
    ["kubernetes","Habilidad clave: orquestar contenedores a escala."],
    ["cloud_basico","Habilidad clave: entender los servicios de la nube."],
    ["poo","Base necesaria para Spring Boot."],
    ["spring_boot","Framework sobre el que se construyen los servicios."],
    ["http_rest_conceptos","Fundamento de las APIs."],
    ["apis_rest","Requisito de microservicios."],
    ["microservicios","Requisito de observabilidad."],
    ["observabilidad","Habilidad clave: monitorear sistemas distribuidos."]
  ]
};

const $ = id => document.getElementById(id);
function mostrar(id){
  document.querySelectorAll(".vista").forEach(v => v.classList.remove("activa"));
  $(id).classList.add("activa");
  window.scrollTo(0,0);
}

// ---- Pantalla 1: llenar listas ----
Object.entries(METAS).forEach(([id,nombre]) => {
  const o = document.createElement("option"); o.value = id; o.textContent = nombre; $("meta").appendChild(o);
});
Object.entries(HAB).forEach(([id,h]) => {
  const b = document.createElement("button");
  b.type = "button"; b.className = "chip"; b.textContent = h[0];
  if (id === "java_basico" || id === "git") b.classList.add("on");
  b.setAttribute("aria-pressed", b.classList.contains("on"));
  b.onclick = () => { b.classList.toggle("on"); b.setAttribute("aria-pressed", b.classList.contains("on")); };
  $("chips").appendChild(b);
});
const hoy = new Date(); hoy.setHours(0,0,0,0);
$("fecha").min = hoy.toISOString().slice(0,10);
$("meta").onchange = () => { $("btn-generar").disabled = !$("meta").value; };

// ---- Generar (simulado) ----
$("btn-generar").onclick = () => {
  const meta = $("meta").value;
  if (!meta) return;
  const error = $("simular-error").checked;
  mostrar("vista-carga");
  const msgs = [[0,"Generando con Claude…"],[1000,"Validando contra el catálogo…"]];
  if (error) msgs.push([2000,"Reintentando…"],[3000,"Validando de nuevo…"]);
  msgs.forEach(([t,m]) => setTimeout(() => { $("msg-carga").textContent = m; }, t));
  setTimeout(() => { error ? mostrar("vista-error") : construir(meta); }, error ? 4000 : 2000);
};
$("btn-reintentar").onclick = () => mostrar("vista-meta");
$("btn-volver").onclick = () => mostrar("vista-meta");

// ---- Pantalla 2 ----
let marcadas = new Set(), aplicadas = new Set(), total = 0;

function construir(meta){
  const pasos = ROADMAPS[meta];
  total = pasos.length; marcadas = new Set(); aplicadas = new Set();
  $("titulo-meta").textContent = METAS[meta];
  $("texto-fecha").textContent = textoFecha();
  const linea = $("linea"); linea.innerHTML = "";
  pasos.forEach(([id,just], i) => {
    const [nombre,nivel,pre] = HAB[id];
    const item = document.createElement("div");
    item.className = "item";
    item.innerHTML =
      '<div class="tarjeta" role="button" tabindex="0" aria-pressed="false">' +
        '<div class="top"><span class="num">' + (i+1) + '</span><span class="nivel">' + NIVEL[nivel] + '</span></div>' +
        '<h3 class="nombre">' + nombre + '</h3>' +
        '<p>' + just + '</p>' +
        '<p class="req">' + (pre.length ? "Requiere: " + pre.map(p => HAB[p][0]).join(", ") : "Sin prerrequisitos") + '</p>' +
      '</div>';
    const tarjeta = item.firstChild;
    const alternar = () => {
      marcadas.has(i) ? marcadas.delete(i) : marcadas.add(i);
      item.classList.toggle("hecho", marcadas.has(i));
      tarjeta.setAttribute("aria-pressed", marcadas.has(i));
      pintar();
    };
    tarjeta.onclick = alternar;
    tarjeta.onkeydown = e => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); alternar(); } };
    linea.appendChild(item);
  });
  pintar();
  mostrar("vista-roadmap");
}

function textoFecha(){
  const v = $("fecha").value;
  if (!v) return "Sin fecha objetivo";
  const [y,m,d] = v.split("-").map(Number);
  const dias = Math.round((new Date(y,m-1,d) - hoy) / 86400000);
  const resto = dias > 0 ? "faltan " + dias + " días" : dias === 0 ? "es hoy" : "fecha vencida";
  return "Fecha objetivo: " + d + "/" + m + "/" + y + " · " + resto;
}

function difiere(){
  if (marcadas.size !== aplicadas.size) return true;
  for (const x of marcadas) if (!aplicadas.has(x)) return true;
  return false;
}

function pintar(){
  const pct = total ? Math.round(aplicadas.size / total * 100) : 0;
  $("relleno").style.width = pct + "%";
  const p = $("pct");
  p.textContent = pct + "%";
  p.style.left = pct + "%";
  p.style.transform = pct >= 12 ? "translateX(calc(-100% - 10px))" : "translateX(10px)";
  p.style.color = pct >= 12 ? "#10202a" : "var(--text)";
  $("pend").textContent = difiere() ? "Cambios sin aplicar" : "";
}

$("btn-actualizar").onclick = () => { aplicadas = new Set(marcadas); pintar(); };