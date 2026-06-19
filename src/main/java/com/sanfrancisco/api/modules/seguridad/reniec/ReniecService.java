package com.sanfrancisco.api.modules.seguridad.reniec;

import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;

import java.util.Optional;

public interface ReniecService {

    /**
     * Consulta los datos de identidad de un DNI en RENIEC (vía apisperu.com).
     * Lanza {@code BusinessException} si el DNI es inválido, no existe o el
     * proveedor no está disponible. Pensado para el endpoint público de consulta.
     *
     * @param dni número de DNI de 8 dígitos
     * @return datos de identidad
     */
    ReniecConsultaResponse consultarDni(String dni);

    /**
     * Variante tolerante a fallos para enriquecer datos durante el registro.
     * Nunca lanza excepción: si el DNI es inválido o el proveedor falla,
     * devuelve {@code Optional.empty()} y el flujo de registro continúa.
     *
     * @param dni número de DNI de 8 dígitos
     * @return datos de identidad si la consulta fue exitosa
     */
    Optional<ReniecConsultaResponse> consultarDniSilencioso(String dni);
}
