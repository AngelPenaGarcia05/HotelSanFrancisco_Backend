-- Modalidad de pago inicial de la reserva (PARCIAL = 50%, TOTAL = 100%).
-- Se persiste para poder re-derivar el adelanto cuando cambia el total al editar.
ALTER TABLE reservas ADD COLUMN tipo_pago VARCHAR(10);

-- Backfill: las reservas existentes se asumen con adelanto parcial.
UPDATE reservas SET tipo_pago = 'PARCIAL' WHERE tipo_pago IS NULL;
