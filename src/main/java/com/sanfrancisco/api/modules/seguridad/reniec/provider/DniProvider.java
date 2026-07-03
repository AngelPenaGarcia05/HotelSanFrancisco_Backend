package com.sanfrancisco.api.modules.seguridad.reniec.provider;

import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;

import java.util.Optional;

/**
 * Proveedor externo de consulta de identidad por DNI.
 * <p>
 * Contrato para la cadena de fallback: el orquestador recorre los proveedores
 * en orden ({@code @Order}) y pasa al siguiente tanto si el DNI no existe en el
 * padrón del proveedor como si este falla técnicamente.
 */
public interface DniProvider {

    /** Nombre corto del proveedor, solo para logs y diagnóstico. */
    String nombre();

    /**
     * Indica si el proveedor puede usarse (habilitado por configuración y con
     * token presente). Los proveedores no disponibles se saltan sin error.
     */
    boolean disponible();

    /**
     * Consulta el DNI en el proveedor.
     *
     * @param dni DNI ya validado (8 dígitos)
     * @return datos normalizados, o vacío si el proveedor respondió que el DNI
     *         no existe en su padrón
     * @throws DniProviderException ante fallas técnicas (timeout, token
     *         inválido, error HTTP inesperado), para que la cadena continúe
     */
    Optional<ReniecConsultaResponse> consultar(String dni);
}
