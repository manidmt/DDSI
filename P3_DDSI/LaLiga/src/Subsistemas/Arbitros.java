/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Subsistemas;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Savepoint;

/**
 *
 * @author ventura
 */
public class Arbitros {
    
    /**
     * Conexion a nuestra BD
     */
    private static Connection conn1;
    
    
    /**
     * Metodo para setear nuestra conexion
     * @param conn Connection conexion a nuestra BD
     */
    public static void setConnection(Connection conn){
        conn1 = conn;
    }
    
        
    public static boolean checkDNI(String dni){
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
    
        
    private static boolean existsTable(String tabla){
        try{
            Statement statement = conn1.createStatement();
            String get_tables_query = "SELECT table_name FROM user_tables";
            ResultSet resultSet = statement.executeQuery(get_tables_query);
            
            while(resultSet.next()){
                tabla = tabla.toUpperCase();
                if(resultSet.getString("TABLE_NAME").equals(tabla)){
                    return true;
                }
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
        
        return false;
    }
    
        
    private static boolean Arbitroexiste (String DNI){
        try{
            String get_Arbitro_query = "SELECT DNI_Arbitro FROM Arbitro WHERE DNI_Arbitro=?";
            PreparedStatement pstmt = conn1.prepareStatement(get_Arbitro_query);
            pstmt.setString(1,DNI);
            
            ResultSet resultSet = pstmt.executeQuery();
            
            if(resultSet.next()){
                return true;
            }

        } catch(SQLException e){
            e.printStackTrace();
        }
        
        return false;
    }
    
    
        
    
    public static void borrarSistemaArbitros(){
        try{
            Statement smt = conn1.createStatement();
            smt.execute("DELETE FROM Arbitro");
            smt.execute("DROP TABLE Arbitro");
            conn1.commit();
            
        }catch(SQLException ex){
            System.out.println("No ha sido posible eliminar el subsistema Jugadores!\n");
        }
        
    }
    
        
    private static void altaNuevoArbitro(){
        //Input
        Scanner scanner = new Scanner(System.in);   
        
        System.out.print("\nIntroduce los datos del arbitro:\n\t-DNI del Arbitro: ");
        String DNI = scanner.nextLine();
        
                  
//        if(!checkDNI(DNI)){
//            System.out.println("El DNI no ha sido introducido correctamente, saliendo...");
//            return;
//        }
                    
        if(Arbitroexiste(DNI)){
            System.out.println("El DNI ya corresponde a un arbitro registrado, saliendo...");
            return;
        }
                    
        System.out.print("\t-Nombre (max. 20 caracteres): ");
        String nombre = scanner.nextLine(); 
                    
        if(nombre.equals(" ")){
            System.out.println("El nombre no puede ser vacio, saliendo...");
            return;
        }
                    
        System.out.print("\t-Apellido Paterno (max. 20 caracteres): ");
        String Apellido_paterno = scanner.nextLine();
                    
        if(Apellido_paterno.equals(" ")){
            System.out.println("El Apellido_Paterno no puede ser vacio, saliendo...");
            return;
        }
                    
        //Campo Opcional
        System.out.print("\t-Apellido Materno (max. 20 caracteres): ");
        String Apellido_materno = scanner.nextLine();
                    
        if(Apellido_materno.equals(" ")){
            Apellido_materno=null;
        }
                    
        System.out.print("\t-Fecha nacimiento (dd mm yyyy): ");
        String fecha = scanner.nextLine();
                    
        //Parseamos la fecha
        Date fechaUtil;
        java.sql.Date sqlDate=null;
        try {
            fechaUtil = new SimpleDateFormat("dd mm yyyy").parse(fecha);
            sqlDate = new java.sql.Date(fechaUtil.getTime());
            
        } catch (ParseException ex) {
            System.out.println("Formato de la fecha incorrecto!");
        }
        
        try{
           
            String query = "INSERT INTO Arbitro (DNI_Arbitro, Nombre, Apellido_paterno, Apellido_Materno, Fecha_nacimiento) VALUES (?, ? , ?, ?, ?)";
            PreparedStatement ptsmt = conn1.prepareStatement(query);
            
            ptsmt.setString(1, DNI);
            ptsmt.setString(2, nombre);
            ptsmt.setString(3, Apellido_paterno);
            ptsmt.setString(4, Apellido_materno);
            ptsmt.setDate(5, sqlDate);
            
            ptsmt.executeUpdate(); //El arbitro ya esta registrado en la BD
            
            conn1.commit();
         
        }catch(SQLException e){
            e.printStackTrace();
        }
    }  
        
        
    private static void ModificarDatosArbitro(){
        try{
            //Input
            Scanner scanner = new Scanner(System.in);
            
            System.out.print("\nIntroduce los datos del arbitro a modificar:\n\t-DNI del Arbitro: ");
            String DNI = scanner.nextLine();
                    
//            if(!checkDNI(DNI)){
//                System.out.println("El DNI no ha sido introducido correctamente, saliendo...");
//                return;
//            }
                    
            if(!Arbitroexiste(DNI)){
                 System.out.println("El DNI introducido no corresponde a ningun arbitro guardado, saliendo...");
                 return;
            }
            //INICIO DE TRANSACCIÓN DE MODIFICACIÓN
            Savepoint st_inicial = conn1.setSavepoint();
            
            //Savepoint despues de modificacion
            Savepoint tras_modificar = null;
            
            //Menú
            boolean keep = true;
            
            while(keep){
                int option;
                
                //Interfaz para el usuario
                System.out.println("\nMODIFICAR DATOS DE ARBITRO " + DNI);
                System.out.println("----------------------------");
                
                System.out.println("1.- Modificar Nombre");
                System.out.println("2.- Modificar Apellido Paterno");
                System.out.println("3.- Modificar Apellido Materno");
                System.out.println("4.- Modificar fecha de nacimiento");
                System.out.println("5.- Guardar y salir");
                System.out.println("6.- Salir sin guardar");
                System.out.print("Opcion: ");
                
                option = scanner.nextInt();
                
                //Statement
                PreparedStatement pstmt = null;
                
                switch(option){
                    case 1:
                        String nombre;
                        System.out.print("Introduce el nuevo nombre del arbitro: ");
                        nombre = scanner.nextLine(); //Parsing...
                        nombre = scanner.nextLine();
                        
                        //Hacemos la query
                        try{
                            pstmt = conn1.prepareStatement("UPDATE Arbitro SET Nombre = ? WHERE DNI_Arbitro = '" + DNI + "'");
                            
                            if(nombre.equals("")){
                                System.out.println("No ha introducido ningún nombre!");
                            }
                            else{
                                pstmt.setString(1, nombre);
                                pstmt.executeQuery();
                                System.out.println("Nombre cambiado a " + nombre);
                                
                                tras_modificar = conn1.setSavepoint();
                            }
                        }catch(SQLException ex){
                            System.out.println("No ha sido posible modificar el nombre del arbitro!");
                            
                            if(tras_modificar != null){
                                try{
                                conn1.rollback(tras_modificar);
                                }catch(SQLException ex1){
                                    ex1.printStackTrace();
                                }
                            }
                        }
                        
                        break;
                        
                    case 2:
                    String apellido_paterno;
                    System.out.print("Introduce el nuevo apellido paterno del arbitro: ");
                    apellido_paterno = scanner.nextLine(); //Parsing...
                    apellido_paterno = scanner.nextLine();

                    //Hacemos la query
                    try{
                        pstmt = conn1.prepareStatement("UPDATE Arbitro SET Apellido_Paterno = ? WHERE DNI_Arbitro = '" + DNI + "'");

                        if(apellido_paterno.equals("")){
                            System.out.println("No ha introducido ningún apellido!!");
                        }
                        else{
                            pstmt.setString(1, apellido_paterno);
                            pstmt.executeQuery();
                            System.out.println("Apellido Paterno cambiado a " + apellido_paterno);
                            tras_modificar = conn1.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar el apellido parterno del arbitro!");

                        if(tras_modificar != null){
                            try{
                            conn1.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;

                    case 3:
                    String apellido_materno;
                    System.out.print("Introduce el nuevo apellido materno del arbitro: ");
                    apellido_materno = scanner.nextLine(); //Parsing...
                    apellido_materno = scanner.nextLine();

                    //Hacemos la query
                    try{
                        pstmt = conn1.prepareStatement("UPDATE Arbitro SET Apellido_Materno = ? WHERE DNI_Arbitro = '" + DNI + "'");

                        if(apellido_materno.equals("")){
                            System.out.println("No ha introducido ningún apellido!!");
                        }
                        else{
                            pstmt.setString(1, apellido_materno);
                            pstmt.executeQuery();
                            System.out.println("Apellido Materno cambiado a " + apellido_materno);
                            tras_modificar = conn1.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar el apellido materno del arbitro!");

                        if(tras_modificar != null){
                            try{
                            conn1.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;

                    case 4:
                    String nacimiento;
                    System.out.print("Introduce la nueva fecha de nacimiento del arbitro(dd mm yyyy): ");
                    nacimiento = scanner.nextLine(); //Parsing...
                    nacimiento = scanner.nextLine();

                    Date cumple;
                    
                    java.sql.Date sqlCumple =null;
                    

                    //Hacemos la query
                    try{

                        pstmt = conn1.prepareStatement("UPDATE Arbitro SET Fecha_nacimiento = ? WHERE DNI_Arbitro = '" + DNI + "'");

                        if(nacimiento.equals("")){
                            System.out.println("No ha introducido ninguna fecha de nacimiento!!");
                        }
                        else{
                            try{
                                cumple = new SimpleDateFormat("dd MM yyyy").parse(nacimiento);
                                sqlCumple = new java.sql.Date(cumple.getTime());
                            }catch(ParseException ex1){
                                System.out.println("Formato de la fecha incorecto! Saliendo...\n");
                                break;
                            }
                            pstmt.setDate(1, sqlCumple);
                            pstmt.executeQuery();
                            System.out.println("Fecha de nacimiento cambiada a " + nacimiento);
                            tras_modificar = conn1.setSavepoint();
                        }
                    }catch(SQLException ex){
                        System.out.println("No ha sido posible modificar la fecha de nacimiento del arbitro!");

                        if(tras_modificar != null){
                            try{
                            conn1.rollback(tras_modificar);
                            }catch(SQLException ex1){
                                ex1.printStackTrace();
                            }
                        }
                    }

                    break;
    
                        
                    case 5:
                        System.out.println("Saliendo...\n");
                        conn1.commit();
                        keep = false;
                        break;
                        
                    case 6:
                        System.out.println("Saliendo sin guardar...\n");
                        conn1.rollback(st_inicial);
                        keep = false;
                        break;
                        
                    default:
                        System.out.println("Esa opción no existe!");
                }
            }
            
           
        }catch(SQLException e){
            e.printStackTrace();
        }
        
    }
    
    public static void crearTablaArbitro(){
        try {              
            //CREACION DE TABLAS
            System.out.println("Creando las tablas...");
            System.out.println("-----------------------");
            
            //Creamos la tabla Arbitro

            Statement stm = conn1.createStatement();
            stm.execute("CREATE TABLE Arbitro(DNI_Arbitro VARCHAR2(9) PRIMARY KEY, Nombre VARCHAR2(20) NOT NULL," +
                "Apellido_Paterno VARCHAR(20) NOT NULL," +
                "Apellido_Materno VARCHAR(20)," +
                "Fecha_nacimiento DATE NOT NULL)");
            
            //Creamos el disparador para comprobar que el arbitro sea mayor de edad
            stm.execute("CREATE OR REPLACE TRIGGER validar_edad_arbitro "
                    + "BEFORE INSERT ON Arbitro "
                    + "FOR EACH ROW "
                    + "DECLARE fecha_nacimiento_valida BOOLEAN; "
                    + "BEGIN "
                    + "fecha_nacimiento_valida := (SYSDATE - :NEW.fecha_nacimiento) >= 6570; "
                    + "IF NOT fecha_nacimiento_valida THEN "
                    + "RAISE_APPLICATION_ERROR(-20001, 'El jugador debe de ser mayor de edad!'); "
                    + "END IF; "
                    + "END");

            System.out.println("Tabla Arbitro creada correctamente!");
            
            //Escribimos los cambios a la BD
            conn1.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn1.rollback(); //Si no ha ido bien, deshacemos los cambios
                System.out.println("ERROR: No ha sido posible Resetear la BD!\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Muestra la BD
     */
    public static void mostrarListadoArbitro(){
        try{
            
            System.out.println("\nMostrando tabla de Arbitro...\n");
            

            //Mostramos la tabla Arbitro
            System.out.println("Tabla Arbitro");
            System.out.println("-----------------------");
            System.out.println("DNI_Arbitro\t\tNombre\t\tApellido_Paterno\t\tApellido_Materno\t\tFecha_nacimiento");
            System.out.println("-----------\t\t------\t\t----------------\t\t----------------\t\t----------------");
            
            Statement stmt = conn1.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Arbitro");
                
            while(resultSet.next()){

                String cadena = resultSet.getString("DNI_Arbitro") + "\t\t" + resultSet.getString("Nombre")+
                                           "\t\t" + resultSet.getString("Apellido_Paterno") + "\t\t";
                        
                if(resultSet.getString("Apellido_Materno") == null){       
                    cadena += "\t\t\t";                       
                }
                
                else cadena += "\t\t" + resultSet.getString("Apellido_Materno");
                
                cadena += "\t\t\t" + resultSet.getString("Fecha_nacimiento");
                
                System.out.println(cadena);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Método para dar de baja un nuevo jugador
     */
    private static void bajaArbitro(){
        try{
            Scanner scanner = new Scanner(System.in); //Entrada del usuario
            //Preparacion de la query
            String queryBorrado = "DELETE FROM Arbitro WHERE DNI_Arbitro = ?";
            PreparedStatement pstmt = conn1.prepareStatement(queryBorrado);
            
            System.out.print("\nIntroduce los datos del arbitro:\n\t-DNI del Arbitro: ");
            String DNI= scanner.nextLine();
            
                    
            if(!Arbitroexiste(DNI)){
                System.out.println("El DNI introducido no corresponde a ningun arbitro guardado, saliendo...");
                return;
            }

            
            //Obtenemos el nombre y el apellido del jugador a eliminar
            String queryConsulta = "SELECT Nombre, Apellido_Paterno FROM Arbitro WHERE DNI_Arbitro = ?";
            PreparedStatement consulta = conn1.prepareStatement(queryConsulta);
            consulta.setString(1, DNI);
            ResultSet rs = consulta.executeQuery();
            
            //Borramos el jugador
            pstmt.setString(1, DNI);
            pstmt.executeQuery();
            
            conn1.commit();
            
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
    
    /**
     * Método para imprimir la lista de partidos asignados a un arbitro
     */
    
    private static void PartidosAsignadosArbitro(){
        Scanner scanner = new Scanner(System.in); //Entrada del usuario        
        
        System.out.print("\nIntroduce los datos del arbitro:\n\t-DNI del Arbitro: ");
        String DNI= scanner.nextLine();
                    
                    
        if(!Arbitroexiste(DNI)){
            System.out.println("El DNI introducido no corresponde a ningun arbitro guardado, saliendo...");
            return;
        }
        
        
        System.out.println("PARTIDOS ASIGANDOS AL ARBITRO CON DNI: " + DNI);
        System.out.println("------------------------------------------------");
        
        try{
            
            PreparedStatement pstmt = conn1.prepareStatement("SELECT ID_Partido FROM Arbitra WHERE DNI_Arbitro=?");
            pstmt.setString(1,DNI);
            
            ResultSet resultSet = pstmt.executeQuery();

            while(resultSet.next()){
                System.out.println(resultSet.getString("ID_Partido") + "\n");
            }
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        
        
        
    }
    
    public static void menuPrincipalArbitro(){
        //Presentacion del menu
        System.out.println("MENU DE ARBITRO");
        System.out.println("-----------------------");
        
        int opcion = -1; //Opcion que ha elegido el usuario
        
        Scanner scanner = new Scanner(System.in); //Entrada del usuario
        
        //Mostramos las opciones al usuario
        while (opcion != 6){ //Mientras no queramos salir del programa
            
            System.out.println("1.- Dar de alta nuevo Arbitro");
            System.out.println("2.- Dar de baja un arbitro");
            System.out.println("3.- Mostrar contenido BD Arbitro");
            System.out.println("4.- Modificar datos de un Arbitro");
            System.out.println("5.- Mostrar listado de futuros partidos asignados");
            System.out.println("6.- Salir");
            
            //Pedimos una opcion al usuario
            System.out.print("Opcion: ");
            opcion = scanner.nextInt();
            
            //Pasamos a la logica
            switch(opcion){
                
                case 0:
                    crearTablaArbitro();
                    break;
                
                case 1:
                    altaNuevoArbitro();
                    break;
                case 2:
                    bajaArbitro();
                    break;
                
                case 3:
                    mostrarListadoArbitro();
                    break;
                    
                case 4:
                    ModificarDatosArbitro();
                    break;
                    
        
                case 5:
                    PartidosAsignadosArbitro();
                    break;
                   
                    
                case 6:
                    System.out.println("Saliendo del sistema de gestión de arbitro");
                    break;
                
                default:
                    System.out.println("Opción no válida! \n----------------------- \n");
                    break;
            }
        }
    }
    
}
