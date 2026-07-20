-- ============================================================
-- Base de datos: PuntoMaya - Sistema de Ventas
-- Motor: MySQL (crear/correr desde MySQL Workbench)
-- ============================================================

CREATE DATABASE IF NOT EXISTS puntomaya
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE puntomaya;

-- ------------------------------------------------------------
-- USUARIO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    nombre_usuario  VARCHAR(50)  NOT NULL UNIQUE,
    contrasena      VARCHAR(255) NOT NULL,
    rol             ENUM('ADMINISTRADOR','CAJERO','ALMACENISTA') NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------
-- CLIENTE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    telefono        VARCHAR(15),
    limite_credito  DECIMAL(10,2) NOT NULL DEFAULT 0,
    saldo_actual    DECIMAL(10,2) NOT NULL DEFAULT 0
);

-- ------------------------------------------------------------
-- PROVEEDOR
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor    INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL
);

-- ------------------------------------------------------------
-- PRODUCTO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id_producto      INT AUTO_INCREMENT PRIMARY KEY,
    codigo_barras    VARCHAR(30) UNIQUE,
    nombre           VARCHAR(100) NOT NULL,
    precio_venta     DECIMAL(10,2) NOT NULL,
    precio_costo     DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock            DECIMAL(10,2) NOT NULL DEFAULT 0,
    es_granel        BOOLEAN NOT NULL DEFAULT FALSE,
    punto_reorden    DECIMAL(10,2) NOT NULL DEFAULT 5,
    fecha_caducidad  DATE NULL
);

-- ------------------------------------------------------------
-- VENTA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta (
    id_venta        INT AUTO_INCREMENT PRIMARY KEY,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal        DECIMAL(10,2) NOT NULL,
    descuento       DECIMAL(10,2) NOT NULL DEFAULT 0,
    total           DECIMAL(10,2) NOT NULL,
    forma_pago      ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
    es_fiado        BOOLEAN NOT NULL DEFAULT FALSE,
    cancelada       BOOLEAN NOT NULL DEFAULT FALSE,
    id_cliente      INT NULL,
    id_usuario      INT NOT NULL,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ------------------------------------------------------------
-- DETALLE_VENTA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle       INT AUTO_INCREMENT PRIMARY KEY,
    id_venta         INT NOT NULL,
    id_producto      INT NOT NULL,
    cantidad         DECIMAL(10,2) NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    importe          DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_venta   FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- ------------------------------------------------------------
-- INVENTARIO (movimientos: entradas, mermas, devoluciones, ajustes)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventario (
    id_movimiento   INT AUTO_INCREMENT PRIMARY KEY,
    id_producto     INT NOT NULL,
    id_proveedor    INT NULL,
    tipo            ENUM('ENTRADA','MERMA','DEVOLUCION','AJUSTE') NOT NULL,
    cantidad        DECIMAL(10,2) NOT NULL,
    motivo          VARCHAR(150) NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario      INT NOT NULL,
    CONSTRAINT fk_inv_producto  FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT fk_inv_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor),
    CONSTRAINT fk_inv_usuario   FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ------------------------------------------------------------
-- CORTE_CAJA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS corte_caja (
    id_corte           INT AUTO_INCREMENT PRIMARY KEY,
    fecha              DATE NOT NULL,
    turno              VARCHAR(20) NOT NULL,
    id_usuario         INT NOT NULL,
    fondo_inicial      DECIMAL(10,2) NOT NULL,
    efectivo_esperado  DECIMAL(10,2) NOT NULL,
    efectivo_real      DECIMAL(10,2) NOT NULL,
    diferencia         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_corte_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ------------------------------------------------------------
-- Datos iniciales de prueba
-- ------------------------------------------------------------
INSERT INTO usuario (nombre, nombre_usuario, contrasena, rol) VALUES
    ('Doña Rosa', 'rosa', 'admin123', 'ADMINISTRADOR'),
    ('Lupita', 'lupita', 'cajero123', 'CAJERO'),
    ('Beto', 'beto', 'almacen123', 'ALMACENISTA');

INSERT INTO producto (codigo_barras, nombre, precio_venta, precio_costo, stock, es_granel, punto_reorden) VALUES
    ('7501000111316', 'Coca-Cola 600ml', 18.00, 12.00, 48, FALSE, 12),
    ('7501000112320', 'Sabritas Original 45g', 20.00, 14.00, 30, FALSE, 10),
    (NULL,           'Frijol (kg)',          28.00, 20.00, 50, TRUE,  10),
    (NULL,           'Arroz (kg)',           24.00, 17.00, 40, TRUE,  10);

INSERT INTO cliente (nombre, telefono, limite_credito, saldo_actual) VALUES
    ('Sra. Petrona', '9211234567', 500.00, 0.00);
