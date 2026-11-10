package com.petservices.db;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicializa la base de datos cuando arranca la aplicación web y la cierra
 * ordenadamente al detenerla.
 *
 * Si la inicialización falla, el error se registra y se guarda en el contexto.
 * La aplicación sigue arrancando para que el filtro de errores pueda mostrar
 * un mensaje claro en lugar de una página en blanco.
 */
@WebListener
public class BaseDatosContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(BaseDatosContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try {
            BaseDatos.inicializar();
            evento.getServletContext().setAttribute("baseDatosRuta", BaseDatos.descripcionRuta());
            LOG.info("PetServices: persistencia SQLite inicializada en " + BaseDatos.descripcionRuta());
        } catch (RuntimeException e) {
            evento.getServletContext().setAttribute("errorBaseDatos", e.getMessage());
            LOG.log(Level.SEVERE, "PetServices: no se pudo inicializar la base de datos local", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        BaseDatos.cerrar();
        deregistrarDrivers();
        LOG.info("PetServices: base de datos cerrada correctamente");
    }

    /**
     * Anula el registro de los drivers JDBC cargados por esta aplicación.
     * Evita la advertencia de Tomcat sobre fugas de memoria al redesplegar.
     */
    private void deregistrarDrivers() {
        ClassLoader cargadorAplicacion = BaseDatosContextListener.class.getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cargadorAplicacion) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    LOG.log(Level.FINE, "No se pudo anular el registro del driver JDBC", e);
                }
            }
        }
    }
}
