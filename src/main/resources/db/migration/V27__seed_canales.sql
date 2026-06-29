INSERT INTO canales (canal_id, nombre, estado, fecha_creacion, fecha_modificacion)
VALUES
    (1, 'Directa', 'ACTIVO', NOW(), NOW()),
    (2, 'Booking', 'ACTIVO', NOW(), NOW()),
    (3, 'Online',  'ACTIVO', NOW(), NOW())
ON CONFLICT (canal_id) DO NOTHING;

-- Sincronizar la secuencia para que el próximo auto-id no colisione
SELECT setval('canales_canal_id_seq', (SELECT MAX(canal_id) FROM canales));
