import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
	
	private static final String URL = "jdbc:sqlite:";
	private static Connection conexion;
	
	
	private ConexionDB(String url) throws SQLException {
		conexion = DriverManager.getConnection(url);
	}
	
	/**
	 * Devuelve la conexión de la base de datos. Si ya existe devuelve la existente
	 * @param nombre del archivo de la base de datos Sqlite. Hay que incluir la extensión (.db)
	 * @return conexion
	 * @throws SQLException
	 */
	public static Connection getConection(String archivo) throws SQLException {
		String strUrl = URL + archivo;
		if (conexion==null) {
			new ConexionDB(strUrl);
		}
		
		return conexion;
	}
	
	/**
	 * Cierra la conexion de la base de datos
	 * @throws SQLException
	 */
	public static void cerrarConexion() throws SQLException {
		if (conexion!=null)
			conexion.close();
	}

}