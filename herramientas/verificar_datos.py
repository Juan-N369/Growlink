import json
from pathlib import Path

D = Path(__file__).resolve().parent.parent / "datos"
cargar = lambda n: json.loads((D / n).read_text(encoding="utf-8"))

cat = cargar("catalogo.json")
ids = [h["id"] for h in cat["habilidades"]]
assert len(ids) == len(set(ids)), "Hay ids duplicados"
conocidos = set(ids)
prereq = {h["id"]: h["prerrequisitos"] for h in cat["habilidades"]}

for hid, ps in prereq.items():
    for p in ps:
        assert p in conocidos, f"{hid}: prerrequisito inexistente '{p}'"
for m in cat["metas"]:
    for k in m["habilidades_clave"]:
        assert k in conocidos, f"Meta {m['id']}: habilidad inexistente '{k}'"

estado = {}
def visitar(i):
    if estado.get(i) == 1:
        raise AssertionError(f"Ciclo de prerrequisitos en '{i}'")
    if estado.get(i) == 2:
        return
    estado[i] = 1
    for p in prereq[i]:
        visitar(p)
    estado[i] = 2
for i in ids:
    visitar(i)
print(f"OK: {len(ids)} habilidades, {len(cat['metas'])} metas, sin ciclos")

try:
    from jsonschema import validate
    validate(cargar("roadmap_ejemplo.json"), cargar("roadmap.schema.json"))
    print("OK: roadmap_ejemplo cumple el schema")
except ImportError:
    print("(opcional) pip install jsonschema para validar el schema")