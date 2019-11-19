import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBExisteDAO {
	private static final String URL = "jdbc:sqlite:";
	private static Connection conexion;
	private static DBExisteDAO dao;

	private DBExisteDAO(String url) throws SQLException {
		this.conexion = ConexionDB.getConection(url);
	}

	public static DBExisteDAO getDao(String archivo) throws SQLException {
		String strUrl = URL + archivo;
		if (conexion == null) {
			dao = new DBExisteDAO(strUrl);
		}

		return dao;
	}

	public static void consultarMetaData(DatabaseMetaData dbmd) throws SQLException {
		StringBuilder sb = new StringBuilder();
		String[] tipos = {"TABLE"};
		ResultSet resul = resul = dbmd.getTables("LIBROS", "PUBLIC", null, tipos);
		ResultSet columnas = null;

		//Bucle por tablas
		while (resul.next()) {
			
			sb.delete(0, sb.length());

			String tabla = resul.getString("TABLE_NAME");
			
			sb.append("create table " + tabla + "(");
			
			
			columnas = dbmd.getColumns("LIBROS", "PUBLIC", tabla, null);
			while (columnas.next()) {
				String nombreCol = columnas.getString("COLUMN_NAME");
				String tipoCol = columnas.getString("TYPE_NAME");

				sb.append(nombreCol + " " + tipoCol + ",");
			}
			sb.delete(sb.length()-1, sb.length());
			sb.append(");");
			
			System.out.println(sb.toString());
		}

	}



	}

