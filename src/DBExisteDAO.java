import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBExisteDAO {
	private static Connection conexion;
	private static DBExisteDAO dao;

	private DBExisteDAO() throws SQLException {
	}

	/**
	 * Devuelve el objeto DAO. Si si ya existe devuelve el existente
	 * @param archivo
	 * @return DAO objeto
	 * @throws SQLException
	 */
	public static DBExisteDAO getDao(String archivo) throws SQLException {

		if (conexion == null)
			dao = new DBExisteDAO();

		return dao;
	}

	/**
	 * Consulta
	 * 
	 * @param dbmd
	 * @throws SQLException
	 */
	public static void migrar(DatabaseMetaData dbmd) throws SQLException {
		//Setautocommit(false) no funciona
		String strSentencia;
		String tabla;
		String[] tipos = { "TABLE" };
		ResultSet resul = dbmd.getTables(null, "PUBLIC", null, tipos);
		ConexionDBNueva.getConnection().setAutoCommit(false);
		// Bucle por tablas
		while (resul.next()) {
			tabla = resul.getString("TABLE_NAME");
			// Se compruena que no sea una tabla propia de sqlite
			if (!tabla.startsWith("sqlite")) {
				strSentencia = crearSentencia(dbmd, tabla);
				System.out.println(strSentencia);
				DBNuevaDAO.addSentencia(strSentencia);
				consultarClavesPrimarias(dbmd, tabla);
			}
		}
		
		consultarClavesAjenas(dbmd);
		ConexionDBNueva.getConnection().commit();
		
	}

	/**
	 * Crea la sentencia SQL para crear tabla obtenidos de los metadatos de la BD indicada
	 * @param dbmd Databasemetadata - metadatos de una base de datos
	 * @param nombre String - nombre de la tabla a crear
	 * @throws SQLException
	 */
	private static String crearSentencia(DatabaseMetaData dbmd, String tabla) throws SQLException {
		ResultSet columnas;
		StringBuilder sb = new StringBuilder();

		sb.append("create table " + tabla + "(");
		columnas = dbmd.getColumns(null, "PUBLIC", tabla, null);

		while (columnas.next()) {
			String nombreCol = columnas.getString("COLUMN_NAME");
			String tipoCol = columnas.getString("TYPE_NAME");
			String nula = columnas.getString("IS_NULLABLE");
			// String autoin = columnas.getString("IS_AUTOINCREMENT");
			String nulo = "";

			if (nula.equals("N"))
				nulo = " NOT NULL";

			tipoCol = comprobarTipoColumna(tipoCol);
			// Comprobar autoin

			sb.append(nombreCol + " " + tipoCol + nulo + ",");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.append(");");

		return sb.toString();

	}

	private static void consultarClavesPrimarias(DatabaseMetaData dbmd, String tableName) throws SQLException {
		ArrayList<String> claves = new ArrayList<String>();
		ResultSet primeryKeys = dbmd.getPrimaryKeys(null, null, tableName);

		while (primeryKeys.next()) {
			claves.add(primeryKeys.getString("COLUMN_NAME"));
		}

		if (claves.size() > 0) {
			sentenciaClavePrimaria(claves, tableName);
			claves.clear();
		}

	}

	private static String comprobarTipoColumna(String tipoCol) {
		String numero, nombre = tipoCol;
		if (tipoCol.toUpperCase().startsWith("NVARCHAR") || tipoCol.startsWith("VARACHAR")
				|| tipoCol.toUpperCase().startsWith("VARCHAR2")) {
			numero = tipoCol.substring(tipoCol.indexOf("("), tipoCol.indexOf(")"));
			nombre = "VARCHAR" + numero + ")";
		}

		if (tipoCol.toUpperCase().startsWith("NUMBER")) {
			numero = tipoCol.substring(tipoCol.indexOf("("), tipoCol.indexOf(")"));
			nombre = "DECIMAL" + numero + ")";
		}

		return nombre;
	}


	private static void sentenciaClavePrimaria(ArrayList<String> claves, String tableName) throws SQLException {
		StringBuilder sentencia = new StringBuilder();

		if (claves.size() == 1) {
			sentencia.append("ALTER TABLE " + tableName + " ADD PRIMARY KEY(" + claves.get(0) + ")");
		} else {
			sentencia.append("ALTER TABLE " + tableName + " ADD PRIMARY KEY(" + claves.get(0));

			for (int i = 1; i < claves.size(); i++) {
				sentencia.append(", " + claves.get(i));
			}

			sentencia.append(")");
		}

		System.out.println(sentencia);
		DBNuevaDAO.addSentencia(sentencia.toString());

	}

	/**
	 * Método que lee todas las claves ajenas de una base de datos
	 * 
	 * @param dbmd Metadatos
	 * @throws SQLException
	 */
	private static void consultarClavesAjenas(DatabaseMetaData dbmd) throws SQLException {
		String[] rol = { "CASCADE", "RESTRICT", "SET NULL", "NO ACTION", "SET DEFAULT" };
		Short update = -1, delete = -1;
		String[] tipos = { "TABLE" };
		ResultSet tables = dbmd.getTables("main", "PUBLIC", null, tipos);
		ResultSet foreingKeys;
		StringBuilder sentencia = new StringBuilder();

		while (tables.next()) {
			String tableName = tables.getString("TABLE_NAME");
			foreingKeys = dbmd.getImportedKeys(null, null, tableName);

			while (foreingKeys.next()) {
				String fkColumnName = foreingKeys.getString("FKCOLUMN_NAME");
				String pkColumnName = foreingKeys.getString("PKCOLUMN_NAME");
				String pkTableName = foreingKeys.getString("PKTABLE_NAME");
				update = foreingKeys.getShort("UPDATE_RULE");
				delete = foreingKeys.getShort("DELETE_RULE");
				// Comprobar constrain DEFERRABILITY
				sentencia.append("ALTER TABLE " + tableName + " ADD FOREIGN KEY (" + fkColumnName + ") REFERENCES "
						+ pkTableName + "(" + pkColumnName + ")");

				if (delete != -1)
					sentencia.append(" ON DELETE " + rol[delete]);

				if (update != -1)
					sentencia.append(" ON UPDATE " + rol[update]);

				System.out.println(sentencia);
				DBNuevaDAO.addSentencia(sentencia.toString());
				sentencia.delete(0, sentencia.length());

			}
		}
	}

}
