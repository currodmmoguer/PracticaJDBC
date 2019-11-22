import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDBNueva {
	
	
	   private static final String DB_URL = "jdbc:mysql://localhost/";

	   //  Database credentials
	   private static final String USER = "root";
	   private static final String PASS = "";
	   
	   private static Connection conexion;
	   
	   /**
	    * Constructor de la conexión de la nueva base de datos a crear.
	    * @param nombre de la base de datos
	    * @throws SQLException
	    */
	   private ConexionDBNueva(String nombre) throws SQLException {
		   conexion = DriverManager.getConnection(DB_URL, USER, PASS);
		   Statement sentencia = conexion.createStatement();
		   sentencia.executeUpdate("CREATE DATABASE " + nombre);
		   sentencia.executeUpdate("USE " + nombre);
		   sentencia.close();
	   }
	   
	   public static Connection getConnection() throws SQLException {
		   return conexion;
	   }
	   
	   /**
	    * Crea la conexion con la base de datos. Si ya existe devuelve la existente.
	    * @param nombre de la base de datos
	    * @return conexion
	    * @throws SQLException
	    */
	   public static Connection crearConexion(String nombre) throws SQLException {
		   if (conexion==null)
			   new ConexionDBNueva(nombre);
		   
		   return conexion;
	   }
	   
	   /**
	    * Cierra la conexion
	    * @throws SQLException
	    */
	   public static void cerrarConexion() throws SQLException {
		   if (conexion!=null)
			   conexion.close();
	   }

}
