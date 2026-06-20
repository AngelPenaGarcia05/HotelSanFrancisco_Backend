package com.sanfrancisco.api.modules.booking.controller;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.CreateBookingRequest;
import com.sanfrancisco.api.modules.booking.dto.HabitacionDisponibleResponse;
import com.sanfrancisco.api.modules.booking.dto.MetodoPagoPublicoResponse;
import com.sanfrancisco.api.modules.booking.service.BookingService;
import com.sanfrancisco.api.shared.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
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
}
