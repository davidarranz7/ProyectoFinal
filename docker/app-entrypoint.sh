#!/usr/bin/env bash
set -euo pipefail

DB_HOST="${DB_HOST:-mariadb}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-ropa_db}"
DB_ADMIN_USER="${DB_ADMIN_USER:-${SPRING_DATASOURCE_USERNAME:-root}}"
DB_ADMIN_PASSWORD="${DB_ADMIN_PASSWORD:-${SPRING_DATASOURCE_PASSWORD:-}}"
DB_WAIT_MAX_ATTEMPTS="${DB_WAIT_MAX_ATTEMPTS:-90}"
DB_WAIT_INTERVAL_SECONDS="${DB_WAIT_INTERVAL_SECONDS:-2}"

export MYSQL_PWD="${DB_ADMIN_PASSWORD}"

intento=1
until mariadb-admin ping -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_ADMIN_USER}" --silent; do
  if [ "${intento}" -ge "${DB_WAIT_MAX_ATTEMPTS}" ]; then
    echo "[entrypoint] MariaDB no estuvo disponible a tiempo. Cancelando arranque." >&2
    exit 1
  fi

  echo "[entrypoint] Esperando a MariaDB (${intento}/${DB_WAIT_MAX_ATTEMPTS})..."
  intento=$((intento + 1))
  sleep "${DB_WAIT_INTERVAL_SECONDS}"
done

echo "[entrypoint] MariaDB disponible. Garantizando base ${DB_NAME}..."
mariadb \
  -h"${DB_HOST}" \
  -P"${DB_PORT}" \
  -u"${DB_ADMIN_USER}" \
  -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

unset MYSQL_PWD

echo "[entrypoint] Base ${DB_NAME} lista. Arrancando aplicacion..."
exec java -jar app.jar
