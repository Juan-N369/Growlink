"""Mock del endpoint POST /api/roadmap con el mismo contrato que el backend real.

Uso:
    python herramientas/mock_server.py [--puerto 8080] [--prob-error 0.15] [--prob-reintento 0.25] [--semilla 42]

Responde con datos/roadmap_ejemplo.json. Aleatoriamente simula:
  - un reintento exitoso (intentos = 2),
  - un error controlado (HTTP 422 o 502) con el cuerpo definido en el contrato.
"""
import argparse
import json
import random
import time
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

DATOS = Path(__file__).resolve().parent.parent / "datos"
ROADMAP = json.loads((DATOS / "roadmap_ejemplo.json").read_text(encoding="utf-8"))
CATALOGO = json.loads((DATOS / "catalogo.json").read_text(encoding="utf-8"))
IDS = {h["id"] for h in CATALOGO["habilidades"]}
METAS = {m["id"] for m in CATALOGO["metas"]}
REGLAS = ["R1", "R2", "R3", "R4", "R5", "R6", "R7"]


class Handler(BaseHTTPRequestHandler):
    cfg = None

    def _responder(self, codigo, cuerpo):
        datos = json.dumps(cuerpo, ensure_ascii=False).encode("utf-8")
        self.send_response(codigo)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(datos)))
        self.end_headers()
        self.wfile.write(datos)

    def do_POST(self):
        if self.path != "/api/roadmap":
            return self._responder(404, {"error": "Ruta no encontrada"})
        try:
            n = int(self.headers.get("Content-Length", 0))
            perfil = json.loads(self.rfile.read(n).decode("utf-8"))
            assert perfil["meta"] in METAS
            assert all(h in IDS for h in perfil["habilidades_actuales"])
        except Exception:
            return self._responder(400, {"error": "Perfil inválido", "intentos": 0, "reglasFalladas": []})

        c, r = self.cfg, self.cfg.rng
        t = r.randint(3000, 12000)  # tiempo simulado reportado por el "backend"
        time.sleep(c.latencia)
        x = r.random()
        if x < c.prob_error:
            fallas = sorted(r.sample(REGLAS, r.randint(1, 2)))
            codigo = r.choice([422, 502])
            return self._responder(codigo, {"error": "No se pudo generar un roadmap válido", "intentos": 2, "reglasFalladas": fallas})
        intentos = 2 if x < c.prob_error + c.prob_reintento else 1
        return self._responder(200, {
            "roadmap": ROADMAP["roadmap"],
            "meta": {"intentos": intentos, "valido": True, "tiempoMs": t * intentos,
                     "reglasFalladas": [r.choice(REGLAS)] if intentos == 2 else []},
        })

    def log_message(self, *a):
        pass


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--puerto", type=int, default=8080)
    ap.add_argument("--prob-error", type=float, default=0.15)
    ap.add_argument("--prob-reintento", type=float, default=0.25)
    ap.add_argument("--latencia", type=float, default=0.05, help="segundos de espera real por petición")
    ap.add_argument("--semilla", type=int, default=None)
    a = ap.parse_args()
    a.rng = random.Random(a.semilla)
    Handler.cfg = a
    print(f"Mock escuchando en http://localhost:{a.puerto}/api/roadmap (Ctrl+C para salir)")
    HTTPServer(("", a.puerto), Handler).serve_forever()
