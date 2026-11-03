package com.petservices.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la lógica de fechas/estados de Cita (M9 de la auditoría).
 */
class CitaTest {

    @Test
    void separaFechaYHora() {
        Cita cita = new Cita("2026-08-20", "10:30", 1, 0);
        assertEquals("2026-08-20", cita.getFecha());
        assertEquals("10:30", cita.getHora());
        assertEquals("2026-08-20T10:30", cita.getFechaHora());
    }

    @Test
    void unaHoraVaciaNoBorraLaExistente() {
        Cita cita = new Cita("2026-08-20", "15:45", 1, 0);
        cita.setHora("");
        cita.setHora(null);
        assertEquals("15:45", cita.getHora(), "setHora vacío debe conservar la hora");
    }

    @Test
    void cambiarLaFechaConservaLaHora() {
        Cita cita = new Cita("2026-08-20", "15:45", 1, 0);
        cita.setFecha("2026-09-01");
        assertEquals("2026-09-01T15:45", cita.getFechaHora());
    }

    @Test
    void unaCitaNuevaSinHoraUsaMedianoche() {
        Cita cita = new Cita("2026-08-20", "", 1, 0);
        assertEquals("00:00", cita.getHora());
    }
}
