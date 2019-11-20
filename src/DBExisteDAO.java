import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DBExisteDAO {
	private static final String URL = "jdbc:sqlite:";
	private static Connection conexion;
	private static DBExisteDAO dao;
	private static String[][] rel = {{"NVARCHAR"},{"VARCHAR"}};

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

		ResultSet resul = dbmd.getTables("main", "PUBLIC", null, tipos);

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
				String nula=columnas.getString("IS_NULLABLE");
//				String autoin = columnas.getString("IS_AUTOINCREMENT");
				String nulo = "";
				
				if (nula.equals("N"))
					nulo = " NOT NULL";
//				String numero = tipoCol.substring(tipoCol.indexOf("("), tipoCol.indexOf(")"));
				if (tipoCol.matches("^NVARCHAR")) {
//					String numero = tipoCol.substring(tipoCol.indexOf("("), tipoCol.indexOf(")"));
					nombreCol = "VARCHAR(";
				}


				//Comprobar autoin
				
				sb.append(nombreCol + " " + tipoCol + nulo  + ",");
			}
			sb.delete(sb.length()-1, sb.length());
			sb.append(");");
			System.out.println(sb.toString());
			DBNuevaDAO.addSentencia(sb.toString());
		}

	}
	
	/**
	 * Método que lee todas las claves primarias de una base de datos
	 * @param dbmd
	 * @throws SQLException
	 */
	public static void consultarClavesPrimarias(DatabaseMetaData dbmd) throws SQLException {
		String[] tipos = {"TABLE"};
		ResultSet tables = dbmd.getTables("main", "PUBLIC", null, tipos);
		ResultSet primeryKeys;
		
		while (tables.next()) {
	        String tableName = tables.getString("TABLE_NAME");
	        primeryKeys = dbmd.getPrimaryKeys(null, null, tableName);
	       
	        while (primeryKeys.next()) {
	        	String sentencia = "ALTER TABLE " + tableName + " ADD PRIMARY KEY(" + primeryKeys.getString("COLUMN_NAME") + ")";
	        	System.out.println(sentencia);
	        }
		}
	}
	

	/**
	 * Método que lee todas las claves ajenas de una base de datos
	 * @param dbmd Metadatos
	 * @throws SQLException
	 */
	public static void consultarClavesAjenas(DatabaseMetaData dbmd) throws SQLException {
		String[] tipos = {"TABLE"};
		ResultSet tables = dbmd.getTables("main", "PUBLIC", null, tipos);
		ResultSet foreingKeys;
		
		while (tables.next()) {
	        String tableName = tables.getString("TABLE_NAME");
	        foreingKeys = dbmd.getImportedKeys(null, null, tableName);
	        
	        while (foreingKeys.next()) {
	 	       String fkColumnName = foreingKeys.getString("FKCOLUMN_NAME");
	 	       String pkColumnName = foreingKeys.getString("PKCOLUMN_NAME");
	 	       String pkTableName = foreingKeys.getString("PKTABLE_NAME");
	 	       
	 	       String sentencia = "ALTER TABLE " + tableName + " ADD FOREIGN KEY (" + fkColumnName + ") REFERENCES " + pkTableName + "("+ pkColumnName +")";
	 	       System.out.println(sentencia);
	        }
		}
	}
	




}

