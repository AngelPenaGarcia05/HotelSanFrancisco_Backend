# Backups de la base de datos — Hotel San Francisco

Guía mínima de respaldo y restauración de la BD PostgreSQL (`hotel_sf`),
tanto local como en Railway.

## 1. Hacer un backup manual

Con las herramientas de PostgreSQL (`pg_dump`, incluidas en la instalación
local — en Windows: `C:\Program Files\PostgreSQL\18\bin`):

```bash
# Local
pg_dump -h localhost -U postgres -d hotel_sf -F c -f hotel_sf_YYYY-MM-DD.dump

# Railway (host, puerto, usuario y contraseña salen de las variables del
# servicio Postgres en el panel de Railway: PGHOST, PGPORT, PGUSER, PGPASSWORD)
pg_dump -h <PGHOST> -p <PGPORT> -U <PGUSER> -d railway -F c -f hotel_sf_YYYY-MM-DD.dump
```

Notas:
- `-F c` genera formato comprimido "custom", el más flexible para restaurar.
- La contraseña se puede pasar con la variable de entorno `PGPASSWORD` para
  evitar el prompt interactivo.
- **Recomendado**: sacar un backup antes de cada deploy que incluya una
  migración nueva de Flyway, y otro antes de la demo/presentación.

## 2. Restaurar un backup

```bash
# Restaurar sobre una BD vacía (crea las tablas y los datos del dump)
createdb -h localhost -U postgres hotel_sf_restaurada
pg_restore -h localhost -U postgres -d hotel_sf_restaurada hotel_sf_YYYY-MM-DD.dump

# Restaurar "encima" de la BD existente (destructivo: reemplaza objetos)
pg_restore -h localhost -U postgres -d hotel_sf --clean --if-exists hotel_sf_YYYY-MM-DD.dump
```

Regla de oro: **probar la restauración al menos una vez** en una BD temporal
(`hotel_sf_restaurada`) — un backup que nunca se restauró es solo una promesa.
Tras restaurar, arrancar el backend contra esa BD y verificar que Flyway
valide las migraciones y el login funcione.

## 3. Qué hace Railway automáticamente

- Los backups automáticos de Railway dependen del plan contratado
  (en planes de pago existen backups diarios/instantáneas del volumen;
  verificar en: servicio Postgres → pestaña *Backups* del panel).
- Si el plan actual no incluye backups automáticos, los backups manuales de
  la sección 1 son la única red de seguridad: agendar uno antes de cada
  entrega importante.

## 4. Qué contiene (y qué no) un backup

- Contiene: esquema completo, datos, secuencias e historial de Flyway
  (`flyway_schema_history`), por lo que el backend arranca directo tras
  restaurar, sin re-ejecutar migraciones.
- No contiene: variables de entorno (JWT_SECRET_KEY, SMTP_*, etc.), que viven
  en Railway/local y deben respaldarse por separado (gestor de secretos o
  nota segura del equipo).
