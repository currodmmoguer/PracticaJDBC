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
	   
	   private ConexionDBNueva(String nombre) throws SQLException {
		   conexion = DriverManager.getConnection(DB_URL, USER, PASS);
		   Statement sentencia = conexion.createStatement();
		   sentencia.executeUpdate("CREATE DATABASE " + nombre);
	   }
	   
	   public static Connection getConnection() throws SQLException {
		   return conexion;
	   }
	   
	   public static Connection crearConexion(String nombre) throws SQLException {
		   if (conexion==null)
			   new ConexionDBNueva(nombre);
		   
		   return conexion;
	   }

}
