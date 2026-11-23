import json
from pathlib import Path

R = Path(__file__).resolve().parent.parent
cat = {h["id"]: h for h in json.loads(
    (R / "datos" / "catalogo.json").read_text(encoding="utf-8"))["habilidades"]}
log = [json.loads(l) for l in (R / "resultados" / "intentos.jsonl")
       .read_text(encoding="utf-8").splitlines() if l.strip()]
assert len(log) == 20, f"intentos.jsonl tiene {len(log)} líneas, se esperaban 20"
out = []
for i, ruta in enumerate(sorted((R / "datos" / "perfiles_prueba").glob("p*.json"))):
    p = json.loads(ruta.read_text(encoding="utf-8"))
    resp = json.loads((R / "resultados" / "respuestas" / f"{ruta.stem}.json")
                      .read_text(encoding="utf-8"))
    out.append(f"## {ruta.stem} | {p['nivel_estudios']} | exp {p['anios_experiencia']} | "
               f"meta {p['meta']} | tiene: {','.join(p['habilidades_actuales']) or '-'}")
    for s in resp.get("roadmap", []):
        pre = ",".join(cat[s["habilidad_id"]]["prerrequisitos"]) or "-"
        out.append(f"{s['orden']}. {s['habilidad_id']} (pre: {pre}): {s['justificacion']}")
    for d in log[i]["generacion"]["detalle"]:
        if d["violaciones"]:
            out.append(f"  RECHAZO intento {d['numero']}: " + " / ".join(d["violaciones"]))
(R / "resultados" / "casos_para_rubrica.txt").write_text("\n".join(out), encoding="utf-8")
print("Listo: resultados/casos_para_rubrica.txt")