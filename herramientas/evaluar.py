"""Evalúa el endpoint POST /api/roadmap con los perfiles de datos/perfiles_prueba/.

Uso:
    python herramientas/evaluar.py [--url http://localhost:8080/api/roadmap] [--timeout 60]

Guarda la respuesta cruda en resultados/respuestas/pXX.json y las métricas en
resultados/resultados.csv. La columna 'rubrica' se conserva si el CSV ya existía
(se llena a mano, ver docs/texto/rubrica.md).
Requiere: pip install jsonschema
"""
import argparse
import csv
import json
import socket
import time
import urllib.error
import urllib.request
from pathlib import Path

from jsonschema import Draft202012Validator

RAIZ = Path(__file__).resolve().parent.parent
COLUMNAS = ["perfil", "valido", "intentos", "tiempo_ms", "reglas_falladas", "fuera_catalogo", "schema_ok", "rubrica"]


def llamar(url, perfil, timeout):
    """Devuelve (codigo_http, cuerpo_texto, tiempo_ms, error_transporte)."""
    req = urllib.request.Request(url, data=json.dumps(perfil).encode("utf-8"),
                                 headers={"Content-Type": "application/json"}, method="POST")
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            cuerpo, codigo, err = r.read().decode("utf-8"), r.status, None
    except urllib.error.HTTPError as e:
        cuerpo, codigo, err = e.read().decode("utf-8", "replace"), e.code, None
    except (socket.timeout, TimeoutError):
        cuerpo, codigo, err = "", 0, "TIMEOUT"
    except urllib.error.URLError as e:
        cuerpo, codigo, err = "", 0, "RED" if not isinstance(e.reason, socket.timeout) else "TIMEOUT"
    return codigo, cuerpo, round((time.perf_counter() - t0) * 1000), err


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--url", default="http://localhost:8080/api/roadmap")
    ap.add_argument("--timeout", type=float, default=60)
    ap.add_argument("--perfiles", default=str(RAIZ / "datos" / "perfiles_prueba"))
    ap.add_argument("--salida", default=str(RAIZ / "resultados"))
    a = ap.parse_args()

    ids = {h["id"] for h in json.loads((RAIZ / "datos" / "catalogo.json").read_text(encoding="utf-8"))["habilidades"]}
    validador = Draft202012Validator(json.loads((RAIZ / "datos" / "roadmap.schema.json").read_text(encoding="utf-8")))
    salida = Path(a.salida)
    (salida / "respuestas").mkdir(parents=True, exist_ok=True)
    csv_path = salida / "resultados.csv"

    rubricas = {}  # conservar rúbrica ya llenada
    if csv_path.exists():
        with open(csv_path, newline="", encoding="utf-8") as f:
            rubricas = {r["perfil"]: r.get("rubrica", "") for r in csv.DictReader(f)}

    archivos = sorted(Path(a.perfiles).glob("p*.json"))
    filas = []
    for arch in archivos:
        pid = arch.stem
        perfil = json.loads(arch.read_text(encoding="utf-8"))
        codigo, cuerpo, ms, err = llamar(a.url, perfil, a.timeout)
        (salida / "respuestas" / f"{pid}.json").write_text(cuerpo if cuerpo else json.dumps({"error_transporte": err}), encoding="utf-8")

        try:
            datos = json.loads(cuerpo) if cuerpo else {}
        except json.JSONDecodeError:
            datos = {}
        roadmap = datos.get("roadmap") if isinstance(datos, dict) else None
        meta = datos.get("meta", {}) if isinstance(datos, dict) else {}

        if roadmap is not None:  # respuesta 200 con roadmap
            valido, intentos, reglas = bool(meta.get("valido", False)), meta.get("intentos", 1), meta.get("reglasFalladas", [])
            schema_ok = validador.is_valid({"roadmap": roadmap})
            fuera = sum(1 for p in roadmap if not isinstance(p, dict) or p.get("habilidad_id") not in ids)
        elif err:  # fallo de red / timeout
            valido, intentos, reglas, schema_ok, fuera = False, 0, [err], False, 0
        else:  # error controlado (422/502) u otra respuesta sin roadmap
            valido, intentos, reglas = False, datos.get("intentos", 0) if isinstance(datos, dict) else 0, datos.get("reglasFalladas", []) if isinstance(datos, dict) else []
            schema_ok, fuera = False, 0
        filas.append({"perfil": pid, "valido": valido, "intentos": intentos, "tiempo_ms": ms,
                      "reglas_falladas": ";".join(reglas), "fuera_catalogo": fuera, "schema_ok": schema_ok,
                      "rubrica": rubricas.get(pid, ""), "_error": roadmap is None})
        print(f"{pid}: HTTP {codigo or err} | valido={valido} intentos={intentos} {ms} ms")

    with open(csv_path, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=COLUMNAS, extrasaction="ignore", lineterminator="\n")
        w.writeheader()
        w.writerows(filas)

    n = len(filas)
    pct = lambda k: 100 * k / n if n else 0
    ok_schema = sum(r["schema_ok"] for r in filas)
    primero = sum(1 for r in filas if not r["_error"] and r["valido"] and r["intentos"] == 1)
    reintento = sum(1 for r in filas if not r["_error"] and r["valido"] and r["intentos"] > 1)
    controlado = sum(1 for r in filas if r["_error"] and r["intentos"] > 0)
    red = sum(1 for r in filas if r["_error"] and r["intentos"] == 0)
    tiempos = [r["tiempo_ms"] for r in filas]
    print("\n===== RESUMEN =====")
    print(f"Perfiles evaluados:               {n}")
    print(f"schema_ok:                        {pct(ok_schema):.1f}%  ({ok_schema}/{n})")
    print(f"Válidos al primer intento:        {pct(primero):.1f}%  ({primero}/{n})")
    print(f"Necesitaron reintento (y válidos):{pct(reintento):.1f}%  ({reintento}/{n})")
    print(f"Error controlado (422/502):       {pct(controlado):.1f}%  ({controlado}/{n})")
    print(f"Fallo de red/timeout/otro:        {pct(red):.1f}%  ({red}/{n})")
    if n:
        print(f"Tiempo promedio (cliente):        {sum(tiempos)/n/1000:.2f} s  (máx {max(tiempos)/1000:.2f} s)")
        print(f"Respuestas en ≤ 15 s:             {pct(sum(t <= 15000 for t in tiempos)):.1f}%")
    print(f"Habilidades fuera del catálogo:   {sum(r['fuera_catalogo'] for r in filas)}")
    print(f"\nCSV: {csv_path}")


if __name__ == "__main__":
    main()
