#!/usr/bin/env python3

import json
import os
import sys
from datetime import datetime, timezone
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse


RELAY_HOST = os.getenv("RELAY_HOST", "0.0.0.0")
RELAY_PORT = int(os.getenv("RELAY_PORT", "8787"))
RELAY_TOKEN = os.getenv("RELAY_TOKEN", "").strip()
RELAY_ALLOWED_IP = os.getenv("RELAY_ALLOWED_IP", "").strip()

NOMBRES_PROCESO = {
    "TOTAL": "Scraping total",
    "ZARA": "Scraping Zara",
    "BERSHKA": "Scraping Bershka",
    "PULL_AND_BEAR": "Scraping Pull and Bear",
}


def utc_now_iso():
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def nombre_proceso(tipo):
    if not tipo:
        return "Scraping"

    return NOMBRES_PROCESO.get(tipo, f"Scraping {tipo}")


class IPhoneRelayHandler(BaseHTTPRequestHandler):
    server_version = "iPhoneRelay/1.0"

    def log_message(self, fmt, *args):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        sys.stdout.write(f"[{timestamp}] {self.address_string()} - {fmt % args}\n")
        sys.stdout.flush()

    def do_GET(self):
        path = urlparse(self.path).path

        if path == "/":
            self._send_json(
                HTTPStatus.OK,
                {
                    "status": "ok",
                    "message": "Relay fallback de iPhone activo en iSH.",
                    "routes": [
                        "GET /internal/mail-relay/ping",
                        "POST /internal/mail-relay/scraping",
                    ],
                    "scrapingDisponible": False,
                },
            )
            return

        if path == "/internal/mail-relay/ping":
            if not self._authorize_request():
                return

            self._send_json(
                HTTPStatus.OK,
                {
                    "status": "ok",
                    "message": (
                        "Relay fallback iPhone disponible en iSH. "
                        "Recibe peticiones, pero no puede ejecutar el scraping real de este proyecto."
                    ),
                    "scrapingDisponible": False,
                    "relayRole": "FALLBACK_IPHONE_ISH",
                    "receivedAt": utc_now_iso(),
                },
            )
            return

        self._send_json(
            HTTPStatus.NOT_FOUND,
            {
                "status": "not_found",
                "message": "Ruta no soportada por el relay fallback del iPhone.",
                "path": path,
            },
        )

    def do_POST(self):
        path = urlparse(self.path).path

        if path != "/internal/mail-relay/scraping":
            self._send_json(
                HTTPStatus.NOT_FOUND,
                {
                    "status": "not_found",
                    "message": "Ruta no soportada por el relay fallback del iPhone.",
                    "path": path,
                },
            )
            return

        if not self._authorize_request():
            return

        payload = self._read_json_body()
        if payload is None:
            return

        tipo = str(payload.get("tipo", "")).strip().upper()
        nombre = nombre_proceso(tipo)
        mensaje = (
            "El iPhone fallback recibio la peticion"
            + (f" de {nombre}" if tipo else " de scraping")
            + ", pero iSH/iOS no puede ejecutar el scraping real de este proyecto. "
            + "Se devuelve una respuesta controlada para que el backend lo deje pendiente."
        )

        self.log_message("Solicitud de scraping recibida: tipo=%s", tipo or "DESCONOCIDO")

        self._send_json(
            HTTPStatus.OK,
            {
                "tipo": tipo or "DESCONOCIDO",
                "nombreProceso": nombre,
                "productos": [],
                "scrapingDisponible": False,
                "mensajeRelay": mensaje,
                "relayRole": "FALLBACK_IPHONE_ISH",
                "receivedAt": utc_now_iso(),
            },
        )

    def _authorize_request(self):
        token = self.headers.get("X-Relay-Token", "")
        client_ip = self._client_ip()

        if RELAY_TOKEN and token != RELAY_TOKEN:
            self._send_json(
                HTTPStatus.FORBIDDEN,
                {
                    "status": "forbidden",
                    "message": "Token de relay no valido.",
                },
            )
            return False

        if RELAY_ALLOWED_IP and client_ip != RELAY_ALLOWED_IP:
            self._send_json(
                HTTPStatus.FORBIDDEN,
                {
                    "status": "forbidden",
                    "message": "IP no autorizada.",
                    "clientIp": client_ip,
                },
            )
            return False

        return True

    def _client_ip(self):
        forwarded_for = self.headers.get("X-Forwarded-For", "")
        if forwarded_for:
            return forwarded_for.split(",")[0].strip()

        return self.client_address[0]

    def _read_json_body(self):
        content_length = self.headers.get("Content-Length", "0").strip()

        try:
            total_bytes = int(content_length or "0")
        except ValueError:
            total_bytes = 0

        raw_body = self.rfile.read(total_bytes) if total_bytes > 0 else b""

        if not raw_body:
            return {}

        try:
            return json.loads(raw_body.decode("utf-8"))
        except Exception:
            self._send_json(
                HTTPStatus.BAD_REQUEST,
                {
                    "status": "bad_request",
                    "message": "JSON no valido.",
                },
            )
            return None

    def _send_json(self, status_code, payload):
        body = json.dumps(payload, ensure_ascii=True).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)


def main():
    if not RELAY_TOKEN:
        print(
            "AVISO: RELAY_TOKEN no esta definido. El relay aceptara peticiones sin validar token.",
            flush=True,
        )

    server = ThreadingHTTPServer((RELAY_HOST, RELAY_PORT), IPhoneRelayHandler)
    print(f"Relay fallback iPhone escuchando en http://{RELAY_HOST}:{RELAY_PORT}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nRelay detenido.", flush=True)
