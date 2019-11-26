import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DAONuevaDB {
	
	private static DAONuevaDB dao;
	private static Connection conexion;
	
	private DAONuevaDB() throws SQLException {
		conexion = ConexionDBNueva.getConnection();
		conexion.setAutoCommit(false);
	}
	
	public static DAONuevaDB getDao() throws SQLException {
		if (dao==null)
			dao = new DAONuevaDB();
		
		return dao;
	}
	
	/**
	 * Ejecuta una sentencia en la base de datos
	 * @param sentencia SQL
	 * @throws SQLException
	 */
	public static void ejecutarSentencia(String sentencia) throws SQLException {
		System.out.println(sentencia);
		Statement sent = ConexionDBNueva.getConnection().createStatement();
		sent.executeUpdate(sentencia);
		sent.close();
		
	}

}
