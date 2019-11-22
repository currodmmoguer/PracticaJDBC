import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;



public class Principal {
	
	private static Scanner teclado = new Scanner(System.in);
	private static final String DRIVER = "org.sqlite.JDBC";

	public static void main(String[] args) {
		String nombreFicheroExiste, nombreDBNueva;
		try {
			
			Class.forName(DRIVER);
			
			
//			System.out.print("Introduce el nombre del archivo de la base de datos que quieres migrar: ");
//			nombreFicheroExiste = teclado.nextLine();
//			System.out.print("Introduce el nombre de la base de datos nueva: ");
//			nombreDBNueva = teclado.nextLine();
//			ConexionDBNueva.crearConexion("chino");
			
			
			
			String url = "chinook.db";
			consultarMetaData(url);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	



	private static void consultarMetaData(String url) {
		try {
		ConexionDBNueva.crearConexion("nueva9");
		DatabaseMetaData dbmd = ConexionDB.getConection(url).getMetaData();
		DBExisteDAO.migrar(dbmd);
		
		
		
		ConexionDB.cerrarConexion();
		ConexionDBNueva.cerrarConexion();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
