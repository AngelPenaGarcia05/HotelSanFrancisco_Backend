package com.sanfrancisco.api.modules.servicios.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.repository.EstanciaRepository;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import com.sanfrancisco.api.modules.servicios.dto.request.CreatePedidoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.request.RechazarPedidoServicioRequest;
import com.sanfrancisco.api.modules.servicios.dto.response.PedidoServicioResponse;
import com.sanfrancisco.api.modules.servicios.entity.PedidoServicio;
import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import com.sanfrancisco.api.modules.servicios.enums.EstadoPedidoServicio;
import com.sanfrancisco.api.modules.servicios.mapper.PedidoServicioMapper;
import com.sanfrancisco.api.modules.servicios.repository.PedidoServicioRepository;
import com.sanfrancisco.api.modules.servicios.repository.ServicioRepository;
import com.sanfrancisco.api.modules.servicios.repository.TipoServicioRepository;
import com.sanfrancisco.api.modules.servicios.service.interfaces.PedidoServicioService;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PedidoServicioServiceImpl implements PedidoServicioService {

    private final PedidoServicioRepository pedidoRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EstanciaRepository estanciaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoServicioMapper mapper;

    public PedidoServicioServiceImpl(PedidoServicioRepository pedidoRepository,
                                     TipoServicioRepository tipoServicioRepository,
                                     EstanciaRepository estanciaRepository,
                                     ServicioRepository servicioRepository,
                                     UsuarioRepository usuarioRepository,
                                     PedidoServicioMapper mapper) {
        this.pedidoRepository = pedidoRepository;
        this.tipoServicioRepository = tipoServicioRepository;
        this.estanciaRepository = estanciaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
    }

    // =========================================================================
    // CLIENTE
    // =========================================================================

    @Override
    public PedidoServicioResponse crear(CreatePedidoServicioRequest request) {
        Integer usuarioId = currentUserId();

        // La estancia activa (check-in hecho, sin check-out) ata el pedido al huésped.
        List<Estancia> activas = estanciaRepository
                .findByReservaUsuarioUsuarioIdAndFechaCheckoutIsNull(usuarioId);
        if (activas.isEmpty()) {
            throw new BusinessException("No tienes una estadía activa. Solo puedes pedir "
                    + "servicios durante tu estancia (con check-in realizado).");
        }
        Estancia estancia = activas.get(0);

        TipoServicio tipoServicio = tipoServicioRepository.findById(request.tipoServicioId())
                .orElseThrow(() -> new ResourceNotFoundException("TipoServicio", request.tipoServicioId()));
        if (tipoServicio.getEstado() != EstadoActivo.ACTIVO) {
            throw new BusinessException("El servicio seleccionado no está disponible.");
        }

        PedidoServicio pedido = PedidoServicio.builder()
                .tipoServicio(tipoServicio)
                .estancia(estancia)
                .cantidad(request.cantidad())
                .observaciones(request.observaciones())
                .estado(EstadoPedidoServicio.PENDIENTE)
                .build();

        return mapper.toResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoServicioResponse> getMisPedidos() {
        Integer usuarioId = currentUserId();
        return pedidoRepository
                .findByEstanciaReservaUsuarioUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public PedidoServicioResponse cancelar(Integer pedidoServicioId) {
        Integer usuarioId = currentUserId();
        // 404 (no 403) si no es del cliente, para no revelar pedidos ajenos.
        PedidoServicio pedido = pedidoRepository.findById(pedidoServicioId)
                .filter(p -> esDelCliente(p, usuarioId))
                .orElseThrow(() -> new ResourceNotFoundException("PedidoServicio", pedidoServicioId));

        if (pedido.getEstado() != EstadoPedidoServicio.PENDIENTE) {
            throw new BusinessException("Solo puedes cancelar un pedido que siga pendiente.");
        }
        pedido.setEstado(EstadoPedidoServicio.CANCELADO);
        pedido.setFechaRespuesta(LocalDateTime.now());
        return mapper.toResponse(pedidoRepository.save(pedido));
    }

    // =========================================================================
    // RECEPCIÓN
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoServicioResponse> buscar(EstadoPedidoServicio estado, Pageable pageable) {
        Page<PedidoServicio> page = (estado != null)
                ? pedidoRepository.findByEstado(estado, pageable)
                : pedidoRepository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    @Override
    public PedidoServicioResponse aprobar(Integer pedidoServicioId) {
        PedidoServicio pedido = obtenerPendiente(pedidoServicioId);

        // Aprobar genera el consumo facturable ligado a la estancia.
        TipoServicio tipo = pedido.getTipoServicio();
        BigDecimal precioAplicado = tipo.getCostoBase();
        BigDecimal subtotal = pedido.getCantidad().multiply(precioAplicado);

        Servicio servicio = Servicio.builder()
                .tipoServicio(tipo)
                .estancia(pedido.getEstancia())
                .cantidad(pedido.getCantidad())
                .precioAplicado(precioAplicado)
                .subtotal(subtotal)
                .observaciones(pedido.getObservaciones())
                .fechaConsumo(LocalDateTime.now())
                .build();
        servicio = servicioRepository.save(servicio);

        pedido.setEstado(EstadoPedidoServicio.APROBADO);
        pedido.setServicio(servicio);
        pedido.setUsuarioRespuesta(usuarioActual());
        pedido.setFechaRespuesta(LocalDateTime.now());
        return mapper.toResponse(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoServicioResponse rechazar(Integer pedidoServicioId, RechazarPedidoServicioRequest request) {
        PedidoServicio pedido = obtenerPendiente(pedidoServicioId);
        pedido.setEstado(EstadoPedidoServicio.RECHAZADO);
        pedido.setMotivoRespuesta(request.motivo());
        pedido.setUsuarioRespuesta(usuarioActual());
        pedido.setFechaRespuesta(LocalDateTime.now());
        return mapper.toResponse(pedidoRepository.save(pedido));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private PedidoServicio obtenerPendiente(Integer pedidoServicioId) {
        PedidoServicio pedido = pedidoRepository.findById(pedidoServicioId)
                .orElseThrow(() -> new ResourceNotFoundException("PedidoServicio", pedidoServicioId));
        if (pedido.getEstado() != EstadoPedidoServicio.PENDIENTE) {
            throw new BusinessException("El pedido ya fue atendido (estado actual: " + pedido.getEstado() + ").");
        }
        return pedido;
    }

    private boolean esDelCliente(PedidoServicio p, Integer usuarioId) {
        return p.getEstancia() != null
                && p.getEstancia().getReserva() != null
                && p.getEstancia().getReserva().getUsuario() != null
                && usuarioId.equals(p.getEstancia().getReserva().getUsuario().getUsuarioId());
    }

    private Usuario usuarioActual() {
        return usuarioRepository.getReferenceById(currentUserId());
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }
        return principal.userId();
    }
}
