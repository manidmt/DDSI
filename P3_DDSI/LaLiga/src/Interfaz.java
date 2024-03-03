import Subsistemas.Arbitros;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.DriverManager;

import Subsistemas.Clasificacion;
import Subsistemas.Jugadores;
import Subsistemas.Partidos;
import Subsistemas.Clubes;
import Subsistemas.Entrenadores;


/**
 * @author Grupo D2JJ
 */

public class Interfaz{
    //Nuestra conexion a la BD
    private static Connection conn = null;
    private static String passAdmin = "WeLovDDSI";
    
    /**
     * Método para cerrar la conexion conn con la BD
     */
    private static boolean cerrarConexion(){
        if(conn != null){
            try{
                conn.close();
                System.out.println("Conexion con la BD cerrada con éxito");
                return true;
            }catch(SQLException e){
                System.out.println("No ha sido posible cerrar la conexion!");
                return false;
            }
        }
        else{
            return true;
        }
    }

    
    /**
     * Menú principal de nuestro sistema. Desde aqui llamamos a los otros
     * submenus de los subsistemas
     */
    public static void menuPrincipal(){
        //Presentacion del menu
        System.out.println("LaLiga S.A");
        System.out.println("----------");
        
        int opcion = -1; //Opcion que elige el usuario
        
        Scanner sc = new Scanner(System.in);
        
        //Mostramos el menu al usuario
        while(opcion != 7){
            System.out.println("1.- Gestión de clubes");
            System.out.println("2.- Gestión de partidos");
            System.out.println("3.- Gestión de jugadores");
            System.out.println("4.- Gestión de entrenadores");
            System.out.println("5.- Gestión de árbitros");
            System.out.println("6.- Gestión de la clasificación");
            System.out.println("7.- Salir");
            
            //Pedimos una opcion al usuario
            System.out.print("Opcion: ");
            opcion = sc.nextInt();
            
            //Pasamos a la lógica
            switch(opcion){
                case 1:
                    Clubes.menuPrincipalClubes();
                    break;
                case 2:
                    Partidos.menuPrincipalPartidos();
                    break;
                case 3:
                    Jugadores.menuJugadores();
                    break;
                case 4:
                    Entrenadores.menuPrincipalEntrenadores();
                    break;
                case 5:
                    Arbitros.menuPrincipalArbitro();
                    break;
                case 6:
                    //System.out.println("Funcionalidad no implementada, saliendo a menu principal");
                    Clasificacion.menuClasificacion();
                    break;
                    
                case 7:
                    if(cerrarConexion()){
                        System.out.println("Saliendo del sistema...");
                    }
                    break;
                    
                default: 
                    System.out.println("ERROR: Esa opcion no existe!");
            }
        }
    }
    
    /**
     * Main principal
     */
    public static void main(String[] args){
        try{
            //Conexion a la BD
            Class.forName("oracle.jdbc.OracleDriver");
            
            //URL Name, username and password for the server
            String dbURL = "";
            String username = "";
            String password = "";
            
            conn = DriverManager.getConnection(dbURL, username, password);
           
            
            if(conn == null){
                System.out.println("Imposible conectar con la base de datos!");
            }else{
                //Ponemos el autocommit a false
                conn.setAutoCommit(false);
                
                //Damos conexion a todos nuestros subsistemas
                Jugadores.setConnection(conn);
                Arbitros.setConnection(conn);
                Partidos.setConnection(conn);
                Entrenadores.setConnection(conn);
                Clubes.setConnection(conn);
                Clasificacion.setConnection(conn);

                
                //Menu principal de el sistema
                menuPrincipal();
            }
        }catch(ClassNotFoundException ex){
            ex.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
