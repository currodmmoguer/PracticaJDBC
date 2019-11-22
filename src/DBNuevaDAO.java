import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBNuevaDAO {
	
	private static DBNuevaDAO dao;
	private static Connection conexion;
	
	private DBNuevaDAO() throws SQLException {
		conexion = ConexionDBNueva.getConnection();
		conexion.setAutoCommit(false);
	}
	
	public static DBNuevaDAO getDao() throws SQLException {
		if (dao==null)
			dao = new DBNuevaDAO();
		
		return dao;
	}
	
	/**
	 * Ejecuta una sentencia en la base de datos
	 * @param sentencia SQL
	 * @throws SQLException
	 */
	public static void addSentencia(String sentencia) throws SQLException {
		Statement sent = ConexionDBNueva.getConnection().createStatement();
		sent.executeUpdate(sentencia);
		sent.close();
		
	}

}
