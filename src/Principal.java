import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Principal {
	
	private static final String URL_BD = "jdbc:sqlite:";
	private static final String DRIVER = "org.sqlite.JDBC";

	public static void main(String[] args) {
		
		try {
			Class.forName(DRIVER);
			String url = URL_BD + "academia.db";
			consultarMetaData(url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void consultarMetaData(String url) {
		try ( Connection conexion = DriverManager.getConnection(url)) {

			DatabaseMetaData dbmd = conexion.getMetaData();// Creamos objeto
															// DatabaseMetaData
			DBExisteDAO.consultarMetaData(dbmd);
		
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
