import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DAOExistenteDB {
	private static Connection conexion;
	private static DAOExistenteDB dao;

	private DAOExistenteDB() throws SQLException {
	}

	/**
	 * Devuelve el objeto DAO. Si si ya existe devuelve el existente
	 * 
	 * @param archivo
	 * @return DAO objeto
	 * @throws SQLException
	 */
	public static DAOExistenteDB getDao(String archivo) throws SQLException {

		if (conexion == null)
			dao = new DAOExistenteDB();

		return dao;
	}

	/**
	 * Obtiene todos los metadatos de la BD y lo ejecuta en la otra base de datos
	 * 
	 * @param dbmd
	 * @throws MigracionException 
	 * @throws SQLException
	 */
	public static void migrar(DatabaseMetaData dbmd) throws MigracionException {
		// Setautocommit(false) no funciona
		String strSentencia;
		String tabla;
		String[] tipos = { "TABLE" };
		
		try {
		ResultSet resul = dbmd.getTables(null, "PUBLIC", null, tipos);

		// Bucle por tablas
		while (resul.next()) {
			tabla = resul.getString("TABLE_NAME");
			// Se compruena que no sea una tabla propia de sqlite
			if (!tabla.startsWith("sqlite")) {
				strSentencia = sentenciaTabla(dbmd, tabla);
				System.out.println(strSentencia);
				DBNuevaDAO.addSentencia(strSentencia);
				migrarClavesPrimarias(dbmd, tabla);
			}
		}

		resul.close();
		migrarClavesAjenas(dbmd);
		} catch (SQLException e) {
			throw new MigracionException("Error en la migración de la base de datos.");
		}

	}

	/**
	 * Consulta las claves primarias y prepara la sentencia en MySQL
	 * 
	 * @param dbmd
	 * @param tableName nombre de la tabla que consulta
	 * @throws SQLException
	 */
	private static void migrarClavesPrimarias(DatabaseMetaData dbmd, String tableName) throws SQLException {
		ArrayList<String> claves = new ArrayList<String>();
		ResultSet primeryKeys = dbmd.getPrimaryKeys(null, null, tableName);

		while (primeryKeys.next()) {
			claves.add(primeryKeys.getString("COLUMN_NAME"));
		}

		primeryKeys.close();

		if (claves.size() > 0) {
			sentenciaClavePrimaria(claves, tableName);
			claves.clear();
		}

	}

	/**
	 * Método que lee todas las claves ajenas de una base de datos y la ejecuta en
	 * la otra
	 * 
	 * @param dbmd Metadatos
	 * @throws SQLException
	 */
	private static void migrarClavesAjenas(DatabaseMetaData dbmd) throws SQLException {

		Short update = -1, delete = -1; // Se inicializa a -1 para la condición de más abajo (si es -1 es que no tiene
		String[] tipos = { "TABLE" };
		ResultSet tables = dbmd.getTables("main", "PUBLIC", null, tipos);
		ResultSet foreingKeys;

		String fkColumnName, pkColumnName, pkTableName;
		while (tables.next()) {
			String tableName = tables.getString("TABLE_NAME");
			foreingKeys = dbmd.getImportedKeys(null, null, tableName);

			while (foreingKeys.next()) {
				fkColumnName = foreingKeys.getString("FKCOLUMN_NAME");
				pkColumnName = foreingKeys.getString("PKCOLUMN_NAME");
				pkTableName = foreingKeys.getString("PKTABLE_NAME");
				update = foreingKeys.getShort("UPDATE_RULE");
				delete = foreingKeys.getShort("DELETE_RULE");
				// Comprobar constrain DEFERRABILITY


				DBNuevaDAO.addSentencia(
						sentenciaClaveAjena(update, delete, fkColumnName, pkColumnName, pkTableName, tableName));

			}
			foreingKeys.close();
		}
		tables.close();
	}
	


	/**
	 * Crea la sentencia SQL para crear tabla obtenidos de los metadatos de la BD
	 * indicada
	 * 
	 * @param dbmd   Databasemetadata - metadatos de una base de datos
	 * @param nombre String - nombre de la tabla a crear
	 * @throws SQLException
	 */
	private static String sentenciaTabla(DatabaseMetaData dbmd, String tabla) throws SQLException {
		ResultSet columnas = dbmd.getColumns(null, "PUBLIC", tabla, null);
		StringBuilder sb = new StringBuilder("create table " + tabla + "(");
		String nombreCol, tipoCol, nula;

		while (columnas.next()) {
			nombreCol = columnas.getString("COLUMN_NAME");
			
			tipoCol = comprobarTipoColumna(columnas.getString("TYPE_NAME"));
			nula = isNullable(columnas.getString("IS_NULLABLE"));
			//String autoin = columnas.getString("IS_AUTOINCREMENT");

			sb.append(nombreCol + " " + tipoCol + nula + ",");
		}

		sb.delete(sb.length() - 1, sb.length());
		sb.append(");");
		columnas.close();

		return sb.toString();
	}

	/**
	 * Crea la sentencia para la clave primaria y la ejecuta en la otra BD
	 * 
	 * @param claves
	 * @param tableName
	 * @throws SQLException
	 */
	private static void sentenciaClavePrimaria(ArrayList<String> claves, String tableName) throws SQLException {
		StringBuilder sbSentencia = new StringBuilder();

		if (claves.size() == 1) {
			sbSentencia.append("ALTER TABLE " + tableName + " ADD PRIMARY KEY(" + claves.get(0) + ")");
		} else {
			sbSentencia.append("ALTER TABLE " + tableName + " ADD PRIMARY KEY(" + claves.get(0));

			for (int i = 1; i < claves.size(); i++) {
				sbSentencia.append(", " + claves.get(i));
			}

			sbSentencia.append(")");
		}

		System.out.println(sbSentencia);
		DBNuevaDAO.addSentencia(sbSentencia.toString());
	}

	/**
	 * Crea la sentencia SQL para añadir las claves ajenas
	 * 
	 * @param update       Acción aplicada a la clave externa (actualización)
	 * @param delete       Acción aplicada a la clave externa (eliminación)
	 * @param fkColumnName Nombre de clave externa
	 * @param pkColumnName Nombre de clave principal que hace referencia
	 * @param pkTableName  Nombre tabla de clave principal
	 * @param tableName    Nombre de la tabla
	 * @return sentencia SQL
	 * @throws SQLException
	 */
	private static String sentenciaClaveAjena(Short update, Short delete, String fkColumnName, String pkColumnName,
			String pkTableName, String tableName) throws SQLException {

		String[] rol = { "CASCADE", "RESTRICT", "SET NULL", "NO ACTION", "SET DEFAULT" };
		
		String restriccion = "FK" + tableName + "_" + pkTableName;
		StringBuilder sentencia = new StringBuilder("ALTER TABLE " + tableName + " ADD CONSTRAINT " + restriccion + " FOREIGN KEY (" + fkColumnName + ") REFERENCES "
				+ pkTableName + "(" + pkColumnName + ")");
		
		if (delete != -1)
			sentencia.append(" ON DELETE " + rol[delete]);

		if (update != -1)
			sentencia.append(" ON UPDATE " + rol[update]);

		System.out.println(sentencia);
		
		return sentencia.toString();
	}

	/**
	 * Comprueba tipos de datos y realiza la conversión a su equivalencia en MySQL
	 * 
	 * @param tipoCol tipo de dato en Sqlite
	 * @return tipo de dato en MySQL
	 */
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

	/**
	 * Realiza la conversión sobre lo que devuelve "IS_NULLABE" a una sentencia SQL
	 * 
	 * @param str Y | N, solo hace algo si es N
	 * @return si no permite nulo -> NOT NULL, si lo permite devuelve un string
	 *         vacío
	 */
	private static String isNullable(String str) {
		String nulo;

		if (str.equals("N"))
			nulo = " NOT NULL";
		else
			nulo = "";

		return nulo;
	}

}
