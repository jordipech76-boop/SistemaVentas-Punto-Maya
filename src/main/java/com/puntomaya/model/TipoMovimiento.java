package com.puntomaya.model;

/**
 * Tipos de movimiento que puede tener el inventario de un producto.
 */
public enum TipoMovimiento {
    ENTRADA,      // llega mercancía de un proveedor
    MERMA,        // se rompe, caduca o se pierde producto
    DEVOLUCION,   // se devuelve producto a un proveedor
    AJUSTE        // corrección manual de existencia
}
