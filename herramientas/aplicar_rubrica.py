import csv
from pathlib import Path

R = Path(__file__).resolve().parent.parent
# perfil: (coherencia, justificaciones, adecuacion, cobertura)
P = {
 "p01": (4, 3, 5, 5), "p02": (4, 4, 5, 5), "p03": (3, 4, 3, 5), "p04": (3, 4, 5, 5),
 "p05": (4, 3, 5, 5), "p06": (4, 3, 4, 4), "p07": (4, 3, 5, 5), "p08": (5, 3, 5, 5),
 "p09": (5, 4, 5, 5), "p10": (4, 3, 5, 5), "p11": (5, 4, 5, 5), "p12": (5, 5, 5, 5),
 "p13": (4, 4, 5, 5), "p14": (5, 5, 5, 5), "p15": (4, 3, 4, 5), "p16": (4, 3, 5, 5),
 "p17": (5, 4, 5, 5), "p18": (4, 4, 5, 5), "p19": (4, 4, 5, 5), "p20": (5, 3, 5, 5),
}
ruta = R / "resultados" / "resultados.csv"
with open(ruta, encoding="utf-8") as fh:
    filas = list(csv.DictReader(fh))
for f in filas:
    f["rubrica"] = f"{sum(P[f['perfil']]) / 4:.2f}"
with open(ruta, "w", newline="", encoding="utf-8") as fh:
    w = csv.DictWriter(fh, fieldnames=list(filas[0].keys()))
    w.writeheader()
    w.writerows(filas)

print("| Perfil | Coherencia | Justificaciones | Adecuación | Cobertura | Promedio |")
print("|---|---|---|---|---|---|")
for k, v in P.items():
    print(f"| {k} | {v[0]} | {v[1]} | {v[2]} | {v[3]} | {sum(v) / 4:.2f} |")
cols = list(zip(*P.values()))
print("Promedio por criterio:", [round(sum(c) / len(P), 2) for c in cols])
print("Promedio global:", round(sum(sum(v) for v in P.values()) / (4 * len(P)), 2))