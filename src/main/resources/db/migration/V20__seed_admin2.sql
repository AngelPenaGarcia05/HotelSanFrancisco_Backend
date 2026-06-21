-- Segundo usuario administrador
-- Contraseña: admin123 (BCrypt hash)
INSERT INTO usuarios (nombre, apellido_paterno, apellido_materno, numero_documento, correo, telefono, fecha_nacimiento, contrasena_hash, estado, fecha_creacion, rol_id, tipo_documento_id)
VALUES (
    'Admin',
    'Dos',
    'Sistema',
    '87654321',
    'admin2@sanfrancisco.com',
    '987654322',
    '1990-01-01',
    '$2a$10$gR5xG29Geqs1Gf9iV1/DCO8EpyqgYmZ54w2xT1gM6hK/P81aLwV3G',
    'ACTIVO',
    NOW(),
    1,
    1
);
