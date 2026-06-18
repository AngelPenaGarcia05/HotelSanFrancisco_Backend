package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateClienteRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ClienteResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Huesped toEntity(CreateClienteRequest request, Usuario usuario) {
        return Huesped.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .numeroDocumento(request.numeroDocumento())
                .nacionalidad(request.nacionalidad())
                .correo(request.correo())
                .telefono(request.telefono())
                .estado(request.estado())
                .usuario(usuario)
                .build();
    }

    public void updateEntity(Huesped target, UpdateClienteRequest request, Usuario usuario) {
        if (request.nombre() != null)           target.setNombre(request.nombre());
        if (request.apellidoPaterno() != null)  target.setApellidoPaterno(request.apellidoPaterno());
        if (request.apellidoMaterno() != null)  target.setApellidoMaterno(request.apellidoMaterno());
        if (request.numeroDocumento() != null)  target.setNumeroDocumento(request.numeroDocumento());
        if (request.nacionalidad() != null)     target.setNacionalidad(request.nacionalidad());
        if (request.correo() != null)           target.setCorreo(request.correo());
        if (request.telefono() != null)         target.setTelefono(request.telefono());
        if (request.estado() != null)           target.setEstado(request.estado());
        if (usuario != null)                    target.setUsuario(usuario);
    }

    public ClienteResponse toResponse(Huesped entity) {
        String nombreCompleto = entity.getNombre() + " " + entity.getApellidoPaterno()
                + (entity.getApellidoMaterno() != null ? " " + entity.getApellidoMaterno() : "");

        String usuarioNombre = null;
        Integer usuarioId = null;
        if (entity.getUsuario() != null) {
            usuarioId = entity.getUsuario().getUsuarioId();
            usuarioNombre = entity.getUsuario().getNombre() + " " + entity.getUsuario().getApellidoPaterno();
        }

        return new ClienteResponse(
                entity.getHuespedId(),
                entity.getNombre(),
                entity.getApellidoPaterno(),
                entity.getApellidoMaterno(),
                nombreCompleto.trim(),
                entity.getNumeroDocumento(),
                entity.getNacionalidad(),
                entity.getCorreo(),
                entity.getTelefono(),
                entity.getEstado(),
                usuarioId,
                usuarioNombre,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
