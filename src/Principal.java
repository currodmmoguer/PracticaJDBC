import java.util.Scanner;

public class Principal {

	private static Scanner teclado = new Scanner(System.in);
	private static final String DRIVER = "org.sqlite.JDBC";

	public static void main(String[] args) {
		String nombreFicheroExiste, nombreDBNueva;
		try {

			Class.forName(DRIVER);

			System.out.print("Introduce el nombre del archivo de la base de datos que quieres migrar: ");
			nombreFicheroExiste = teclado.nextLine();
			System.out.print("Introduce el nombre de la base de datos nueva: ");
			nombreDBNueva = teclado.nextLine();

			consultarMetaData(nombreFicheroExiste, nombreDBNueva);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void consultarMetaData(String dbExistente, String dbNueva) {
		DAOExistenteDB daoSqlite;
		try {
			ConexionDBNueva.crearConexion(dbNueva);
			daoSqlite = DAOExistenteDB.getDao(dbExistente);
			daoSqlite.migrar(ConexionDBExistente.getMetadatos());
			ConexionDBNueva.realizarCommit();
			System.out.println("Se ha realizado la migración de la base de datos \"" + dbExistente + "\", ahora se llama \"" + dbNueva + "\" en MySQL");
			
		} catch (MigracionException e) {
			System.err.println(e.getMessage());
			ConexionDBNueva.realizarRollBack();
		} 

		ConexionDBNueva.cerrarConexion();
		ConexionDBExistente.cerrarConexion();

	}

}
