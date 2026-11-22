import csv
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

try:
    from jsonschema import ValidationError, validate
except ImportError:
    sys.exit("Falta jsonschema: ejecuta  pip install jsonschema")

RAIZ = Path(__file__).resolve().parent.parent
URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/api/roadmap"
PAUSA = float(sys.argv[2]) if len(sys.argv) > 2 else 13
SCHEMA = json.loads((RAIZ / "datos" / "roadmap.schema.json").read_text(encoding="utf-8"))
SALIDA = RAIZ / "resultados"
(SALIDA / "respuestas").mkdir(parents=True, exist_ok=True)


def llamar(perfil):
    req = urllib.request.Request(
        URL, data=json.dumps(perfil).encode("utf-8"),
        headers={"Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=300) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8"))
        except Exception:
            return e.code, {}
    except Exception as e:
        return 0, {"error": str(e)}


perfiles = sorted((RAIZ / "datos" / "perfiles_prueba").glob("p*.json"))
filas = []
for i, ruta in enumerate(perfiles):
    perfil = json.loads(ruta.read_text(encoding="utf-8"))
    t0 = time.time()
    status, cuerpo = llamar(perfil)
    t_ms = int((time.time() - t0) * 1000)
    (SALIDA / "respuestas" / f"{ruta.stem}.json").write_text(
        json.dumps(cuerpo, ensure_ascii=False, indent=2), encoding="utf-8")

    meta = cuerpo.get("meta", cuerpo)
    reglas = meta.get("reglasFalladas", [])
    schema_ok = 0
    if status == 200:
        try:
            validate({"roadmap": cuerpo["roadmap"]}, SCHEMA)
            schema_ok = 1
        except (ValidationError, KeyError):
            pass
    fila = {
        "perfil": ruta.stem,
        "valido": int(status == 200 and meta.get("valido", False)),
        "intentos": meta.get("intentos", 0),
        "tiempo_ms": t_ms,
        "reglas_falladas": "|".join(reglas),
        "fuera_catalogo": int("R2" in reglas),
        "schema_ok": schema_ok,
        "rubrica": "",
    }
    filas.append(fila)
    print(fila["perfil"], "HTTP", status, "intentos", fila["intentos"],
          "ms", t_ms, reglas, flush=True)
    if i < len(perfiles) - 1:
        time.sleep(PAUSA)

with open(SALIDA / "resultados.csv", "w", newline="", encoding="utf-8") as f:
    w = csv.DictWriter(f, fieldnames=list(filas[0].keys()))
    w.writeheader()
    w.writerows(filas)

n = len(filas)
pct = lambda x: round(100 * x / n, 1)
validos = [f for f in filas if f["valido"]]
tiempos = [f["tiempo_ms"] for f in filas]
print("\n=== RESUMEN ===")
print(f"Perfiles evaluados: {n}")
print(f"Cumplen el schema: {pct(sum(f['schema_ok'] for f in filas))}%")
print(f"Válidos: {pct(len(validos))}%  (al primer intento: "
      f"{pct(sum(1 for f in validos if f['intentos'] == 1))}%)")
print(f"Con rechazo del validador en algún intento: "
      f"{pct(sum(1 for f in filas if f['reglas_falladas']))}%")
print(f"Terminaron en error controlado: {pct(n - len(validos))}%")
print(f"Perfiles con habilidad fuera del catálogo (R2) en algún intento: "
      f"{sum(f['fuera_catalogo'] for f in filas)}")
print(f"Tiempo promedio: {sum(tiempos) / n / 1000:.1f} s  |  "
      f"dentro de 15 s: {pct(sum(1 for t in tiempos if t <= 15000))}%")