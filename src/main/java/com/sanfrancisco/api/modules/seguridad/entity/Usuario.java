package com.sanfrancisco.api.modules.seguridad.entity;

import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.shared.entity.AuditedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @NotBlank
    @Size(max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @NotBlank
    @Size(max = 80)
    @Column(name = "apellido_paterno", nullable = false, length = 80)
    private String apellidoPaterno;

    @Size(max = 80)
    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;

    @NotBlank
    @Size(max = 20)
    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @NotBlank
    @Size(max = 255)
    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private EstadoUsuario estado;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipoDocumento;

    // ── Campos laborales (opcionales — solo relevantes para roles de staff) ──

    @Size(max = 80)
    @Column(name = "cargo", length = 80)
    private String cargo;

    @Size(max = 80)
    @Column(name = "departamento", length = 80)
    private String departamento;

    @Size(max = 30)
    @Column(name = "codigo_empleado", length = 30, unique = true)
    private String codigoEmpleado;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @PositiveOrZero
    @Column(name = "salario", precision = 12, scale = 2)
    private BigDecimal salario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario that)) return false;
        return usuarioId != null && usuarioId.equals(that.usuarioId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
