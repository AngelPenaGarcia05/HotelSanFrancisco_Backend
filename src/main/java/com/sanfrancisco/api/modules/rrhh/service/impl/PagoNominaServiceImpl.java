package com.sanfrancisco.api.modules.rrhh.service.impl;

import com.sanfrancisco.api.exception.BusinessException;
import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.rrhh.dto.request.CalcularNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CambiarEstadoPagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.CreatePagoNominaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.PagoNominaFilterRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.CalculoNominaResponse;
import com.sanfrancisco.api.modules.rrhh.dto.response.PagoNominaResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import com.sanfrancisco.api.modules.rrhh.entity.Bono;
import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.TipoAsistencia;
import com.sanfrancisco.api.modules.rrhh.mapper.PagoNominaMapper;
import com.sanfrancisco.api.modules.rrhh.repository.AsistenciaRepository;
import com.sanfrancisco.api.modules.rrhh.repository.BonoRepository;
import com.sanfrancisco.api.modules.rrhh.repository.PagoNominaRepository;
import com.sanfrancisco.api.modules.rrhh.service.interfaces.PagoNominaService;
import com.sanfrancisco.api.modules.rrhh.specification.PagoNominaSpecification;
import com.sanfrancisco.api.modules.rrhh.websocket.PagoNominaEventPublisher;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class PagoNominaServiceImpl implements PagoNominaService {

    /** Jornada estándar en horas y multa fija por tardanza (política interna configurable). */
    private static final int HORAS_POR_DIA = 8;
    private static final BigDecimal MULTA_TARDANZA = new BigDecimal("5.00");

    private final PagoNominaRepository pagoNominaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final BonoRepository bonoRepository;
    private final PagoNominaMapper pagoNominaMapper;
    private final PagoNominaEventPublisher eventPublisher;

    public PagoNominaServiceImpl(PagoNominaRepository pagoNominaRepository,
                                 UsuarioRepository usuarioRepository,
                                 AsistenciaRepository asistenciaRepository,
                                 BonoRepository bonoRepository,
                                 PagoNominaMapper pagoNominaMapper,
                                 PagoNominaEventPublisher eventPublisher) {
        this.pagoNominaRepository = pagoNominaRepository;
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.bonoRepository = bonoRepository;
        this.pagoNominaMapper = pagoNominaMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PagoNominaResponse create(CreatePagoNominaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        PagoNomina entity = pagoNominaMapper.toEntity(request, usuario);
        PagoNomina saved = pagoNominaRepository.save(entity);
        eventPublisher.publishCreated(saved);
        return pagoNominaMapper.toResponse(saved);
    }

    @Override
    public PagoNominaResponse cambiarEstado(Integer pagoNominaId, CambiarEstadoPagoNominaRequest request) {
        PagoNomina pago = obtenerOFallar(pagoNominaId);
        EstadoNomina actual = pago.getEstado();
        EstadoNomina nuevo = request.nuevoEstado();

        if (actual == nuevo) {
            throw new BusinessException("El pago ya se encuentra en estado " + nuevo);
        }

        if (actual == EstadoNomina.ANULADO || actual == EstadoNomina.PAGADO) {
            throw new BusinessException("No se puede cambiar el estado de un pago ya PAGADO o ANULADO");
        }

        pago.setEstado(nuevo);
        
        // You could append 'motivo' to observations if such column existed,
        // but PagoNomina doesn't have an observaciones field in the DB schema by default.

        PagoNomina saved = pagoNominaRepository.save(pago);
        eventPublisher.publishStateChanged(saved);
        return pagoNominaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoNominaResponse findById(Integer pagoNominaId) {
        return pagoNominaMapper.toResponse(obtenerOFallar(pagoNominaId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PagoNominaResponse> search(PagoNominaFilterRequest filter, Pageable pageable) {
        return pagoNominaRepository.findAll(PagoNominaSpecification.build(filter), pageable)
                .map(pagoNominaMapper::toResponse);
    }

    @Override
    public void deleteById(Integer pagoNominaId) {
        PagoNomina pago = obtenerOFallar(pagoNominaId);
        if (pago.getEstado() == EstadoNomina.PAGADO) {
            throw new BusinessException("No se puede eliminar un pago que ya fue realizado");
        }
        pagoNominaRepository.delete(pago);
        eventPublisher.publishDeleted(pago.getPagoNominaId());
    }

    @Override
    @Transactional(readOnly = true)
    public CalculoNominaResponse calcularDesdeAsistencia(CalcularNominaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + request.usuarioId()));

        YearMonth ym;
        try {
            ym = YearMonth.parse(request.periodo());
        } catch (Exception e) {
            throw new BusinessException("Periodo inválido, use el formato YYYY-MM: " + request.periodo());
        }
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        BigDecimal sueldoBase = usuario.getSalario() != null ? usuario.getSalario() : BigDecimal.ZERO;

        // Base de cálculo: días laborables (Lun-Vie) del mes × jornada estándar.
        int diasLaborables = contarDiasLaborables(inicio, fin);
        BigDecimal horasEsperadas = BigDecimal.valueOf((long) diasLaborables * HORAS_POR_DIA);
        BigDecimal tarifaHora = horasEsperadas.signum() > 0
                ? sueldoBase.divide(horasEsperadas, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Incidencias reales tomadas de la asistencia del periodo.
        List<Asistencia> marcas = asistenciaRepository
                .findByUsuarioUsuarioIdAndFechaBetween(request.usuarioId(), inicio, fin);

        long faltasInjustificadas = marcas.stream()
                .filter(a -> a.getTipo() == TipoAsistencia.FALTA_INJUSTIFICADA).count();
        long tardanzas = marcas.stream()
                .filter(a -> a.getTipo() == TipoAsistencia.TARDANZA).count();
        BigDecimal horasReales = marcas.stream()
                .map(a -> a.getHorasTrabajadas() != null ? a.getHorasTrabajadas() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Descuentos derivados.
        BigDecimal descuentoFaltas = tarifaHora
                .multiply(BigDecimal.valueOf(HORAS_POR_DIA))
                .multiply(BigDecimal.valueOf(faltasInjustificadas))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuentoTardanzas = MULTA_TARDANZA
                .multiply(BigDecimal.valueOf(tardanzas))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDescuentos = descuentoFaltas.add(descuentoTardanzas);

        // Bonos ACTIVOS del empleado aún no liquidados en una nómina.
        BigDecimal totalBonos = bonoRepository.findByUsuarioUsuarioId(request.usuarioId()).stream()
                .filter(b -> b.getEstado() == EstadoBono.ACTIVO && b.getPagoNomina() == null)
                .map(Bono::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal montoNeto = sueldoBase.add(totalBonos).subtract(totalDescuentos)
                .setScale(2, RoundingMode.HALF_UP);

        return new CalculoNominaResponse(
                usuario.getUsuarioId(),
                buildNombreCompleto(usuario),
                request.periodo(),
                sueldoBase.setScale(2, RoundingMode.HALF_UP),
                diasLaborables,
                horasEsperadas.setScale(2, RoundingMode.HALF_UP),
                horasReales.setScale(2, RoundingMode.HALF_UP),
                tarifaHora,
                faltasInjustificadas,
                tardanzas,
                descuentoFaltas,
                descuentoTardanzas,
                totalDescuentos,
                totalBonos,
                montoNeto
        );
    }

    private int contarDiasLaborables(LocalDate inicio, LocalDate fin) {
        int dias = 0;
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                dias++;
            }
        }
        return dias;
    }

    private String buildNombreCompleto(Usuario u) {
        StringBuilder sb = new StringBuilder(u.getNombre()).append(' ').append(u.getApellidoPaterno());
        if (u.getApellidoMaterno() != null && !u.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(u.getApellidoMaterno());
        }
        return sb.toString();
    }

    private PagoNomina obtenerOFallar(Integer pagoNominaId) {
        return pagoNominaRepository.findById(pagoNominaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago de nómina no encontrado: " + pagoNominaId));
    }
}
