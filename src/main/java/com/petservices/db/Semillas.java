package com.petservices.db;

import com.petservices.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Datos iniciales de PetServices.
 *
 * Se cargan UNA SOLA VEZ, cuando la base de datos se crea por primera vez.
 * Conservan los mismos identificadores y valores que tenían los bloques
 * {@code static} de los DAO en memoria, para no romper las cuentas de prueba,
 * las relaciones existentes ni las pruebas automatizadas.
 *
 * Si la base ya contiene datos (por ejemplo, una restauración), no se inserta
 * nada: la marca {@code semillas_cargadas} evita duplicados en cada arranque.
 */
public final class Semillas {

    private static final String CLAVE_SEMILLAS = "semillas_cargadas";

    private Semillas() {
    }

    /** Inserta las semillas si la base está vacía y nunca se han cargado. */
    public static void cargar(Connection conexion) throws SQLException {
        if (configuracion(conexion, CLAVE_SEMILLAS) != null) {
            return;
        }

        if (hayDatos(conexion)) {
            guardarConfiguracion(conexion, CLAVE_SEMILLAS, "1");
            return;
        }

        insertarClientes(conexion);
        insertarEstablecimientos(conexion);
        insertarServicios(conexion);
        insertarMascotas(conexion);
        insertarCitas(conexion);
        insertarProductos(conexion);
        insertarPedidos(conexion);
        insertarDetalles(conexion);

        guardarConfiguracion(conexion, CLAVE_SEMILLAS, "1");
        guardarConfiguracion(conexion, "version_datos", "1");
        guardarConfiguracion(conexion, "retencion_copias", "10");

        ajustarSecuencias(conexion);
    }

    private static boolean hayDatos(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM clientes")) {
            return resultado.next() && resultado.getInt(1) > 0;
        }
    }

    private static String configuracion(Connection conexion, String clave) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "SELECT valor FROM configuracion WHERE clave = ?")) {
            sentencia.setString(1, clave);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getString(1) : null;
            }
        }
    }

    private static void guardarConfiguracion(Connection conexion, String clave, String valor)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "INSERT INTO configuracion (clave, valor) VALUES (?, ?) "
                        + "ON CONFLICT(clave) DO UPDATE SET valor = excluded.valor")) {
            sentencia.setString(1, clave);
            sentencia.setString(2, valor);
            sentencia.executeUpdate();
        }
    }

    private static void insertarClientes(Connection conexion) throws SQLException {
        String sql = "INSERT INTO clientes (id_cliente, nombre, correo, contrasena, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?)";
        insertarCliente(conexion, sql, 1, "Alejandro Puerto", "alejandro@gmail.com", "2026-01-15");
        insertarCliente(conexion, sql, 2, "Daniel Mejia", "daniel@gmail.com", "2026-01-16");
        insertarCliente(conexion, sql, 3, "David Yaya", "david@gmail.com", "2026-01-17");
    }

    private static void insertarCliente(Connection conexion, String sql, int id, String nombre,
                                        String correo, String fecha) throws SQLException {
        Jdbc.ejecutar(conexion, sql, id, nombre, correo, PasswordUtil.hashear("123456"), fecha);
    }

    private static void insertarEstablecimientos(Connection conexion) throws SQLException {
        String sql = "INSERT INTO establecimientos "
                + "(id_establecimiento, nombre, tipo, direccion, telefono) VALUES (?, ?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, "Veterinaria San Juan", "veterinaria",
                "Calle 15 #23-45 Tunja", "3204567890");
        Jdbc.ejecutar(conexion, sql, 2, "Pet Shop Amigos", "petshop",
                "Carrera 10 #8-20 Tunja", "3109876543");
        Jdbc.ejecutar(conexion, sql, 3, "Groomer Happy Pets", "mixto",
                "Av. Oriental #45-67 Tunja", "3187654321");
    }

    private static void insertarServicios(Connection conexion) throws SQLException {
        String sql = "INSERT INTO servicios (id_servicio, tipo, nombre, id_mascota) "
                + "VALUES (?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, "Consulta", "Vacunación antirrábica", null);
        Jdbc.ejecutar(conexion, sql, 2, "Baño y corte", "Grooming completo", null);
        Jdbc.ejecutar(conexion, sql, 3, "Cirugía", "Esterilización", null);
    }

    private static void insertarMascotas(Connection conexion) throws SQLException {
        String sql = "INSERT INTO mascotas "
                + "(id_mascota, nombre, especie, raza, edad, id_cliente, nombre_cliente, id_servicio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, "Max", "Perro", "Labrador", 5, 1, "Alejandro Puerto", null);
        Jdbc.ejecutar(conexion, sql, 2, "Luna", "Gato", "Siamés", 3, 2, "Daniel Mejia", null);
        Jdbc.ejecutar(conexion, sql, 3, "Rocky", "Perro", "Golden Retriever", 4, 1, "Alejandro Puerto", null);
        Jdbc.ejecutar(conexion, sql, 4, "Thor", "Perro", "Pastor Alemán", 2, 3, "David Yaya", null);
    }

    private static void insertarCitas(Connection conexion) throws SQLException {
        String sql = "INSERT INTO citas "
                + "(id_cita, id_cliente, nombre_cliente, id_mascota, nombre_mascota, "
                + " fecha_hora, servicio, estado, id_establecimiento, id_servicio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, 1, "Alejandro Puerto", 1, "Max",
                "2026-08-20T10:00", "Consulta", "Confirmada", null, null);
        Jdbc.ejecutar(conexion, sql, 2, 2, "Daniel Mejia", 2, "Luna",
                "2026-08-22T14:30", "Vacunación", "Pendiente", null, null);
        Jdbc.ejecutar(conexion, sql, 3, 1, "Alejandro Puerto", 3, "Rocky",
                "2026-08-25T09:00", "Baño/Peluquería", "Pendiente", null, null);
        Jdbc.ejecutar(conexion, sql, 4, 3, "David Yaya", 4, "Thor",
                "2026-08-28T11:00", "Cirugía", "Cancelada", null, null);
    }

    private static void insertarProductos(Connection conexion) throws SQLException {
        String sql = "INSERT INTO productos "
                + "(id_producto, nombre, categoria, precio, stock, descripcion, estado, "
                + " id_establecimiento, id_cliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, "Croquetas Pedigree Adulto 10kg", "Alimento",
                45000.00, 30, "Alimento", "disponible", null, null);
        Jdbc.ejecutar(conexion, sql, 2, "Shampoo Antipulgas", "Medicamento",
                18000.00, 50, "Medicamento", "disponible", null, null);
        Jdbc.ejecutar(conexion, sql, 3, "Collar Reflectivo", "Accesorio",
                25000.00, 15, "Accesorio", "disponible", null, null);
        Jdbc.ejecutar(conexion, sql, 4, "Juguete Mordedor", "Juguete",
                22000.00, 40, "Juguete", "disponible", null, null);
    }

    private static void insertarPedidos(Connection conexion) throws SQLException {
        String sql = "INSERT INTO pedidos "
                + "(id_pedido, id_cliente, nombre_cliente, fecha, total, estado, cantidad, "
                + " id_establecimiento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, 1, "Alejandro Puerto", "2026-08-10",
                90000.00, "Pagado", 2, null);
        Jdbc.ejecutar(conexion, sql, 2, 2, "Daniel Mejia", "2026-08-12",
                25000.00, "Entregado", 1, null);
    }

    private static void insertarDetalles(Connection conexion) throws SQLException {
        String sql = "INSERT INTO detalle_pedido "
                + "(id_detalle, id_pedido, id_producto, nombre_producto, cantidad, "
                + " precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Jdbc.ejecutar(conexion, sql, 1, 1, 1, "Croquetas Pedigree Adulto 10kg",
                2, 45000.00, 90000.00);
        Jdbc.ejecutar(conexion, sql, 2, 2, 3, "Collar Reflectivo",
                1, 25000.00, 25000.00);
    }

    /**
     * Ajusta {@code sqlite_sequence} tras insertar IDs explícitos. Así el
     * siguiente registro generado continúa después del máximo existente y no
     * reutiliza identificadores de registros eliminados.
     */
    private static void ajustarSecuencias(Connection conexion) throws SQLException {
        String[][] tablas = {
                {"clientes", "id_cliente"},
                {"establecimientos", "id_establecimiento"},
                {"servicios", "id_servicio"},
                {"mascotas", "id_mascota"},
                {"citas", "id_cita"},
                {"productos", "id_producto"},
                {"pedidos", "id_pedido"},
                {"detalle_pedido", "id_detalle"}
        };
        for (String[] tabla : tablas) {
            String nombreTabla = tabla[0];
            String id = tabla[1];
            try (Statement sentencia = conexion.createStatement()) {
                int actualizadas = sentencia.executeUpdate(
                        "UPDATE sqlite_sequence SET seq = "
                                + "(SELECT COALESCE(MAX(" + id + "), 0) FROM " + nombreTabla + ") "
                                + "WHERE name = '" + nombreTabla + "'");
                if (actualizadas == 0) {
                    sentencia.executeUpdate(
                            "INSERT INTO sqlite_sequence (name, seq) "
                                    + "SELECT '" + nombreTabla + "', COALESCE(MAX(" + id + "), 0) FROM "
                                    + nombreTabla);
                }
            }
        }
    }
}
