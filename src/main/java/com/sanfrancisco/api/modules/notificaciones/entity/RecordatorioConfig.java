package com.sanfrancisco.api.modules.notificaciones.entity;

import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "recordatorio_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordatorioConfig extends AuditedEntity {

    @Id
    @Column(name = "recordatorio_config_id")
    private Integer recordatorioConfigId;

    @NotNull
    @Column(name = "horas_antes_checkin", nullable = false)
    private Integer horasAntesCheckin;

    @NotNull
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado;

    @NotBlank
    @Size(max = 5)
    @Column(name = "hora_envio", nullable = false, length = 5)
    private String horaEnvio;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordatorioConfig that)) return false;
        return recordatorioConfigId != null && recordatorioConfigId.equals(that.recordatorioConfigId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
