import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class DAOExistenteDB {
	private static Connection conexion;
	private static DAOExistenteDB dao;

	private DAOExistenteDB(String archivo) throws MigracionException {
		conexion = ConexionDBExistente.getConection(archivo);
	}

	/**
	 * Devuelve el objeto DAO. Si si ya existe devuelve el existente
	 * 
	 * @param archivo
	 * @return DAO objeto
	 * @throws MigracionException
	 */
	public static DAOExistenteDB getDao(String archivo) throws MigracionException {

		if (conexion == null)
			dao = new DAOExistenteDB(archivo);

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
		String tabla;
		String[] tipos = { "TABLE" };
		int numeroColumnas;
		ArrayList<Object> listaTipoColumnas;

		try {
			ResultSet resul = dbmd.getTables(null, "PUBLIC", null, tipos);

			// Bucle por tablas (no migra las claves ajenas)
			while (resul.next()) {
				tabla = resul.getString("TABLE_NAME");
				// Se compruena que no sea una tabla propia de sqlite
				if (!tabla.startsWith("sqlite")) {
					listaTipoColumnas = migrarTabla(dbmd, tabla);

					migrarDatos(dbmd, tabla, listaTipoColumnas);
					migrarClavesPrimarias(dbmd, tabla);
				}
			}

			resul.close();
			migrarClavesAjenas(dbmd);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MigracionException("Error en la migración de la base de datos.");
		}

	}

	private static void migrarDatos(DatabaseMetaData dbmd, String tabla, ArrayList<Object> listaTipoColumnas)
			throws SQLException {
		StringBuilder sbSentenciaInsert = new StringBuilder();
		String strSentenciaQuery = "SELECT * FROM " + tabla;
		Statement sentenciaQuery = conexion.createStatement();
		Statement sencenciaInsert = conexion.createStatement();
		ResultSet resultQuery = sentenciaQuery.executeQuery(strSentenciaQuery);
		int contadorColumna = 1;

		while (resultQuery.next()) {
			sbSentenciaInsert.append("INSERT INTO " + tabla + " VALUES (");
			System.out.println(listaTipoColumnas.size());

			while (contadorColumna <= listaTipoColumnas.size()) {

				if (resultQuery.getObject(contadorColumna) == null) {
					sbSentenciaInsert.append("null,");
				} else if (listaTipoColumnas.get(contadorColumna - 1).getClass() == String.class
						|| listaTipoColumnas.get(contadorColumna - 1).getClass() == Date.class) {

					if (resultQuery.getObject(contadorColumna).toString().indexOf("\"") == -1) {
						sbSentenciaInsert.append("\"" + resultQuery.getObject(contadorColumna) + "\",");
					} else {
						sbSentenciaInsert.append("'" + resultQuery.getObject(contadorColumna) + "',");
					}

				} else {
					sbSentenciaInsert.append(resultQuery.getObject(contadorColumna) + ",");
				}
				contadorColumna++;
			}
			// Cierra la sentencia
			sbSentenciaInsert.deleteCharAt(sbSentenciaInsert.length() - 1);
			sbSentenciaInsert.append(")");

			DAONuevaDB.addSentencia(sbSentenciaInsert.toString());

			contadorColumna = 1;
			sbSentenciaInsert.delete(0, sbSentenciaInsert.length());
		}

	}

	/**
	 * Crea la sentencia SQL y la ejecuta para crear tabla obtenidos de los
	 * metadatos de la BD indicada
	 * 
	 * @param dbmd   Databasemetadata - metadatos de una base de datos
	 * @param nombre String - nombre de la tabla a crear
	 * @throws MigracionException
	 */
	private static ArrayList<Object> migrarTabla(DatabaseMetaData dbmd, String tabla) throws MigracionException {
		StringBuilder sbSentencia = new StringBuilder("create table " + tabla + "(");
		String nombreCol, tipoCol, nula;
		ArrayList<Object> tipoColumnas = new ArrayList<Object>();

		try {
			ResultSet columnas = dbmd.getColumns(null, "PUBLIC", tabla, null);

			while (columnas.next()) {
				nombreCol = columnas.getString("COLUMN_NAME");
				tipoCol = comprobarTipoColumna(columnas.getString("TYPE_NAME"));
				nula = isNullable(columnas.getString("IS_NULLABLE"));
				// String autoin = columnas.getString("IS_AUTOINCREMENT");
				comprobarDato(tipoCol, tipoColumnas);
				sbSentencia.append(nombreCol + " " + tipoCol + nula + ",");
			}

			sbSentencia.delete(sbSentencia.length() - 1, sbSentencia.length());
			sbSentencia.append(");");
			columnas.close();

			DAONuevaDB.addSentencia(sbSentencia.toString());
		} catch (SQLException e) {
			throw new MigracionException("Error en la migración de la base de datos con la tabla \"" + tabla + "\"");
		}

		return tipoColumnas;
	}

	private static void comprobarDato(String data, ArrayList<Object> list) {

		// Comprueba solo los datos que hay que introducirlos entre comilla, los demas
		// pone un 0 simplemente
		// porque si se pusiera null al hacerle el .getClass() saldría
		// NullPointerException
		if (data.toUpperCase().startsWith("VARCHAR")) {
			list.add(new String());

		} else {
			if (data.toUpperCase().startsWith("DATE")) {
				list.add(new Date());
			} else {
				list.add(0);
			}

		}

	}

	/**
	 * Consulta las claves primarias y prepara la sentencia en MySQL
	 * 
	 * @param dbmd
	 * @param tableName nombre de la tabla que consulta
	 * @throws MigracionException
	 * @throws SQLException
	 */
	private static void migrarClavesPrimarias(DatabaseMetaData dbmd, String tableName) throws MigracionException {
		ArrayList<String> claves = new ArrayList<String>();

		try {
			ResultSet primeryKeys = dbmd.getPrimaryKeys(null, null, tableName);

			while (primeryKeys.next()) {
				claves.add(primeryKeys.getString("COLUMN_NAME"));
			}

			primeryKeys.close();

			if (claves.size() > 0) {
				sentenciaClavePrimaria(claves, tableName);
				claves.clear();
			}
		} catch (SQLException e) {
			throw new MigracionException("Error al migrar la/s clave/s primaria/s de la tabla \"" + tableName + "\"");
		}

	}

	/**
	 * Método que lee todas las claves ajenas de una base de datos y la ejecuta en
	 * la otra
	 * 
	 * @param dbmd Metadatos
	 * @throws MigracionException
	 * @throws SQLException
	 */
	private static void migrarClavesAjenas(DatabaseMetaData dbmd) throws MigracionException {
		String[] tipos = { "TABLE" };
		Short update, delete;
		ResultSet tables, foreingKeys;
		String tableName = null;
		String fkColumnName = null, pkColumnName, pkTableName;

		try {
			tables = dbmd.getTables(null, "PUBLIC", null, tipos);

			while (tables.next()) {
				tableName = tables.getString("TABLE_NAME");
				foreingKeys = dbmd.getImportedKeys(null, null, tableName);

				while (foreingKeys.next()) {
					fkColumnName = foreingKeys.getString("FKCOLUMN_NAME");
					pkColumnName = foreingKeys.getString("PKCOLUMN_NAME");
					pkTableName = foreingKeys.getString("PKTABLE_NAME");
					update = foreingKeys.getShort("UPDATE_RULE");
					delete = foreingKeys.getShort("DELETE_RULE");
					// Comprobar constrain DEFERRABILITY

					DAONuevaDB.addSentencia(
							sentenciaClaveAjena(update, delete, fkColumnName, pkColumnName, pkTableName, tableName));

				}
				foreingKeys.close();
			}
			tables.close();
		} catch (SQLException e) {
			throw new MigracionException(
					"Error en la migración de la base de datos (" + tableName + "-" + fkColumnName + ")");
		}
	}

	/**
	 * Crea la sentencia para la clave primaria y la ejecuta en la otra BD
	 * 
	 * @param claves
	 * @param tableName
	 * @throws MigracionException
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

		DAONuevaDB.addSentencia(sbSentencia.toString());

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
			String pkTableName, String tableName) {

		String[] rol = { "CASCADE", "RESTRICT", "SET NULL", "NO ACTION", "SET DEFAULT" };

		String restriccion = "FK" + tableName + "_" + pkTableName;
		StringBuilder sentencia = new StringBuilder("ALTER TABLE " + tableName + " ADD CONSTRAINT " + restriccion
				+ " FOREIGN KEY (" + fkColumnName + ") REFERENCES " + pkTableName + "(" + pkColumnName + ")");

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
