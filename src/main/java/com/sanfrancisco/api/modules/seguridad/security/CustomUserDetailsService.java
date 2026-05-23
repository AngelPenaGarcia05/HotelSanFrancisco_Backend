package com.sanfrancisco.api.modules.seguridad.security;

import com.sanfrancisco.api.modules.seguridad.entity.DetalleRol;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.DetalleRolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final DetalleRolRepository detalleRolRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository, DetalleRolRepository detalleRolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.detalleRolRepository = detalleRolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add Role
        if (usuario.getRol() != null) {
            String roleName = usuario.getRol().getNombre().toUpperCase();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            authorities.add(new SimpleGrantedAuthority(roleName));

            // Fetch and Add Permissions for this Role
            List<DetalleRol> detalles = detalleRolRepository.findByRolRolId(usuario.getRol().getRolId());
            for (DetalleRol detalle : detalles) {
                if (detalle.getPermiso() != null) {
                    authorities.add(new SimpleGrantedAuthority(detalle.getPermiso().getCodigo()));
                }
            }
        }

        return new CustomUserDetails(usuario, authorities);
    }
}
