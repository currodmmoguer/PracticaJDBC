import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Principal {
	
	private static final String URL_BD = "jdbc:sqlite:";
	private static final String DRIVER = "org.sqlite.JDBC";

	public static void main(String[] args) {
		
		try {
			Class.forName(DRIVER);
			String url = URL_BD + "ejemploSqlite.db";
			DBExisteDAO.getDao(url);
			consulta(url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//Esta es
	private static void consulta(String url) {
		try {
			DatabaseMetaData dbmd = ConexionDB.getConection(url).getMetaData();
			String[] tipos = {"TABLE"};
			ResultSet resul = dbmd.getTables(null, "PUBLIC", null, tipos);
			
			System.out.println(dbmd.getDatabaseProductName());
			System.out.println(dbmd.getUserName());
			System.out.println(dbmd.getURL());
			while (resul.next()) {
				String nombreTabla = resul.getString("TABLE_NAME");
				System.out.println(nombreTabla);
			}
			System.out.println("kljgl");
		} catch (SQLException e) {
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
