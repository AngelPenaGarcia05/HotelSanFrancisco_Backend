package com.sanfrancisco.api.modules.seguridad.mapper;

import com.sanfrancisco.api.modules.seguridad.dto.request.CreateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.request.UpdateUsuarioRequest;
import com.sanfrancisco.api.modules.seguridad.dto.response.UsuarioResponse;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.modules.seguridad.entity.TipoDocumento;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    private final PasswordEncoder passwordEncoder;

    public UsuarioMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario toEntity(CreateUsuarioRequest request, Rol rol, TipoDocumento tipoDocumento) {
        if (request == null) return null;
        return Usuario.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .numeroDocumento(request.numeroDocumento())
                .correo(request.correo())
                .telefono(request.telefono())
                .fechaNacimiento(request.fechaNacimiento())
                .contrasenaHash(passwordEncoder.encode(request.contrasena()))
                .estado(request.estado())
                .rol(rol)
                .tipoDocumento(tipoDocumento)
                .cargo(request.cargo())
                .departamento(request.departamento())
                .codigoEmpleado(request.codigoEmpleado())
                .fechaIngreso(request.fechaIngreso())
                .salario(request.salario())
                .build();
    }

    public void updateEntity(Usuario target, UpdateUsuarioRequest request, Rol rol, TipoDocumento tipoDocumento) {
        if (request == null || target == null) return;
        if (request.nombre() != null) target.setNombre(request.nombre());
        if (request.apellidoPaterno() != null) target.setApellidoPaterno(request.apellidoPaterno());
        if (request.apellidoMaterno() != null) target.setApellidoMaterno(request.apellidoMaterno());
        if (request.numeroDocumento() != null) target.setNumeroDocumento(request.numeroDocumento());
        if (request.correo() != null) target.setCorreo(request.correo());
        if (request.telefono() != null) target.setTelefono(request.telefono());
        if (request.fechaNacimiento() != null) target.setFechaNacimiento(request.fechaNacimiento());
        if (request.contrasena() != null && !request.contrasena().isBlank()) {
            target.setContrasenaHash(passwordEncoder.encode(request.contrasena()));
        }
        if (request.estado() != null) target.setEstado(request.estado());
        if (rol != null) target.setRol(rol);
        if (tipoDocumento != null) target.setTipoDocumento(tipoDocumento);
        if (request.cargo() != null) target.setCargo(request.cargo());
        if (request.departamento() != null) target.setDepartamento(request.departamento());
        if (request.codigoEmpleado() != null) target.setCodigoEmpleado(request.codigoEmpleado());
        if (request.fechaIngreso() != null) target.setFechaIngreso(request.fechaIngreso());
        if (request.salario() != null) target.setSalario(request.salario());
    }

    public UsuarioResponse toResponse(Usuario entity) {
        if (entity == null) return null;
        Rol r = entity.getRol();
        TipoDocumento td = entity.getTipoDocumento();

        String apellidoMaternoStr = entity.getApellidoMaterno() != null ? " " + entity.getApellidoMaterno() : "";
        String nombreCompleto = entity.getNombre() + " " + entity.getApellidoPaterno() + apellidoMaternoStr;

        return new UsuarioResponse(
                entity.getUsuarioId(),
                entity.getNombre(),
                entity.getApellidoPaterno(),
                entity.getApellidoMaterno(),
                nombreCompleto,
                entity.getNumeroDocumento(),
                entity.getCorreo(),
                entity.getTelefono(),
                entity.getFechaNacimiento(),
                entity.getEstado(),
                r != null ? r.getRolId() : null,
                r != null ? r.getNombre() : null,
                td != null ? td.getTipoDocumentoId() : null,
                td != null ? td.getAcronimo() : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion(),
                entity.getCargo(),
                entity.getDepartamento(),
                entity.getCodigoEmpleado(),
                entity.getFechaIngreso(),
                entity.getSalario()
        );
    }
}
