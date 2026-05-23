-- =============================================================================
-- V1__initial_schema.sql
-- Sistema Hotelero San Francisco – Esquema inicial completo
-- Orden de creación: respeta dependencias FK (topological sort)
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- TIER 1: Tablas sin dependencias FK
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE tipos_documento (
    tipo_documento_id SERIAL          PRIMARY KEY,
    acronimo          VARCHAR(10)     NOT NULL,
    nombre            VARCHAR(80)     NOT NULL,
    estado            VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion    TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT chk_tipos_documento_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE roles (
    rol_id             SERIAL          PRIMARY KEY,
    nombre             VARCHAR(80)     NOT NULL,
    descripcion        TEXT,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT chk_roles_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE permisos (
    permiso_id         SERIAL          PRIMARY KEY,
    nombre             VARCHAR(100)    NOT NULL,
    codigo             VARCHAR(80)     NOT NULL,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT uk_permisos_codigo UNIQUE (codigo)
);

CREATE TABLE categorias_producto (
    categoria_producto_id SERIAL      PRIMARY KEY,
    nombre                VARCHAR(100) NOT NULL,
    descripcion           TEXT,
    estado                VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion        TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_modificacion    TIMESTAMP,

    CONSTRAINT chk_categorias_producto_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE horarios (
    horario_id         SERIAL          PRIMARY KEY,
    nombre_turno       VARCHAR(80)     NOT NULL,
    hora_entrada       TIME            NOT NULL,
    hora_salida        TIME            NOT NULL,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT chk_horarios_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE tipos_habitacion (
    tipo_habitacion_id SERIAL          PRIMARY KEY,
    nombre             VARCHAR(80)     NOT NULL,
    precio_base        NUMERIC(12,2)   NOT NULL,
    descripcion        TEXT,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    capacidad_maxima   INTEGER         NOT NULL,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT chk_tipos_habitacion_estado   CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT chk_tipos_habitacion_precio   CHECK (precio_base >= 0),
    CONSTRAINT chk_tipos_habitacion_capacidad CHECK (capacidad_maxima >= 1)
);

CREATE TABLE canales (
    canal_id           SERIAL          PRIMARY KEY,
    nombre             VARCHAR(80)     NOT NULL,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT chk_canales_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE metodos_pago (
    metodo_pago_id       SERIAL        PRIMARY KEY,
    nombre               VARCHAR(80)   NOT NULL,
    estado               VARCHAR(10)   NOT NULL DEFAULT 'ACTIVO',
    requiere_comprobante BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha_creacion       TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP,

    CONSTRAINT chk_metodos_pago_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE tipos_servicio (
    tipo_servicio_id   SERIAL          PRIMARY KEY,
    nombre             VARCHAR(100)    NOT NULL,
    costo_base         NUMERIC(12,2)   NOT NULL,
    descripcion        TEXT,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT chk_tipos_servicio_estado  CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT chk_tipos_servicio_costo   CHECK (costo_base >= 0)
);

CREATE TABLE proveedores (
    proveedor_id       SERIAL          PRIMARY KEY,
    ruc_nit_cif        VARCHAR(20),
    razon_social       VARCHAR(200)    NOT NULL,
    contacto_nombre    VARCHAR(100),
    telefono           VARCHAR(20),
    email              VARCHAR(100),
    direccion          VARCHAR(300)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TIER 2: Dependen de Tier 1
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE detalles_rol (
    permiso_id         INTEGER         NOT NULL,
    rol_id             INTEGER         NOT NULL,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT pk_detalles_rol        PRIMARY KEY (permiso_id, rol_id),
    CONSTRAINT fk_detalles_rol_permiso FOREIGN KEY (permiso_id) REFERENCES permisos(permiso_id),
    CONSTRAINT fk_detalles_rol_rol     FOREIGN KEY (rol_id)     REFERENCES roles(rol_id)
);

CREATE TABLE usuarios (
    usuario_id         SERIAL          PRIMARY KEY,
    nombre             VARCHAR(80)     NOT NULL,
    apellido_paterno   VARCHAR(80)     NOT NULL,
    apellido_materno   VARCHAR(80),
    numero_documento   VARCHAR(20)     NOT NULL,
    correo             VARCHAR(150)    NOT NULL,
    telefono           VARCHAR(20),
    fecha_nacimiento   DATE,
    contrasena_hash    VARCHAR(255)    NOT NULL,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    rol_id             INTEGER         NOT NULL,
    tipo_documento_id  INTEGER         NOT NULL,

    CONSTRAINT uk_usuarios_correo          UNIQUE (correo),
    CONSTRAINT fk_usuarios_rol             FOREIGN KEY (rol_id)            REFERENCES roles(rol_id),
    CONSTRAINT fk_usuarios_tipo_documento  FOREIGN KEY (tipo_documento_id) REFERENCES tipos_documento(tipo_documento_id),
    CONSTRAINT chk_usuarios_estado         CHECK (estado IN ('ACTIVO','INACTIVO','BLOQUEADO'))
);

CREATE TABLE productos (
    producto_id           SERIAL          PRIMARY KEY,
    nombre                VARCHAR(150)    NOT NULL,
    descripcion           TEXT,
    precio_venta          NUMERIC(12,2)   NOT NULL,
    stock_actual          NUMERIC(10,2)   NOT NULL,
    stock_minimo          NUMERIC(10,2)   NOT NULL,
    estado                VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion        TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion    TIMESTAMP,
    categoria_producto_id INTEGER         NOT NULL,

    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_producto_id) REFERENCES categorias_producto(categoria_producto_id),
    CONSTRAINT chk_productos_estado   CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT chk_productos_precio   CHECK (precio_venta >= 0),
    CONSTRAINT chk_productos_stock    CHECK (stock_actual >= 0 AND stock_minimo >= 0)
);

CREATE TABLE habitaciones (
    habitacion_id      SERIAL          PRIMARY KEY,
    numero             VARCHAR(10)     NOT NULL,
    piso               INTEGER         NOT NULL,
    estado             VARCHAR(20)     NOT NULL DEFAULT 'DISPONIBLE',
    descripcion        TEXT,
    observaciones      TEXT,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT uk_habitaciones_numero  UNIQUE (numero),
    CONSTRAINT chk_habitaciones_estado CHECK (estado IN ('DISPONIBLE','OCUPADA','MANTENIMIENTO','BLOQUEADA')),
    CONSTRAINT chk_habitaciones_piso   CHECK (piso >= 1)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TIER 3: Dependen de Tier 2
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE sesiones (
    sesion_id          SERIAL          PRIMARY KEY,
    token_hash         VARCHAR(255)    NOT NULL,
    ip_origen          VARCHAR(45),
    user_agent         VARCHAR(300),
    fecha_inicio       TIMESTAMP       NOT NULL,
    fecha_expiracion   TIMESTAMP       NOT NULL,
    fecha_cierre       TIMESTAMP,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVA',
    usuario_id         INTEGER         NOT NULL,

    CONSTRAINT fk_sesiones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_sesiones_estado CHECK (estado IN ('ACTIVA','EXPIRADA','CERRADA'))
);

CREATE TABLE huespedes (
    huesped_id         SERIAL          PRIMARY KEY,
    nombre             VARCHAR(80)     NOT NULL,
    apellido_paterno   VARCHAR(80)     NOT NULL,
    apellido_materno   VARCHAR(80),
    numero_documento   VARCHAR(20)     NOT NULL,
    nacionalidad       VARCHAR(60),
    correo             VARCHAR(150),
    telefono           VARCHAR(20),
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    usuario_id         INTEGER,

    CONSTRAINT fk_huespedes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_huespedes_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE reservas (
    reserva_id         SERIAL          PRIMARY KEY,
    cod_reserva        VARCHAR(30)     NOT NULL,
    fecha_inicio       DATE            NOT NULL,
    fecha_fin          DATE            NOT NULL,
    monto_total        NUMERIC(12,2)   NOT NULL,
    estado             VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    nro_adultos        INTEGER         NOT NULL DEFAULT 1,
    nro_ninos          INTEGER         NOT NULL DEFAULT 0,
    subtotal           NUMERIC(12,2)   NOT NULL,
    descuento          NUMERIC(12,2)   NOT NULL DEFAULT 0,
    adelanto           NUMERIC(12,2)   NOT NULL DEFAULT 0,
    impuesto           NUMERIC(12,2)   NOT NULL DEFAULT 0,
    observaciones      TEXT,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    usuario_id         INTEGER         NOT NULL,
    canal_id           INTEGER,

    CONSTRAINT uk_reservas_cod          UNIQUE (cod_reserva),
    CONSTRAINT fk_reservas_usuario      FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_reservas_canal        FOREIGN KEY (canal_id)   REFERENCES canales(canal_id),
    CONSTRAINT chk_reservas_estado      CHECK (estado IN ('PENDIENTE','CONFIRMADA','CHECK_IN','CHECK_OUT','CANCELADA','NO_SHOW')),
    CONSTRAINT chk_reservas_fechas      CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_reservas_adultos     CHECK (nro_adultos >= 0),
    CONSTRAINT chk_reservas_ninos       CHECK (nro_ninos >= 0),
    CONSTRAINT chk_reservas_montos      CHECK (monto_total >= 0 AND subtotal >= 0 AND descuento >= 0 AND adelanto >= 0 AND impuesto >= 0)
);

CREATE TABLE compras (
    compra_id          SERIAL          PRIMARY KEY,
    proveedor_id       INTEGER         NOT NULL,
    fecha_compra       DATE            NOT NULL,
    numero_factura     VARCHAR(50),
    subtotal           NUMERIC(12,2)   NOT NULL,
    impuesto           NUMERIC(12,2)   NOT NULL,
    monto_total        NUMERIC(12,2)   NOT NULL,

    CONSTRAINT fk_compras_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(proveedor_id),
    CONSTRAINT chk_compras_montos   CHECK (subtotal >= 0 AND impuesto >= 0 AND monto_total >= 0)
);

CREATE TABLE detalles_horario (
    usuario_id            INTEGER     NOT NULL,
    horario_id            INTEGER     NOT NULL,
    dia_semana            INTEGER     NOT NULL,
    estado                VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',
    fecha_vigencia_inicio DATE        NOT NULL,
    fecha_vigencia_fin    DATE,

    CONSTRAINT pk_detalles_horario        PRIMARY KEY (usuario_id, horario_id),
    CONSTRAINT fk_detalles_horario_usuario FOREIGN KEY (usuario_id)  REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_detalles_horario_horario FOREIGN KEY (horario_id)  REFERENCES horarios(horario_id),
    CONSTRAINT chk_detalles_horario_dia   CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT chk_detalles_horario_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
);

CREATE TABLE asistencia (
    asistencia_id      SERIAL          PRIMARY KEY,
    fecha              DATE            NOT NULL,
    hora_ingreso       TIME            NOT NULL,
    hora_egreso        TIME,
    horas_trabajadas   NUMERIC(5,2),
    tipo               VARCHAR(30)     NOT NULL,
    observaciones      TEXT,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    usuario_id         INTEGER         NOT NULL,

    CONSTRAINT fk_asistencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_asistencia_tipo   CHECK (tipo IN ('NORMAL','TARDANZA','FALTA_JUSTIFICADA','FALTA_INJUSTIFICADA','PERMISO'))
);

CREATE TABLE pagos_nomina (
    pago_nomina_id     SERIAL          PRIMARY KEY,
    periodo            VARCHAR(20)     NOT NULL,
    fecha_emision      DATE            NOT NULL,
    sueldo_base        NUMERIC(12,2)   NOT NULL,
    total_bonos        NUMERIC(12,2)   NOT NULL DEFAULT 0,
    total_descuentos   NUMERIC(12,2)   NOT NULL DEFAULT 0,
    monto_neto         NUMERIC(12,2)   NOT NULL,
    estado             VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    usuario_id         INTEGER         NOT NULL,

    CONSTRAINT fk_pagos_nomina_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_pagos_nomina_estado CHECK (estado IN ('PENDIENTE','PAGADO','ANULADO')),
    CONSTRAINT chk_pagos_nomina_montos CHECK (sueldo_base >= 0 AND total_bonos >= 0 AND total_descuentos >= 0 AND monto_neto >= 0)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TIER 4: Dependen de Tier 3
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE bonos (
    bono_id            SERIAL          PRIMARY KEY,
    monto              NUMERIC(12,2)   NOT NULL,
    motivo             VARCHAR(200)    NOT NULL,
    fecha_asignacion   DATE            NOT NULL,
    estado             VARCHAR(10)     NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    pago_nomina_id     INTEGER,
    usuario_id         INTEGER         NOT NULL,

    CONSTRAINT fk_bonos_nomina   FOREIGN KEY (pago_nomina_id) REFERENCES pagos_nomina(pago_nomina_id),
    CONSTRAINT fk_bonos_usuario  FOREIGN KEY (usuario_id)     REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_bonos_estado  CHECK (estado IN ('ACTIVO','ANULADO')),
    CONSTRAINT chk_bonos_monto   CHECK (monto >= 0)
);

CREATE TABLE reserva_habitaciones (
    reserva_habitacion_id SERIAL        PRIMARY KEY,
    tarifa_pactada         NUMERIC(12,2) NOT NULL,
    noches                 INTEGER       NOT NULL,
    estado                 VARCHAR(20)   NOT NULL DEFAULT 'RESERVADA',
    subtotal               NUMERIC(12,2) NOT NULL,
    fecha_creacion         TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_modificacion     TIMESTAMP,
    habitacion_id          INTEGER       NOT NULL,
    tipo_habitacion_id     INTEGER       NOT NULL,
    reserva_id             INTEGER       NOT NULL,

    CONSTRAINT fk_rh_habitacion     FOREIGN KEY (habitacion_id)      REFERENCES habitaciones(habitacion_id),
    CONSTRAINT fk_rh_tipo_hab       FOREIGN KEY (tipo_habitacion_id) REFERENCES tipos_habitacion(tipo_habitacion_id),
    CONSTRAINT fk_rh_reserva        FOREIGN KEY (reserva_id)         REFERENCES reservas(reserva_id),
    CONSTRAINT chk_rh_estado        CHECK (estado IN ('RESERVADA','OCUPADA','LIBERADA')),
    CONSTRAINT chk_rh_noches        CHECK (noches >= 1),
    CONSTRAINT chk_rh_tarifa        CHECK (tarifa_pactada >= 0 AND subtotal >= 0)
);

CREATE TABLE detalles_huesped (
    huesped_id         INTEGER         NOT NULL,
    reserva_id         INTEGER         NOT NULL,
    es_principal       BOOLEAN         NOT NULL DEFAULT FALSE,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT pk_detalles_huesped        PRIMARY KEY (huesped_id, reserva_id),
    CONSTRAINT fk_detalles_huesped_huesped FOREIGN KEY (huesped_id) REFERENCES huespedes(huesped_id),
    CONSTRAINT fk_detalles_huesped_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(reserva_id)
);

CREATE TABLE estancias (
    estancia_id         SERIAL          PRIMARY KEY,
    fecha_checkin       TIMESTAMP       NOT NULL,
    fecha_checkout      TIMESTAMP,
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    usuario_checkin_id  INTEGER         NOT NULL,
    usuario_checkout_id INTEGER,
    reserva_id          INTEGER         NOT NULL,

    CONSTRAINT fk_estancias_checkin  FOREIGN KEY (usuario_checkin_id)  REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_estancias_checkout FOREIGN KEY (usuario_checkout_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_estancias_reserva  FOREIGN KEY (reserva_id)          REFERENCES reservas(reserva_id)
);

CREATE TABLE detalles_compra (
    compra_id          INTEGER         NOT NULL,
    producto_id        INTEGER         NOT NULL,
    cantidad           NUMERIC(10,2)   NOT NULL,
    costo_unitario     NUMERIC(12,2)   NOT NULL,
    subtotal           NUMERIC(12,2)   NOT NULL,

    CONSTRAINT pk_detalles_compra          PRIMARY KEY (compra_id, producto_id),
    CONSTRAINT fk_detalles_compra_compra   FOREIGN KEY (compra_id)   REFERENCES compras(compra_id),
    CONSTRAINT fk_detalles_compra_producto FOREIGN KEY (producto_id) REFERENCES productos(producto_id),
    CONSTRAINT chk_detalles_compra_cant    CHECK (cantidad > 0 AND costo_unitario >= 0 AND subtotal >= 0)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TIER 5: Dependen de Tier 4
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE ventas (
    venta_id           SERIAL          PRIMARY KEY,
    codigo_venta       VARCHAR(30)     NOT NULL,
    tipo_venta         VARCHAR(30)     NOT NULL,
    monto_total        NUMERIC(12,2)   NOT NULL,
    fecha_venta        TIMESTAMP       NOT NULL DEFAULT NOW(),
    estado             VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    usuario_id         INTEGER         NOT NULL,
    estancia_id        INTEGER,
    huesped_id         INTEGER,

    CONSTRAINT uk_ventas_codigo      UNIQUE (codigo_venta),
    CONSTRAINT fk_ventas_usuario     FOREIGN KEY (usuario_id)  REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_ventas_estancia    FOREIGN KEY (estancia_id) REFERENCES estancias(estancia_id),
    CONSTRAINT fk_ventas_huesped     FOREIGN KEY (huesped_id)  REFERENCES huespedes(huesped_id),
    CONSTRAINT chk_ventas_estado     CHECK (estado IN ('PENDIENTE','COMPLETADA','ANULADA')),
    CONSTRAINT chk_ventas_tipo       CHECK (tipo_venta IN ('DIRECTA','CARGO_HABITACION','DELIVERY','EVENTO')),
    CONSTRAINT chk_ventas_monto      CHECK (monto_total >= 0)
);

CREATE TABLE servicios (
    servicio_id        SERIAL          PRIMARY KEY,
    cantidad           NUMERIC(10,2)   NOT NULL,
    precio_aplicado    NUMERIC(12,2)   NOT NULL,
    subtotal           NUMERIC(12,2)   NOT NULL,
    observaciones      TEXT,
    fecha_consumo      TIMESTAMP       NOT NULL,
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    tipo_servicio_id   INTEGER         NOT NULL,
    estancia_id        INTEGER         NOT NULL,

    CONSTRAINT fk_servicios_tipo     FOREIGN KEY (tipo_servicio_id) REFERENCES tipos_servicio(tipo_servicio_id),
    CONSTRAINT fk_servicios_estancia FOREIGN KEY (estancia_id)      REFERENCES estancias(estancia_id),
    CONSTRAINT chk_servicios_montos  CHECK (cantidad > 0 AND precio_aplicado >= 0 AND subtotal >= 0)
);

CREATE TABLE incidencias (
    incidencia_id          SERIAL      PRIMARY KEY,
    descripcion            TEXT        NOT NULL,
    fecha_reporte          TIMESTAMP   NOT NULL DEFAULT NOW(),
    fecha_resolucion       TIMESTAMP,
    prioridad              VARCHAR(10) NOT NULL,
    solucion               TEXT,
    estado                 VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    fecha_creacion         TIMESTAMP   NOT NULL DEFAULT NOW(),
    fecha_modificacion     TIMESTAMP,
    usuario_id             INTEGER     NOT NULL,
    reserva_habitacion_id  INTEGER,

    CONSTRAINT fk_incidencias_usuario FOREIGN KEY (usuario_id)            REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_incidencias_rh      FOREIGN KEY (reserva_habitacion_id) REFERENCES reserva_habitaciones(reserva_habitacion_id),
    CONSTRAINT chk_incidencias_estado    CHECK (estado    IN ('ABIERTA','EN_PROCESO','RESUELTA','CERRADA')),
    CONSTRAINT chk_incidencias_prioridad CHECK (prioridad IN ('ALTA','MEDIA','BAJA'))
);

-- ─────────────────────────────────────────────────────────────────────────────
-- TIER 6: Dependen de Tier 5
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE detalles_venta (
    venta_id           INTEGER         NOT NULL,
    producto_id        INTEGER         NOT NULL,
    cantidad           NUMERIC(10,2)   NOT NULL,
    precio_unitario    NUMERIC(12,2)   NOT NULL,
    descuento_unitario NUMERIC(12,2)   NOT NULL DEFAULT 0,
    subtotal           NUMERIC(12,2)   NOT NULL,

    CONSTRAINT pk_detalles_venta           PRIMARY KEY (venta_id, producto_id),
    CONSTRAINT fk_detalles_venta_venta     FOREIGN KEY (venta_id)    REFERENCES ventas(venta_id),
    CONSTRAINT fk_detalles_venta_producto  FOREIGN KEY (producto_id) REFERENCES productos(producto_id),
    CONSTRAINT chk_detalles_venta_cant     CHECK (cantidad > 0 AND precio_unitario >= 0 AND descuento_unitario >= 0 AND subtotal >= 0)
);

CREATE TABLE pagos (
    pago_id            SERIAL          PRIMARY KEY,
    metodo_pago_id     INTEGER         NOT NULL,
    tipo_pago          VARCHAR(20)     NOT NULL,
    fecha              TIMESTAMP       NOT NULL DEFAULT NOW(),
    monto              NUMERIC(12,2)   NOT NULL,
    comprobante        VARCHAR(100),
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,
    venta_id           INTEGER,
    reserva_id         INTEGER,

    CONSTRAINT fk_pagos_metodo  FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(metodo_pago_id),
    CONSTRAINT fk_pagos_venta   FOREIGN KEY (venta_id)       REFERENCES ventas(venta_id),
    CONSTRAINT fk_pagos_reserva FOREIGN KEY (reserva_id)     REFERENCES reservas(reserva_id),
    CONSTRAINT chk_pagos_tipo   CHECK (tipo_pago IN ('ANTICIPO','SALDO','TOTAL','REEMBOLSO')),
    CONSTRAINT chk_pagos_monto  CHECK (monto >= 0)
);

-- =============================================================================
-- ÍNDICES para columnas de búsqueda frecuente
-- =============================================================================

CREATE INDEX idx_usuarios_correo          ON usuarios(correo);
CREATE INDEX idx_usuarios_estado          ON usuarios(estado);
CREATE INDEX idx_usuarios_rol             ON usuarios(rol_id);

CREATE INDEX idx_reservas_estado          ON reservas(estado);
CREATE INDEX idx_reservas_fecha_inicio    ON reservas(fecha_inicio);
CREATE INDEX idx_reservas_usuario         ON reservas(usuario_id);

CREATE INDEX idx_habitaciones_estado      ON habitaciones(estado);
CREATE INDEX idx_habitaciones_piso        ON habitaciones(piso);

CREATE INDEX idx_huespedes_numero_doc     ON huespedes(numero_documento);
CREATE INDEX idx_huespedes_estado         ON huespedes(estado);

CREATE INDEX idx_ventas_estado            ON ventas(estado);
CREATE INDEX idx_ventas_fecha             ON ventas(fecha_venta);
CREATE INDEX idx_ventas_estancia          ON ventas(estancia_id);

CREATE INDEX idx_pagos_reserva            ON pagos(reserva_id);
CREATE INDEX idx_pagos_venta              ON pagos(venta_id);
CREATE INDEX idx_pagos_fecha              ON pagos(fecha);

CREATE INDEX idx_servicios_estancia       ON servicios(estancia_id);
CREATE INDEX idx_asistencia_usuario_fecha ON asistencia(usuario_id, fecha);

CREATE INDEX idx_sesiones_token           ON sesiones(token_hash);
CREATE INDEX idx_sesiones_usuario         ON sesiones(usuario_id);

CREATE INDEX idx_incidencias_estado       ON incidencias(estado);
CREATE INDEX idx_incidencias_prioridad    ON incidencias(prioridad);

CREATE INDEX idx_rh_reserva               ON reserva_habitaciones(reserva_id);
CREATE INDEX idx_rh_habitacion            ON reserva_habitaciones(habitacion_id);

CREATE INDEX idx_pagos_nomina_usuario     ON pagos_nomina(usuario_id);
CREATE INDEX idx_pagos_nomina_periodo     ON pagos_nomina(periodo);

CREATE INDEX idx_productos_categoria      ON productos(categoria_producto_id);
CREATE INDEX idx_productos_estado         ON productos(estado);
