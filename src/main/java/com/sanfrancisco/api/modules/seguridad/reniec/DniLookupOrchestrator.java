package com.sanfrancisco.api.modules.seguridad.reniec;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import com.sanfrancisco.api.modules.seguridad.reniec.provider.DniProvider;
import com.sanfrancisco.api.modules.seguridad.reniec.provider.DniProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Orquesta la cadena de proveedores de DNI con fallback automático: recorre los
 * proveedores en orden ({@code @Order}) y pasa al siguiente tanto si el DNI no
 * existe en el padrón del proveedor como si este falla técnicamente (timeout,
 * token inválido, HTTP 5xx).
 * <p>
 * Se aísla en un bean propio para que el cache {@code @Cacheable} opere a través
 * del proxy de Spring incluso cuando {@link ReniecServiceImpl} invoca la consulta
 * desde sus distintos métodos (la auto-invocación dentro del mismo bean omitiría
 * el cache). Solo se cachea el resultado exitoso final, venga del proveedor que
 * venga; cualquier fallo lanza {@link BusinessException} y no se almacena.
 */
@Component
public class DniLookupOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DniLookupOrchestrator.class);

    private final List<DniProvider> providers;

    public DniLookupOrchestrator(List<DniProvider> providers) {
        this.providers = providers;
    }

    /**
     * @param dni DNI ya validado (8 dígitos)
     */
    @Cacheable(value = "reniecDni", key = "#dni")
    public ReniecConsultaResponse fetch(String dni) {
        boolean algunoDisponible = false;
        boolean algunoRespondioNoEncontrado = false;

        for (DniProvider provider : providers) {
            if (!provider.disponible()) {
                continue;
            }
            algunoDisponible = true;
            try {
                Optional<ReniecConsultaResponse> resultado = provider.consultar(dni);
                if (resultado.isPresent()) {
                    return resultado.get();
                }
                algunoRespondioNoEncontrado = true;
                log.info("Proveedor {} no encontró el DNI {}; se intenta el siguiente.", provider.nombre(), dni);
            } catch (DniProviderException e) {
                log.warn("Proveedor {} falló para DNI {} ({}); se intenta el siguiente.",
                        provider.nombre(), dni, e.getMessage());
            }
        }

        if (!algunoDisponible) {
            log.error("Consulta de DNI solicitada pero no hay ningún proveedor disponible/configurado.");
            throw new BusinessException("El servicio de consulta RENIEC no está disponible en este momento.");
        }
        if (algunoRespondioNoEncontrado) {
            throw new BusinessException("No se encontraron datos para el DNI " + dni + " en RENIEC.");
        }
        throw new BusinessException("El servicio de consulta RENIEC no está disponible en este momento.");
    }
}
