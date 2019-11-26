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

			String url = "oracle-sample.db";
//			String url = "ciclistas";
			consultarMetaData(url);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void consultarMetaData(String url) {
		DAOExistenteDB daoSqlite;
		try {
			ConexionDBExistente.getConection(url);	//Crea la conexion
			ConexionDBNueva.crearConexion("nueva6");
			daoSqlite = DAOExistenteDB.getDao(url);
			daoSqlite.migrar(ConexionDBExistente.getMetadatos());
			ConexionDBNueva.realizarCommit();
			
		} catch (MigracionException e) {
			System.err.println(e.getMessage());
			ConexionDBNueva.realizarRollBack();
		} 

		ConexionDBNueva.cerrarConexion();
		ConexionDBExistente.cerrarConexion();

	}

}
