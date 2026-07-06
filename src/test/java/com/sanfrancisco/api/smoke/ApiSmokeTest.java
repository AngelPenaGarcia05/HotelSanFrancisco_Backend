package com.sanfrancisco.api.smoke;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoUsuario;
import com.sanfrancisco.api.modules.seguridad.repository.RolRepository;
import com.sanfrancisco.api.modules.seguridad.repository.TipoDocumentoRepository;
import com.sanfrancisco.api.modules.seguridad.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test de integración: levanta la aplicación completa contra la BD local
 * (la misma que usan los demás tests @SpringBootTest) y verifica que los
 * endpoints reales respondan lo esperado. Detecta regresiones entre módulos
 * que los tests unitarios no ven (p.ej. booking roto por un cambio en reservas).
 *
 * Credenciales: NO depende de los usuarios seed (sus contraseñas pueden variar
 * entre entornos). El test crea/actualiza su propio admin de prueba vía
 * repositorio y se loguea por HTTP real.
 *
 * Ejecución: .\mvnw.cmd test  (solo requiere Postgres local activo)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Smoke test de la API")
class ApiSmokeTest {

    private static final String SMOKE_ADMIN_CORREO = "smoke.admin@test.local";
    private static final String SMOKE_ADMIN_PASSWORD = "SmokeTest123!";

    @org.springframework.boot.test.web.server.LocalServerPort
    int port;

    // Boot 4 ya no autoconfigura el bean TestRestTemplate; se instancia a mano.
    private final TestRestTemplate rest = new TestRestTemplate();

    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String cookie;

    private String url(String ruta) {
        return "http://localhost:" + port + ruta;
    }

    @BeforeAll
    void loginComoAdminDePrueba() {
        upsertAdminDePrueba();

        ResponseEntity<String> res = rest.postForEntity(url("/auth/login"),
                Map.of("correo", SMOKE_ADMIN_CORREO, "contrasena", SMOKE_ADMIN_PASSWORD),
                String.class);
        assertThat(res.getStatusCode().is2xxSuccessful())
                .as("login del admin de prueba (¿BD local activa y migrada?)")
                .isTrue();
        // El backend emite access_token y refresh_token como cookies HttpOnly.
        cookie = String.join("; ", res.getHeaders().getOrDefault(HttpHeaders.SET_COOKIE, java.util.List.of()));
        assertThat(cookie).contains("access_token");
    }

    /** Crea el admin de prueba si no existe; si existe, se asegura de que la contraseña sea la esperada. */
    private void upsertAdminDePrueba() {
        var rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Falta el rol ADMIN del seed"));
        var tipoDoc = tipoDocumentoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta el seed de tipos de documento"));

        Usuario admin = usuarioRepository.findByCorreo(SMOKE_ADMIN_CORREO).orElseGet(() -> Usuario.builder()
                .nombre("Smoke")
                .apellidoPaterno("Admin")
                .apellidoMaterno("Test")
                .numeroDocumento("00000099")
                .correo(SMOKE_ADMIN_CORREO)
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .rol(rolAdmin)
                .tipoDocumento(tipoDoc)
                .build());
        admin.setContrasenaHash(passwordEncoder.encode(SMOKE_ADMIN_PASSWORD));
        admin.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(admin);
    }

    // ------------------------------------------------------------------
    // Helpers HTTP
    // ------------------------------------------------------------------

    private ResponseEntity<String> get(String ruta) {
        return rest.exchange(RequestEntity.get(URI.create(url(ruta)))
                .header(HttpHeaders.COOKIE, cookie).build(), String.class);
    }

    private ResponseEntity<String> post(String ruta, String jsonBody) {
        return rest.exchange(RequestEntity.post(URI.create(url(ruta)))
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody), String.class);
    }

    // ------------------------------------------------------------------
    // Fase 2 — cobertura GET
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "GET {0} responde 2xx")
    @ValueSource(strings = {
            "/api/v1/asistencias",
            "/api/v1/auditoria",
            "/api/v1/bonos",
            "/api/v1/categorias-producto",
            "/api/v1/clientes",
            "/api/v1/compras",
            "/api/v1/dashboard",
            "/api/v1/habitaciones",
            "/api/v1/horarios",
            "/api/v1/incidencias",
            "/api/v1/metodos-pago",
            "/api/v1/mis-reservas",
            "/api/v1/pagos",
            "/api/v1/pagos-nomina",
            "/api/v1/pedidos-servicio",
            "/api/v1/permisos",
            "/api/v1/productos",
            "/api/v1/proveedores",
            "/api/v1/reservas",
            "/api/v1/roles",
            "/api/v1/servicios",
            "/api/v1/solicitudes",
            "/api/v1/tipos-documento",
            "/api/v1/tipos-habitacion",
            "/api/v1/tipos-servicio",
            "/api/v1/usuarios",
            "/api/v1/ventas"
    })
    void getAdminRespondeOk(String ruta) {
        assertThat(get(ruta).getStatusCode().is2xxSuccessful())
                .as("GET %s", ruta).isTrue();
    }

    @ParameterizedTest(name = "GET público {0} responde 2xx sin sesión")
    @ValueSource(strings = {
            "/api/v1/booking/metodos-pago",
            "/api/v1/booking/disponibles?fechaInicio=2027-01-10&fechaFin=2027-01-12&nroAdultos=1&nroNinos=0"
    })
    void getPublicoRespondeOk(String ruta) {
        ResponseEntity<String> res = rest.getForEntity(url(ruta), String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).as("GET %s", ruta).isTrue();
    }

    @ParameterizedTest(name = "GET {0} exige rol CLIENTE (403 para admin)")
    @ValueSource(strings = {
            "/api/v1/mis-servicios",
            "/api/v1/servicios-catalogo",
            "/api/v1/notificaciones"
    })
    void endpointsDeClienteRechazanAdmin(String ruta) {
        assertThat(get(ruta).getStatusCode().value())
                .as("contrato de permisos de %s", ruta).isEqualTo(403);
    }

    // ------------------------------------------------------------------
    // Fase 3 — POSTs críticos (las regresiones históricas del sistema)
    // ------------------------------------------------------------------

    /** Sufijo único por corrida: evita choques de unicidad si el cleanup falla. */
    private static final long RUN = System.currentTimeMillis() % 100_000_000L;

    private final java.util.List<String> creadosParaLimpiar = new java.util.ArrayList<>();

    private tools.jackson.databind.JsonNode json(ResponseEntity<String> res) {
        return tools.jackson.databind.json.JsonMapper.builder().build().readTree(res.getBody());
    }

    @Test
    @DisplayName("POST /booking público crea la reserva completa (regresión modalidadPago)")
    void bookingPublicoFunciona() {
        // Fechas únicas por corrida: la reserva de booking no siempre se puede
        // borrar (tiene pago asociado), así que se evita el solapamiento.
        LocalDate inicio = LocalDate.now().plusYears(1).plusDays(RUN % 3000);
        String body = """
                {"fechaInicio":"%s","fechaFin":"%s","habitacionId":2,"tipoHabitacionId":1,
                 "numeroDocumento":"%08d","nombres":"Smoke","apellidos":"Test",
                 "correo":"smoke.booking.%d@test.local","nroAdultos":1,"nroNinos":0,
                 "tipoPago":"TOTAL","metodoPagoId":2}"""
                .formatted(inicio, inicio.plusDays(2), RUN, RUN);
        ResponseEntity<String> res = rest.exchange(RequestEntity.post(URI.create(url("/api/v1/booking")))
                .contentType(MediaType.APPLICATION_JSON).body(body), String.class);
        assertThat(res.getStatusCode().value()).as("booking público: %s", res.getBody()).isEqualTo(201);
        creadosParaLimpiar.add("/api/v1/reservas/" + json(res).path("data").path("reservaId").asInt());
    }

    @Test
    @DisplayName("POST /ventas acepta fechaVenta como fecha sola (deserializador tolerante)")
    void ventaConFechaSolaFunciona() {
        int productoId = json(get("/api/v1/productos?size=1"))
                .path("data").path("content").path(0).path("productoId").asInt();
        Integer adminId = usuarioRepository.findByCorreo(SMOKE_ADMIN_CORREO).orElseThrow().getUsuarioId();
        String body = """
                {"codigoVenta":"SMOKE-%d","tipoVenta":"DIRECTA","fechaVenta":"%s","usuarioId":%d,
                 "detalles":[{"productoId":%d,"cantidad":1,"precioUnitario":1.00}]}"""
                .formatted(RUN, LocalDate.now(), adminId, productoId);
        ResponseEntity<String> res = post("/api/v1/ventas", body);
        assertThat(res.getStatusCode().value()).as("venta con fecha sola: %s", res.getBody()).isEqualTo(201);
        assertThat(json(res).path("data").path("fechaVenta").asString()).startsWith(LocalDate.now().toString());
        creadosParaLimpiar.add("/api/v1/ventas/" + json(res).path("data").path("ventaId").asInt());
    }

    @Test
    @DisplayName("POST /incidencias: fechaReporte la asigna el servidor (server-authoritative)")
    void incidenciaConFechaDelServidor() {
        Integer adminId = usuarioRepository.findByCorreo(SMOKE_ADMIN_CORREO).orElseThrow().getUsuarioId();
        ResponseEntity<String> res = post("/api/v1/incidencias", """
                {"descripcion":"[SMOKE] incidencia de prueba automatizada","prioridad":"BAJA","usuarioId":%d}"""
                .formatted(adminId));
        assertThat(res.getStatusCode().value()).as("incidencia: %s", res.getBody()).isEqualTo(201);
        assertThat(json(res).path("data").path("fechaReporte").isNull()).isFalse();
        creadosParaLimpiar.add("/api/v1/incidencias/" + json(res).path("data").path("incidenciaId").asInt());
    }

    @Test
    @DisplayName("un body malformado responde 400 MALFORMED_BODY, nunca 500")
    void bodyMalformadoNoEs500() {
        ResponseEntity<String> res = post("/api/v1/ventas",
                "{\"codigoVenta\":\"X\",\"tipoVenta\":\"ENUM-INEXISTENTE\"}");
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).contains("MALFORMED_BODY");
    }

    @org.junit.jupiter.api.AfterAll
    void limpiarDatosDePrueba() {
        // Best effort: si algún DELETE falla (p.ej. FKs), el dato queda marcado
        // SMOKE- y el sufijo único evita choques en la siguiente corrida.
        for (String ruta : creadosParaLimpiar) {
            rest.exchange(RequestEntity.delete(URI.create(url(ruta)))
                    .header(HttpHeaders.COOKIE, cookie).build(), String.class);
        }
    }

    @Test
    @DisplayName("los filtros de rango aceptan fecha sola (YYYY-MM-DD)")
    void filtrosAceptanFechaSola() {
        String hoy = LocalDate.now().toString();
        assertThat(get("/api/v1/pagos?fechaDesde=" + hoy + "&fechaHasta=" + hoy)
                .getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(get("/api/v1/ventas?fechaVentaDesde=" + hoy + "&fechaVentaHasta=" + hoy)
                .getStatusCode().is2xxSuccessful()).isTrue();
        // Formato viejo con hora: debe seguir aceptándose (converter tolerante).
        assertThat(get("/api/v1/ventas?fechaVentaDesde=" + hoy + "T00:00:00")
                .getStatusCode().is2xxSuccessful()).isTrue();
    }
}
