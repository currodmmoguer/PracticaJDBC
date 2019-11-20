import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
	
	private static final String URL = "jdbc:sqlite:";
	private static Connection conexion;
	
	private ConexionDB(String url) throws SQLException {
		conexion = DriverManager.getConnection(url);
	}
	
	public static Connection getConection(String archivo) throws SQLException {
		String strUrl = URL + archivo;
		if (conexion==null) {
			new ConexionDB(strUrl);
		}
		
		return conexion;
	}
	
	public static Connection getConnection() throws SQLException {
		return conexion;
	}
	
	public static void cerrarConexion() throws SQLException {
		if (conexion!=null)
			conexion.close();
	}

}