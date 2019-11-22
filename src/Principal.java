import java.sql.SQLException;
import java.util.Scanner;

public class Principal {

	private static Scanner teclado = new Scanner(System.in);
	private static final String DRIVER = "org.sqlite.JDBC";

	public static void main(String[] args) {
		String nombreFicheroExiste, nombreDBNueva;
		try {

			Class.forName(DRIVER);

			// Bucle que cuando la haga pregunte si hacer otra o salir
//			System.out.print("Introduce el nombre del archivo de la base de datos que quieres migrar: ");
//			nombreFicheroExiste = teclado.nextLine();
//			System.out.print("Introduce el nombre de la base de datos nueva: ");
//			nombreDBNueva = teclado.nextLine();

//			String url = "oracle-sample.db";
			String url = "chinook.db";
			consultarMetaData(url);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void consultarMetaData(String url) {
		try {
			ConexionDBNueva.crearConexion("nueva5");
			DBExisteDAO.migrar(ConexionDB.getConection(url).getMetaData());
			ConexionDBNueva.getConnection().commit();

		} catch (SQLException e) {
			e.printStackTrace();
			ConexionDBNueva.realizarRollBack();
		} finally {
			ConexionDBNueva.cerrarConexion();
			ConexionDB.cerrarConexion();
		}
	}

}
