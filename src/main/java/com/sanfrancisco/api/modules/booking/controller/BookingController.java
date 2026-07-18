package com.sanfrancisco.api.modules.booking.controller;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.ConfirmarPagoRequest;
import com.sanfrancisco.api.modules.booking.dto.CreateBookingRequest;
import com.sanfrancisco.api.modules.booking.dto.CrearSesionPagoResponse;
import com.sanfrancisco.api.modules.booking.dto.HabitacionDisponibleResponse;
import com.sanfrancisco.api.modules.booking.dto.MetodoPagoPublicoResponse;
import com.sanfrancisco.api.modules.booking.service.BookingPaymentService;
import com.sanfrancisco.api.modules.booking.service.BookingService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import com.sanfrancisco.api.shared.exception.ConflictException;
import com.sanfrancisco.api.shared.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final BookingPaymentService bookingPaymentService;
    private final String frontendUrl;

    public BookingController(BookingService bookingService,
                             BookingPaymentService bookingPaymentService,
                             @Value("${app.frontend-url}") String frontendUrl) {
        this.bookingService = bookingService;
        this.bookingPaymentService = bookingPaymentService;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/disponibles")
    public ApiResponse<List<HabitacionDisponibleResponse>> disponibles(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer personas) {
        return ApiResponse.ok(bookingService.findDisponibles(fechaInicio, fechaFin, personas));
    }

    @GetMapping("/metodos-pago")
    public ApiResponse<List<MetodoPagoPublicoResponse>> metodosPago() {
        return ApiResponse.ok(bookingService.findMetodosPago());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingConfirmationResponse>> crearReserva(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingConfirmationResponse confirmation = bookingService.crearReserva(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(confirmation, "Reserva creada exitosamente"));
    }

    /** Sesión de checkout Niubiz para una pre-reserva PENDIENTE (pago online). */
    @PostMapping("/{reservaId}/pago/session")
    public ApiResponse<CrearSesionPagoResponse> crearSesionPago(
            @PathVariable Integer reservaId, HttpServletRequest request) {
        // forward-headers-strategy=framework: getRemoteAddr() ya es la IP real del cliente.
        return ApiResponse.ok(bookingPaymentService.crearSesionPago(reservaId, request.getRemoteAddr()));
    }

    /** Autoriza el cobro con el token del checkout y confirma la reserva. */
    @PostMapping("/pago/confirmar")
    public ApiResponse<BookingConfirmationResponse> confirmarPago(
            @Valid @RequestBody ConfirmarPagoRequest request) {
        return ApiResponse.ok(bookingPaymentService.confirmarPago(request), "Pago confirmado");
    }

    /**
     * Retorno del checkout de Niubiz: el formulario del lightbox hace un POST
     * clásico (form-urlencoded) con el transactionToken a esta URL. Se autoriza
     * el cobro y se redirige al frontend con el resultado en query params,
     * porque la SPA pierde su estado en esta navegación.
     */
    @PostMapping(value = "/pago/retorno/{purchaseNumber}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> retornoCheckout(
            @PathVariable String purchaseNumber,
            @RequestParam("transactionToken") String transactionToken,
            // El mismo checkout sirve al booking público y al dashboard de staff;
            // origen decide a qué pantalla del frontend se vuelve con el resultado.
            @RequestParam(value = "origen", required = false) String origen) {
        String base;
        if ("dashboard".equalsIgnoreCase(origen)) {
            base = "/reservations/mis-reservas";  // <-- ruta correcta del dashboard
        } else {
            base = "/booking";
        }
        String destino;
        try {
            bookingPaymentService.confirmarPago(new ConfirmarPagoRequest(purchaseNumber, transactionToken));
            destino = base + "?pago=exito&purchase=" + encode(purchaseNumber);
        } catch (ValidationException | ConflictException e) {
            destino = base + "?pago=rechazado&msg=" + encode(e.getMessage());
        } catch (ResponseStatusException e) {
            destino = base + "?pago=error";
        } catch (Exception e) {
            log.error("Error inesperado en el retorno del checkout {}: {}", purchaseNumber, e.getMessage(), e);
            destino = base + "?pago=error";
        }
        // 303: el navegador convierte el POST del formulario en un GET al frontend.
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(frontendUrl + destino))
                .build();
    }

    /** Confirmación de una transacción AUTORIZADA (la consulta la SPA tras el redirect). */
    @GetMapping("/pago/{purchaseNumber}/confirmacion")
    public ApiResponse<BookingConfirmationResponse> confirmacionPago(@PathVariable String purchaseNumber) {
        return ApiResponse.ok(bookingPaymentService.confirmacionPorPurchaseNumber(purchaseNumber));
    }

    private String encode(String valor) {
        return URLEncoder.encode(valor != null ? valor : "", StandardCharsets.UTF_8);
    }
}
