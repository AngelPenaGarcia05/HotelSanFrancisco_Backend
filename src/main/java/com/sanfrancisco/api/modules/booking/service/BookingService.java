package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.CreateBookingRequest;
import com.sanfrancisco.api.modules.booking.dto.HabitacionDisponibleResponse;
import com.sanfrancisco.api.modules.booking.dto.MetodoPagoPublicoResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    List<HabitacionDisponibleResponse> findDisponibles(LocalDate fechaInicio, LocalDate fechaFin, Integer personas);

    List<MetodoPagoPublicoResponse> findMetodosPago();

    BookingConfirmationResponse crearReserva(CreateBookingRequest request);
}
