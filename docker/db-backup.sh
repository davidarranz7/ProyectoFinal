#!/usr/bin/env sh
set -eu

DB_HOST="${DB_HOST:-mariadb}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-ropa_db}"
DB_ADMIN_USER="${DB_ADMIN_USER:-root}"
DB_ADMIN_PASSWORD="${DB_ADMIN_PASSWORD:-}"
BACKUP_INTERVAL_SECONDS="${BACKUP_INTERVAL_SECONDS:-86400}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"

mkdir -p /backups

while true; do
  export MYSQL_PWD="${DB_ADMIN_PASSWORD}"

  if mariadb-admin ping -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_ADMIN_USER}" --silent; then
    timestamp="$(date +%Y%m%d_%H%M%S)"
    tmp_file="/backups/${DB_NAME}_${timestamp}.sql.tmp"
    backup_file="/backups/${DB_NAME}_${timestamp}.sql"

    if mariadb-dump \
      -h"${DB_HOST}" \
      -P"${DB_PORT}" \
      -u"${DB_ADMIN_USER}" \
      --single-transaction \
      --quick \
      --skip-lock-tables \
      --databases "${DB_NAME}" > "${tmp_file}"; then
      mv "${tmp_file}" "${backup_file}"
      find /backups -type f -name "${DB_NAME}_*.sql" -mtime +"${BACKUP_RETENTION_DAYS}" -delete || true
      echo "[db-backup] Backup creado: ${backup_file}"
    else
      rm -f "${tmp_file}"
      echo "[db-backup] No se pudo crear el backup de ${DB_NAME}" >&2
    fi
  else
    echo "[db-backup] MariaDB no responde. Se reintentara en el siguiente ciclo." >&2
  fi

  unset MYSQL_PWD
  sleep "${BACKUP_INTERVAL_SECONDS}"
done
