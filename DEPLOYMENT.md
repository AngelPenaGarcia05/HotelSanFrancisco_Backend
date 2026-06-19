# Despliegue en Railway

Guía para desplegar el backend del Hotel San Francisco en [Railway](https://railway.app).

## Arquitectura del despliegue

- **Build**: `Dockerfile` multi-stage (Maven 3.9 + Temurin 21 → JRE 21). Railway
  lo detecta automáticamente (declarado en `railway.toml`).
- **Puerto**: la app escucha en `${PORT}` (Railway lo inyecta). No hace falta
  configurarlo manualmente.
- **Healthcheck**: `GET /actuator/health` (público). Railway lo usa para saber
  cuándo el servicio está listo.
- **Migraciones**: Flyway corre automáticamente al arrancar
  (`validate-on-migrate=true`), aplicando `V1` … `V13`.

## Pasos

1. **Crear el proyecto en Railway** y conectar este repositorio de GitHub.

2. **Agregar un servicio PostgreSQL** (New → Database → PostgreSQL). Railway
   crea las variables `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`.

3. **Configurar las variables de entorno del servicio de la API**
   (Variables → RAW Editor). Ver `.env.example` para la lista completa. Las
   mínimas imprescindibles:

   ```
   DATABASE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   DATABASE_USERNAME=${{Postgres.PGUSER}}
   DATABASE_PASSWORD=${{Postgres.PGPASSWORD}}
   JWT_SECRET_KEY=<clave-aleatoria-de-32+-caracteres>
   SMTP_CIPHER_KEY=<clave-estable-para-cifrar-credenciales-smtp>
   APP_COOKIE_SECURE=true
   APP_COOKIE_SAMESITE=None
   APP_CORS_ALLOWED_ORIGINS=https://tu-frontend.com
   APP_FRONTEND_URL=https://tu-frontend.com
   RENIEC_TOKEN=<token-de-apisperu>
   APP_TIMEZONE=America/Lima
   ```

   > **Importante sobre `DATABASE_URL`**: debe comenzar con `jdbc:postgresql://`.
   > La variable `DATABASE_URL` que algunos add-ons exponen viene como
   > `postgresql://...` (sin `jdbc:`), que **no** sirve para el driver JDBC.
   > Por eso arriba se construye con plantillas a partir de las variables `PG*`.

4. **Deploy**. Railway construye la imagen y arranca el contenedor. Verifica los
   logs: Flyway debe aplicar las migraciones y Spring Boot levantar en el puerto
   asignado.

## Notas de seguridad para producción

- **Cookies cross-site**: si el frontend vive en otro dominio (p.ej. Vercel),
  las cookies de sesión necesitan `APP_COOKIE_SAMESITE=None` **y**
  `APP_COOKIE_SECURE=true` (solo viajan sobre HTTPS). Para mismo dominio, deja
  `SameSite=Lax`.
- **CORS**: `APP_CORS_ALLOWED_ORIGINS` debe listar exactamente el/los dominios
  del frontend (sin barra final), separados por coma. `allowCredentials` está
  activo, por lo que no se permite `*`.
- **Secretos**: `JWT_SECRET_KEY` y `SMTP_CIPHER_KEY` deben ser estables entre
  despliegues (si cambian, se invalidan los tokens y las credenciales SMTP
  cifradas, respectivamente).
- **Logs**: en producción baja el verbosity (`LOG_LEVEL_SQL=WARN`,
  `LOG_LEVEL_BIND=WARN`) para no exponer SQL/parámetros ni saturar logs.

## Prueba local con Docker

```bash
docker build -t hotel-sf-api .
docker run --rm -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/hotel_sf \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=1234 \
  -e JWT_SECRET_KEY=local_dev_secret_key_min_32_characters \
  hotel-sf-api
```
