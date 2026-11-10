package com.petservices.db;

/**
 * Error controlado de las operaciones de copia y restauración.
 *
 * Su mensaje está pensado para mostrarse al usuario del panel: explica qué
 * pasó (archivo inválido, esquema incompatible, copia no encontrada, etc.) sin
 * exponer detalles técnicos.
 */
public class RespaldoException extends RuntimeException {

    public RespaldoException(String mensaje) {
        super(mensaje);
    }

    public RespaldoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
