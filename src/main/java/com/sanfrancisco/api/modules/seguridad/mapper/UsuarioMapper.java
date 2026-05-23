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
                entity.getFechaModificacion()
        );
    }
}
