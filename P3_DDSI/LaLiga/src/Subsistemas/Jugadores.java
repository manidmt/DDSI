package Subsistemas;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.sql.Savepoint;
import Subsistemas.Clubes;

/**
 *
 * @author Javier Gómez López
 */
public class Jugadores {
    
    /**
     * Conexion a nuestra BD
     */
    private static Connection conn;
    
    /**
     * Método para borrar todas tablas asociadas al subsistema
     */
    public static void borrarSistemaJugadores(){
        try{
            Statement smt = conn.createStatement();
            smt.execute("DELETE FROM Jugadores");
            smt.execute("DROP TABLE Jugadores");
            conn.commit();
        }catch(SQLException ex){
            System.out.println("No ha sido posible eliminar el subsistema Jugadores!\n");
        }
        
    }
    
    /**
     * Método para crear las tablas del Subsitema jugadores
     */
    public static void inicializarSistemaJugadores(){
        try{
            Statement smt = conn.createStatement();
            smt.execute("CREATE TABLE JUGADORES ("
                    + "DNI_Jugador VARCHAR(9) PRIMARY KEY,"
                    + "Nombre VARCHAR(20) NOT NULL,"
                    + "Apellido_Paterno VARCHAR(20) NOT NULL,"
                    + "Apellido_Materno VARCHAR(20),"
                    + "Estatura NUMBER(3),"
                    + "Fecha_nacimiento DATE NOT NULL,"
                    + "Nacionalidad VARCHAR(10))");
            
            //Creamos el disparador
            smt.execute("CREATE OR REPLACE TRIGGER validar_edad_jugador "
                    + "BEFORE INSERT ON Jugadores "
                    + "FOR EACH ROW "
                    + "DECLARE fecha_nacimiento_valida BOOLEAN; "
                    + "BEGIN "
                    + "fecha_nacimiento_valida := (SYSDATE - :NEW.fecha_nacimiento) >= 6570; "
                    + "IF NOT fecha_nacimiento_valida THEN "
                    + "RAISE_APPLICATION_ERROR(-20001, 'El jugador debe de ser mayor de edad!'); "
                    + "END IF; "
                    + "END");
            conn.commit();
        }catch(SQLException ex){
            System.out.println("No ha sido posible inicializar el subsistema de Jugadores!\n");
        }
    }
    
    /**
     * Metodo para setear nuestra conexion
     * @param conn1 Connection conexion a nuestra BD
     */
    public static void setConnection(Connection conn1){
        conn = conn1;
    }
    
    /**
     * Método que comprueba si un DNI es correcto
     * @param dni String DNI a comprobar
     * @return True si es correcto, False en caso contrario
     */
    private static boolean checkDNI(String dni){
        //Comenzamos comprobando si tiene la estructura correcta
        if(dni.length() != 9 || Character.isLetter(dni.charAt(8)) == false){
            return false;
        }
        
        //Supuesta ultima letra de mi DNI
        String letraMayuscula = dni.substring(8).toUpperCase();
        
        //COMPROBACION DE QUE LOS 8 PRIMEROS SON NUMEROS
        String[] unoNueve = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String miDNI = "";
        
        for(int i = 0; i < dni.length() - 1; i++){
            String numero = dni.substring(i,i+1);
            
            for (String unoNueve1 : unoNueve) {
                if (numero.equals(unoNueve1)) {
                    miDNI += unoNueve1;
                }
            }
        }
        
        if(miDNI.length() != 8){
            return false;
        }
        
        //COMPROBACION DE QUE LA LETRA ES LA CORRECTA
        int numDNI = Integer.parseInt(dni.substring(0,8));
     
        int resto = 0;
        resto = numDNI % 23;
        
        String[] asignacionLetra = {"T", "R", "W", "A", "G", "M", "Y", "F", "P", "D", "X", "B", "N", "J", "Z", "S", "Q", "V", "H", "L", "C", "K", "E"};
        
        return letraMayuscula.equals(asignacionLetra[resto]) != false;
        
    }
    
    /**
     * Método que indica si un jugador (DNI) ya existe en la base de datos
     * @param dni String DNI del jugador que se desea comprobar
     * @return true si existe, false en caso contrario
     */
    
    public static boolean playerExists(String dni){
        try{
            String query = "SELECT DNI_Jugador FROM JUGADORES WHERE DNI_Jugador=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dni);
            
            ResultSet rs = pstmt.executeQuery();
            
            if(rs.next()){
                return true;
            }
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Alta de nuevo jugador con todo su prompt
     */
    private static void altaNuevoJugador(){
        try{
            //Input
            Scanner sc = new Scanner(System.in);
            
            //Preparacion de la query
            String query = "INSERT INTO JUGADORES (DNI_Jugador, Nombre,"
                    + "Apellido_Paterno, Apellido_Materno, Estatura, Fecha_nacimiento,"
                    + "Nacionalidad) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ptsmt = conn.prepareStatement(query);
            
            
            //Toma de datos del usuario
            System.out.println("\nALTA NUEVO JUGADOR");
            System.out.println("------------------");
            
            String DNI, nombre, paterno, materno, nacionalidad, nacimiento;
            int estatura;
            
                
            System.out.print("Introduce el DNI del nuevo Jugador: ");
            DNI = sc.nextLine();
                
//            if(!checkDNI(DNI)){
//                System.out.println("El DNI introducido no es correcto, saliendo...\n");
//                return;
//            }
            if(playerExists(DNI)){
                System.out.println("Ya existe un jugador con ese DNI, saliendo...\n");
                return;
            }
                
            System.out.print("Introduzca el nombre del jugador: ");
            nombre = sc.nextLine();
            
            if(nombre.equals("")){
                System.out.println("El nombre no puede ser vacío, saliendo...\n");
                return;
            }
            
            System.out.print("Introduce el primer apellido del jugador: ");
            paterno = sc.nextLine();
            
            if(paterno.equals("")){
                System.out.println("El primer apellido no puede ser vacío, saliendo...\n");
                return;
            }
            
            System.out.print("Introduce la fecha de nacimiento (dd mm yyyy): ");
            nacimiento = sc.nextLine();
            
            //Parseamos la fecha
            java.util.Date cumple;
            Date sqlCumple = null;
            try{
                cumple = new SimpleDateFormat("dd MM yyyy").parse(nacimiento);
                sqlCumple = new Date(cumple.getTime());
            }catch(ParseException ex){
                System.out.println("Formato de la fecha incorrecto!");
                return;
            }
            
            //CAMPOS OPCIONALES
            System.out.print("Introduzca su segundo apellido (opcional): ");
            materno = sc.nextLine();
            
            if(materno.equals("")){
                materno = null;
            }
            
            System.out.print("Introduzca su estatura en centímetros (opcional): ");
            String sEstatura = sc.nextLine();
                    
            if(sEstatura.equals("")){
                estatura = 0;
            }else{
                estatura = Integer.parseInt(sEstatura);
            }
            
            System.out.print("Introduce la nacionalidad (opcional): ");
            nacionalidad = sc.nextLine();
            
            if(nacionalidad.equals("")){
                nacionalidad = null;
            }
            
            //Completamos la query con los datos
            ptsmt.setString(1, DNI);
            ptsmt.setString(2, nombre);
            ptsmt.setString(3, paterno);
            ptsmt.setString(4, materno);
            
            if(estatura == 0){
                ptsmt.setString(5, null);
            }
            else{
                ptsmt.setInt(5, estatura);
            }
            ptsmt.setDate(6, sqlCumple);
            ptsmt.setString(7, nacionalidad);

            //Realizamos la query y salimos
            
            ptsmt.executeQuery();
            
            conn.commit();
            System.out.println("Jugador registrado correctamente!\n");
            
        }catch(SQLException e){
            if(e.getErrorCode() == 20001){
                System.out.println("ERROR: El jugador debe de ser mayor de edad!\n");
            }
        }
    }
    
    /**
     * Método para dar de baja un nuevo jugador
     */
    private static void bajaJugador(){
        try{
            //Input
            Scanner sc = new Scanner(System.in);
           
            
            //Preparacion de la query
            String queryBorrado = "DELETE FROM JUGADORES WHERE DNI_Jugador = ?";
            PreparedStatement pstmt = conn.prepareStatement(queryBorrado);
            
            //Tomo el DNI al usuario
            System.out.println("\nBORRAR JUGADOR");
            System.out.println("--------------");
            
            System.out.print("Introduce el DNI del jugador a eliminar: ");
            String DNI = sc.nextLine();
            
            if(!playerExists(DNI)){
                System.out.println("No existe un jugador con ese DNI, saliendo...\n");
                return;
            }
            
            //Obtenemos el nombre y el apellido del jugador a eliminar
            String queryConsulta = "SELECT nombre, apellido_paterno FROM JUGADORES WHERE DNI_Jugador = ?";
            PreparedStatement consulta = conn.prepareStatement(queryConsulta);
            consulta.setString(1, DNI);
            ResultSet rs = consulta.executeQuery();
            
            //Borramos el jugador
            pstmt.setString(1, DNI);
            pstmt.executeQuery();
            
            conn.commit();
            
            rs.next();
            System.out.print("Se ha eliminado al jugador " + rs.getString("nombre") + " ");
            System.out.println(rs.getString("apellido_paterno") + " del sistema!\n");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * Función que muestra un listado de jugadores
     */
    private static void listadoJugadores(){
        //Presentacion del prompt
        try{
            System.out.println("Listado de jugadores de la LaLiga");
            System.out.println("---------------------------------\n");
        
            System.out.println("DNI\t\t|\tNombre\t|\tPrimer Apellido\t|\tSegundo Apellido\t|\tEstatura\t|\tFecha de nacimiento\t|\tNacionalidad");
            System.out.println("---\t\t \t------\t \t---------------\t \t----------------\t \t--------\t \t-------------------\t \t");

            //Preparcion de la query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Jugadores");
            
            while(rs.next()){
                String to_show = rs.getString("DNI_Jugador") + "\t \t" + rs.getString("nombre") + 
                        "\t \t" + rs.getString("apellido_paterno") + "\t \t\t";
                
                if(rs.getString("apellido_materno") == null){
                    to_show += "\t \t\t\t";
                }
                else{
                    to_show += rs.getString("apellido_materno") + "\t \t\t\t";
                }
                
                if(rs.getInt("estatura") == 0){
                    to_show += "\t \t\t";
                }
                else{
                    to_show += rs.getInt("estatura") + "\t \t\t";
                }
                
                to_show += rs.getString("fecha_nacimiento").split(" ")[0] + "\t \t\t";
                
                if(rs.getString("nacionalidad") != null){
                    to_show += rs.getString("nacionalidad");
                }
                
                System.out.println(to_show);
            }
            
            System.out.println();
            
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * Función que permite modificar los datos de un jugador
     */
    private static void modificarJugador(){
        try{
            //Input
            Scanner sc = new Scanner(System.in);
            
            //Pedimos el DNI al usuario
            String DNI;
            System.out.print("Introduce el DNI del jugador a modificar: ");
            DNI = sc.nextLine();
            
            if(!playerExists(DNI)){
                System.out.println("No existe el jugador con ese DNI, saliendo...\n");
                return;
            }
            
            //INICIO DE TRANSACCIÓN DE MODIFICACIÓN
            Savepoint st_inicial = conn.setSavepoint();
            
            //Savepoint despues de modificacion
            Savepoint tras_modificar = null;
            
            //Menú
            boolean keep = true;
            
            while(keep){
                int option;
                
                //Interfaz para el usuario
                System.out.println("\nMODIFICAR DATOS DE JUGADOR " + DNI);
                System.out.println("----------------------------");
                
                System.out.println("1.- Modificar nombre");
                System.out.println("2.- Modificar primer apellido");
                System.out.println("3.- Modificar segundo apellido");
                System.out.println("4.- Modificar estatura");
                System.out.println("5.- Modificar fecha de nacimiento");
                System.out.println("6.- Modificar nacionalidad");
                System.out.println("7.- Guardar y salir");
                System.out.println("8.- Salir sin guardar");
                System.out.print("Opcion: ");
                
                option = sc.nextInt();
                
                //Statement
                PreparedStatement pstmt = null;
                
                switch(option){
                    case 1:
                        String nombre;
                        System.out.print("Introduce el nuevo nombre del jugador: ");
                        nombre = sc.nextLine(); //Parsing...
                        nombre = sc.nextLine();
                        
                        //Hacemos la query
                        try{
                            pstmt = conn.prepareStatement("UPDATE Jugadores SET Nombre = ? WHERE DNI_Jugador = '" + DNI + "'");
                            
                            if(nombre.equals("")){
                                System.out.println("No ha introducido ningún nombre!");
                            }
                            else{
                                pstmt.setString(1, nombre);
                                pstmt.executeQuery();
                                System.out.println("Nombre cambiado a " + nombre);
                                
                                tras_modificar = conn.setSavepoint();
                            }
                        }catch(SQLException ex){
                            System.out.println("No ha sido posible modificar el nombre del jugador!");
                            
                            if(tras_modificar != null){
                                try{
                                conn.rollback(tras_modificar);
                                }catch(SQLException ex1){
                                    ex1.printStackTrace();
                                }
                            }
                        }
                        
                        break;
                        
                    case 2:
                    String apellido_paterno;
                    System.out.print("Introduce el nuevo primer apellido del jugador: ");
                    apellido_paterno = sc.nextLine(); //Parsing...
                    apellido_paterno = sc.nextLine();

                    //Hacemos la query
                    try{
                        pstmt = conn.prepareStatement("UPDATE Jugadores SET Apellido_paterno = ? WHERE DNI_Jugador = '" + DNI + "'");

                        if(apellido_paterno.equals("")){
                            System.out.println("No ha introducido ningún apellido!!");
                        }
                        else{
                            pstmt.setString(1, apellido_paterno);
                            pstmt.executeQuery();
                            System.out.println("Primer apellido cambiado a " + apellido_paterno);
                            tras_modificar = conn.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar el primer apellido del jugador!");

                        if(tras_modificar != null){
                            try{
                            conn.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;

                    case 3:
                    String apellido_materno;
                    System.out.print("Introduce el nuevo segundo apellido del jugador: ");
                    apellido_materno = sc.nextLine(); //Parsing...
                    apellido_materno = sc.nextLine();

                    //Hacemos la query
                    try{
                        pstmt = conn.prepareStatement("UPDATE Jugadores SET Apellido_materno = ? WHERE DNI_Jugador = '" + DNI + "'");

                        if(apellido_materno.equals("")){
                            System.out.println("No ha introducido ningún apellido!!");
                        }
                        else{
                            pstmt.setString(1, apellido_materno);
                            pstmt.executeQuery();
                            System.out.println("Segundo apellido cambiado a " + apellido_materno);
                            tras_modificar = conn.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar el segundo apellido del jugador!");

                        if(tras_modificar != null){
                            try{
                            conn.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;

                    case 4:
                    String sEstatura;
                    System.out.print("Introduce la nueva estatura del jugador: ");
                    sEstatura = sc.nextLine(); //Parsing...
                    sEstatura = sc.nextLine();

                    //Hacemos la query
                    try{
                        pstmt = conn.prepareStatement("UPDATE Jugadores SET Estatura = ? WHERE DNI_Jugador = '" + DNI + "'");

                        if(sEstatura.equals("")){
                            System.out.println("No ha introducido ninguna estatura!!");
                        }
                        else{
                            pstmt.setInt(1, Integer.parseInt(sEstatura));
                            pstmt.executeQuery();
                            System.out.println("Estatura cambiada a " + sEstatura);
                            tras_modificar = conn.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar la estatura del jugador!");

                        if(tras_modificar != null){
                            try{
                            conn.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;

                    case 5:
                    String nacimiento;
                    System.out.print("Introduce la fecha de nacimiento del jugador(dd mm yyyy): ");
                    nacimiento = sc.nextLine(); //Parsing...
                    nacimiento = sc.nextLine();

                    java.util.Date cumple;
                    Date sqlCumple = null;

                    //Hacemos la query
                    try{

                        pstmt = conn.prepareStatement("UPDATE Jugadores SET Fecha_nacimiento = ? WHERE DNI_Jugador = '" + DNI + "'");

                        if(nacimiento.equals("")){
                            System.out.println("No ha introducido ninguna fecha de nacimiento!!");
                        }
                        else{
                            try{
                                cumple = new SimpleDateFormat("dd MM yyyy").parse(nacimiento);
                                sqlCumple = new Date(cumple.getTime());
                            }catch(ParseException ex1){
                                System.out.println("Formato de la fecha incorecto! Saliendo...\n");
                                break;
                            }
                            pstmt.setDate(1, sqlCumple);
                            pstmt.executeQuery();
                            System.out.println("Fecha de cumpleaños cambiada a " + nacimiento);
                            tras_modificar = conn.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar la fecha de nacimiento del jugador!");

                        if(tras_modificar != null){
                            try{
                            conn.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;

                    case 6:
                    String nacionalidad;
                    System.out.print("Introduce la nueva nacionalidad del jugador: ");
                    nacionalidad = sc.nextLine(); //Parsing...
                    nacionalidad = sc.nextLine();

                    //Hacemos la query
                    try{
                        pstmt = conn.prepareStatement("UPDATE Jugadores SET Nacionalidad = ? WHERE DNI_Jugador = '" + DNI + "'");

                        if(nacionalidad.equals("")){
                            System.out.println("No ha introducido ninguna nacionalidad!!");
                        }
                        else{
                            pstmt.setString(1, nacionalidad);
                            pstmt.executeQuery();
                            System.out.println("Nacionalidad cambiada a " + nacionalidad);
                            tras_modificar = conn.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar la nacionalidad del jugador!");

                        if(tras_modificar != null){
                            try{
                            conn.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;
    
                        
                    case 7:
                        System.out.println("Saliendo...\n");
                        conn.commit();
                        keep = false;
                        break;
                        
                    case 8:
                        System.out.println("Saliendo sin guardar...\n");
                        conn.rollback(st_inicial);
                        keep = false;
                        break;
                        
                    default:
                        System.out.println("Esa opción no existe!");
                }
            }
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    private static void mostrarJugadoresClub(String club){
        //Presentacion del prompt
        try{
            System.out.println("Listado de jugadores de la LaLiga del club " + club);
            System.out.println("---------------------------------\n");
        
            System.out.println("DNI\t\t|\tNombre\t|\tPrimer Apellido\t|\tSegundo Apellido\t|\tEstatura\t|\tFecha de nacimiento\t|\tNacionalidad");
            System.out.println("---\t\t \t------\t \t---------------\t \t----------------\t \t--------\t \t-------------------\t \t");

            //Comenzamos obteniendo los jugadores que pertenecen al club
            String query_DNI = "SELECT * FROM Pertenece_jugadores WHERE Nombre_club = ?";
            PreparedStatement psmt_dni = conn.prepareStatement(query_DNI);
            psmt_dni.setString(1, club);
            
            ResultSet dnis = psmt_dni.executeQuery();
            
            //Obtenemos los datos de los jugadores y los mostramos
            PreparedStatement psmt = conn.prepareStatement("SELECT * FROM Jugadores WHERE DNI_Jugador = ?");
            
            while(dnis.next()){
                psmt.setString(1, dnis.getString("dni_jugador"));
                ResultSet jugador = psmt.executeQuery();
                
                while(jugador.next()){
                    String to_show = jugador.getString("DNI_Jugador") + "\t \t" + jugador.getString("nombre") + 
                        "\t \t" + jugador.getString("apellido_paterno") + "\t \t\t";
                
                    if(jugador.getString("apellido_materno") == null){
                        to_show += "\t \t\t\t";
                    }
                    else{
                        to_show += jugador.getString("apellido_materno") + "\t \t\t\t";
                    }

                    if(jugador.getInt("estatura") == 0){
                        to_show += "\t \t\t";
                    }
                    else{
                        to_show += jugador.getInt("estatura") + "\t \t\t";
                    }

                    to_show += jugador.getString("fecha_nacimiento").split(" ")[0] + "\t \t\t";

                    if(jugador.getString("nacionalidad") != null){
                        to_show += jugador.getString("nacionalidad");
                    }

                    System.out.println(to_show);
                }
            }
            
            System.out.println();
            
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * Función que muestra el menú del subsistema
     */
    
    public static void menuJugadores(){
        //Presentacion del menu
        System.out.println("GESTIÓN DE JUGADORES");
        System.out.println("--------------------");
        
        int opcion = -1; //Opcion que elige el usuario
        
        Scanner sc = new Scanner(System.in);
        
        //Mostramos las opciones al usuario
        while(opcion != 6){ //Mientras no queramos salir del subsistema
            System.out.println("1.- Dar de alta un nuevo jugador");
            System.out.println("2.- Dar de baja un jugador");
            System.out.println("3.- Modificar jugador");
            System.out.println("4.- Mostrar listado general de jugadores");
            System.out.println("5.- Mostrar listado de jugadores por club");
            System.out.println("6.- Salir");
            
            //Pedimos una opcion al usuario
            System.out.print("Opcion: ");
            opcion = sc.nextInt();
            
            //Logica del menu
            switch(opcion){
                case 1:
                    altaNuevoJugador();
                    break;
                case 2:
                    bajaJugador();
                    break;
                case 3:
                    modificarJugador();
                    break;
                case 4:
                    listadoJugadores();
                    break;
                case 5:
                    //Pedimos el club al usuario
                    System.out.print("Introduce el club que desea listar: ");
                    String club = sc.nextLine();
                    club = sc.nextLine();
                    
                    if(!Clubes.clubExists(club)){
                        System.out.println("El club introducido no existe!\n");
                    }else{
                        mostrarJugadoresClub(club);
                    }
                    break;
                case 6:
                    System.out.println("Saliendo del sistema de gestión de jugadores...\n");
                    break;
                default:
                    System.out.println("Opción no válida!\n----------------------- \n");
            }
        }
    }
}