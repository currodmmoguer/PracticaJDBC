import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDBExistente {
	
	private static final String URL = "jdbc:sqlite:";
	private static Connection conexion;
	private static String nombreDB;
	
	
	private ConexionDBExistente(String url) throws MigracionException{
		
		try {
			conexion = DriverManager.getConnection(url);
			nombreDB = url;
		} catch (SQLException e) {
			throw new MigracionException("Error. No se ha podido conectar a la base de datos " + url);
		}
	}
	
	/**
	 * Devuelve la conexión de la base de datos. Si ya existe devuelve la existente
	 * @param nombre del archivo de la base de datos Sqlite. Hay que incluir la extensión (.db)
	 * @return conexion
	 * @throws MigracionException 
	 * @throws SQLException
	 */
	public static Connection getConection(String archivo) throws MigracionException{
		String strUrl = URL + archivo;
		if (!hayConexion()) 
			new ConexionDBExistente(strUrl);
		
		return conexion;
	}
	
	public static DatabaseMetaData getMetadatos() throws MigracionException {
		DatabaseMetaData dbmd = null;
		
		if (hayConexion()) {
			try {
				dbmd = conexion.getMetaData();
			} catch (SQLException e) {
				throw new MigracionException("Error. No se ha podido obtener los metadatos de la base de datos.");
			}
		}
		
		return dbmd;
	}
	

	
	/**
	 * Cierra la conexion de la base de datos
	 * @throws SQLException
	 */
	public static void cerrarConexion() {
		if (hayConexion())
			try {
				conexion.close();
			} catch (SQLException e) {
				System.out.println("No se ha podido cerrar la conexión de la base de datos " + nombreDB);
			}
	}
	
	/**
	 * Comprueba si hay conexion con la base de datos o no
	 * @return true si hay conexión, falso si no exite conexión
	 */
	private static boolean hayConexion() {
		return conexion!=null;
	}

}