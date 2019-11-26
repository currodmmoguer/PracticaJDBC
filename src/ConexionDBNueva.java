import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDBNueva {

	private static final String DB_URL = "jdbc:mysql://localhost/";

	// Database credentials
	private static final String USER = "root";
	private static final String PASS = "";
	private static Connection conexion;
	private static String nombreDB;

	/**
	 * Constructor de la conexión de la nueva base de datos a crear.
	 * 
	 * @param nombre de la base de datos
	 * @throws MigracionException 
	 * @throws SQLException
	 */
	private ConexionDBNueva(String nombre) throws MigracionException {

		//Se utiliza 2 try/catch para mostrar 2 mensajes distintos
		try {
			conexion = DriverManager.getConnection(DB_URL, USER, PASS);
			conexion.setAutoCommit(false);
		} catch (SQLException e) {
			throw new MigracionException("Error. No se ha podido conectarse a la base de datos MySQL.");
		}
			
		try {
			Statement sentencia = conexion.createStatement();
			sentencia.executeUpdate("CREATE DATABASE " + nombre);
			sentencia.executeUpdate("USE " + nombre);
			sentencia.close();
			nombreDB = nombre;
		} catch (SQLException e) {
			throw new MigracionException("Error. No se ha podido crear la base de datos " + nombre + " porque ya existe.");
			
		}
			

	}

	public static Connection getConnection() throws SQLException {
		return conexion;
	}

	/**
	 * Crea la conexion con la base de datos. Si ya existe devuelve la existente.
	 * 
	 * @param nombre de la base de datos
	 * @return conexion
	 * @throws MigracionException 
	 */
	public static Connection crearConexion(String nombre) throws MigracionException {
		if (conexion == null)
			new ConexionDBNueva(nombre);

		return conexion;
	}

	/**
	 * Cierra la conexion
	 * 
	 * @throws SQLException
	 */
	public static void cerrarConexion() {
		if (conexion != null)
			try {
				conexion.close();
			} catch (SQLException e) {
				System.err.println("Error al cerrar la conexion en la base de datos " + nombreDB);
			}
	}
	
	/**
	 * Realiza commit en la base de datos
	 */
	public static void realizarCommit() {
		if (conexion != null) {
			try {
				conexion.commit();
			} catch (SQLException e) {
				System.err.println("ERROR AL REALIZAR COMMIT");
			}
		}
	}

	/**
	 * Realiza rollback en la base de datos
	 */
	public static void realizarRollBack() {
		if (conexion != null) {
			try {
				conexion.rollback();
			} catch (SQLException e1) {
				System.err.println("ERROR AL REALIZAR ROLLBACK");
			}
		}
	}

}
