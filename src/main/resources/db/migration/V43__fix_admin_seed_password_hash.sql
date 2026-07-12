-- =============================================================================
-- V43__fix_admin_seed_password_hash.sql
-- Corrige el hash de contraseña de los usuarios admin sembrados en V4 y V20.
-- =============================================================================
-- El hash BCrypt de esos seeds ($2a$10$gR5x...) NO corresponde a 'admin123'
-- (verificado con BCryptPasswordEncoder.matches → false), por lo que la
-- credencial documentada nunca funcionó en bases creadas desde cero.
-- Se reemplaza por un hash verificado de 'admin123'. Solo se actualizan los
-- usuarios que aún conservan el hash roto original, para no pisar contraseñas
-- cambiadas legítimamente después.
-- =============================================================================

UPDATE usuarios
SET contrasena_hash = '$2a$10$jIcMXBpxcP4X0aGuX0xbnO43kLQYZ2q4eghDPUOd5jd5JWDxqbFWq'
WHERE contrasena_hash = '$2a$10$gR5xG29Geqs1Gf9iV1/DCO8EpyqgYmZ54w2xT1gM6hK/P81aLwV3G';
