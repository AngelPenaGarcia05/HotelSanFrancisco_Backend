package com.sanfrancisco.api.modules.notificaciones.entity;

import com.sanfrancisco.api.modules.notificaciones.enums.EmailTemplateKey;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "plantillas_correo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaCorreo extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plantilla_id")
    private Integer plantillaId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "clave", nullable = false, unique = true, length = 40)
    private EmailTemplateKey clave;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Size(max = 200)
    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @NotBlank
    @Column(name = "cuerpo_html", nullable = false, columnDefinition = "TEXT")
    private String cuerpoHtml;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlantillaCorreo that)) return false;
        return plantillaId != null && plantillaId.equals(that.plantillaId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

