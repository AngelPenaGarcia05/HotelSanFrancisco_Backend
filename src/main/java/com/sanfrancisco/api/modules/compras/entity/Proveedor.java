package com.sanfrancisco.api.modules.compras.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proveedor_id")
    private Integer proveedorId;

    @Size(max = 20)
    @Column(name = "ruc_nit_cif", length = 20)
    private String rucNitCif;

    @NotBlank
    @Size(max = 200)
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Size(max = 100)
    @Column(name = "contacto_nombre", length = 100)
    private String contactoNombre;

    @Size(max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Email
    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 300)
    @Column(name = "direccion", length = 300)
    private String direccion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proveedor that)) return false;
        return proveedorId != null && proveedorId.equals(that.proveedorId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
