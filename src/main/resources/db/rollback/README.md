# Rollbacks manuales de migraciones

Flyway (edición Community) solo aplica migraciones hacia adelante: no sabe
deshacerlas. Esta carpeta guarda el **SQL inverso documentado** de cada
migración, para poder revertirla a mano en una emergencia sin improvisar.

**Flyway NO ejecuta nada de esta carpeta** (queda fuera de
`spring.flyway.locations`, que apunta solo a `db/migration`). Son scripts de
referencia para ejecutar manualmente con `psql`.

## Convención

Por cada migración nueva `VNN__descripcion.sql` en `db/migration`, crear aquí
su contraparte `VNN_rollback.sql` con:

1. Las sentencias inversas en **orden inverso** al de la migración
   (lo último que se creó es lo primero que se revierte).
2. Un comentario inicial indicando qué revierte y qué pasos son
   irreversibles (p. ej. datos borrados que no se pueden restaurar).
3. El recordatorio de limpiar el historial de Flyway tras revertir:

   ```sql
   DELETE FROM flyway_schema_history WHERE version = 'NN';
   ```

## Cómo revertir una migración (ejemplo con V31)

```bash
psql -h <host> -U <usuario> -d hotel_sf -f V31_rollback.sql
psql -h <host> -U <usuario> -d hotel_sf \
     -c "DELETE FROM flyway_schema_history WHERE version = '31';"
```

Tras esto, el esquema y el historial quedan como antes de la migración y el
backend puede arrancar con la versión anterior del código.

## Historial

| Migración | Rollback | Notas |
|---|---|---|
| V31__indices_fk_y_constraints.sql | V31_rollback.sql | La limpieza de `smtp_config.password_cifrado` es irreversible (valor no conservado; el correo no depende de él). |
| V32__unique_numero_documento.sql | V32_rollback.sql | Reversión completa; los constraints no alteran datos. |

Las migraciones V1–V30 son anteriores a esta convención y no tienen rollback
escrito; de necesitarse, derivarlo del propio archivo de migración.
