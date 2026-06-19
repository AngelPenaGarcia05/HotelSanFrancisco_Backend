package com.sanfrancisco.api.modules.seguridad.reniec;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.modules.seguridad.dto.response.ReniecConsultaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReniecServiceImpl implements ReniecService {

    private static final Logger log = LoggerFactory.getLogger(ReniecServiceImpl.class);

    private final ReniecClient reniecClient;

    public ReniecServiceImpl(ReniecClient reniecClient) {
        this.reniecClient = reniecClient;
    }

    @Override
    public ReniecConsultaResponse consultarDni(String dni) {
        return reniecClient.fetch(normalizarDni(dni));
    }

    @Override
    public Optional<ReniecConsultaResponse> consultarDniSilencioso(String dni) {
        if (!esDniValido(dni)) {
            return Optional.empty();
        }
        try {
            return Optional.of(reniecClient.fetch(dni.trim()));
        } catch (Exception e) {
            // Degradación elegante: el registro no debe fallar si RENIEC no responde.
            log.info("Enriquecimiento RENIEC omitido para DNI {}: {}", dni, e.getMessage());
            return Optional.empty();
        }
    }

    private String normalizarDni(String dni) {
        String norm = dni == null ? "" : dni.trim();
        if (!norm.matches("\\d{8}")) {
            throw new BusinessException("El DNI debe contener exactamente 8 dígitos numéricos.");
        }
        return norm;
    }

    private boolean esDniValido(String dni) {
        return dni != null && dni.trim().matches("\\d{8}");
    }
}
