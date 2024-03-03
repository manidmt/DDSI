package Subsistemas;

import java.sql.Connection;
import java.util.Scanner;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Savepoint;
import java.sql.DriverManager;
import java.text.ParseException;
import Subsistemas.Clubes;
import Subsistemas.Partidos;

/**
 * 
 * @author Manuel Díaz-Meco Terrés
 */


public class Clasificacion {
    
    private static Connection conn = null;


    
    
    public static void setConnection(Connection conn1){
        conn = conn1;
    }
    
    /**
     * Borrado de las tablas asociadas al subistema ---> Posición, Ocupa, Jornada y Ocurre
     */
    public static void borrarSistemaClasificacion(){

        //System.out.println("Eliminando las tablas existentes...");
        //System.out.println("---------------------------");
        borrarTabla("Ocupa");
        borrarTabla("Jornada");
        borrarTabla("Ocurre");
        borrarTabla("Posicion");
        
    }

    /**
     * Método específico que borra la tabla Ocupa, necesario para la interfaz
     */

    public static void borrarTablaOcupa() {borrarTabla("Ocupa");}

    /**
     * Creación de las tablas del subsistema ---> Posición, Ocupa, Jornada y Ocurre
     */
    public static void inicializarSistemaClasificacion(){

        try {       
            
            borrarSistemaClasificacion();

            System.out.println("");

            //Creamos las tablas
            System.out.println("Creando las tablas...");
            System.out.println("-----------------------");
            
            // Creamos la tabla Jornada:
            String createJornadaSQL = "CREATE TABLE Jornada(ID_Jornada NUMBER PRIMARY KEY NOT NULL)";
            PreparedStatement createJornadaStatement = conn.prepareStatement(createJornadaSQL);
            createJornadaStatement.executeUpdate();
            System.out.println("Tabla Jornada creada correctamente!");


            // Creamos la tabla Posición:
            String createPosicionSQL = "CREATE TABLE Posicion(ID_Posicion NUMBER PRIMARY KEY NOT NULL, ID_Jornada NUMBER NOT NULL, Nombre_Club VARCHAR(50) NOT NULL, PartidosGanados NUMBER(2), PartidosEmpatados NUMBER(2), PartidosPerdidos NUMBER(2), GolesMarcados NUMBER(3), GolesRecibidos NUMBER(3), IndiceMejora NUMBER(1), FOREIGN KEY (ID_Jornada) REFERENCES Jornada (ID_Jornada) ON DELETE CASCADE, FOREIGN KEY (Nombre_Club) REFERENCES Club (Nombre_Club) ON DELETE CASCADE)";
            PreparedStatement createPosicionStatement = conn.prepareStatement(createPosicionSQL);
            createPosicionStatement.executeUpdate();
            System.out.println("Tabla Posicion creada correctamente!");

            // Creamos la tabla Ocurre:
            String createOcurreSQL = "CREATE TABLE Ocurre(ID_Partido VARCHAR2(20) NOT NULL, ID_Jornada NUMBER NOT NULL, PRIMARY KEY (ID_Partido, ID_Jornada), FOREIGN KEY (ID_Partido) REFERENCES Partido (ID_Partido) ON DELETE CASCADE, FOREIGN KEY (ID_Jornada) REFERENCES Jornada (ID_Jornada) ON DELETE CASCADE)";
            PreparedStatement createOcurreStatement = conn.prepareStatement(createOcurreSQL);
            createOcurreStatement.executeUpdate();
            System.out.println("Tabla Ocurre creada correctamente!");

            // Creamos la tabla Ocupa:
            String createOcupaSQL = "CREATE TABLE Ocupa(ID_Posicion NUMBER NOT NULL, Nombre_Club VARCHAR(50) NOT NULL, ID_Jornada NUMBER NOT NULL, PRIMARY KEY (ID_Posicion, Nombre_Club, ID_Jornada), FOREIGN KEY (Nombre_Club) REFERENCES Club (Nombre_Club) ON DELETE CASCADE, FOREIGN KEY (ID_Jornada) REFERENCES Jornada (ID_Jornada) ON DELETE CASCADE, FOREIGN KEY (ID_Posicion) REFERENCES Posicion (ID_Posicion) ON DELETE CASCADE)";
            PreparedStatement createOcupaStatement = conn.prepareStatement(createOcupaSQL);
            createOcupaStatement.executeUpdate();
            System.out.println("Tabla Ocupa creada correctamente!");


            conn.commit();

           } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback(); //Si no ha ido bien, deshacemos los cambios
                System.out.println("ERROR: No ha sido posible Resetear la BD!\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Método que verifica si una jornada existe
     * @param idJornada
     * @return booleano que indica si la jornada existe
     */

    public static boolean JornadaExiste(String idJornada){

        try{
            int idJornadaInt = Integer.parseInt(idJornada);
            String get_Jornada_query = "SELECT ID_Jornada FROM Jornada WHERE ID_Jornada=?";
            PreparedStatement pstmt = conn.prepareStatement(get_Jornada_query);
           
            pstmt.setInt(1, idJornadaInt );
            ResultSet resultSet = pstmt.executeQuery(get_Jornada_query);

            if(resultSet.next()){
                return true;
            }

        } catch(SQLException e){
            e.printStackTrace();
        }catch (NumberFormatException e) {
            System.out.println("El ID de la jornada debe ser numérico.");
        }

        return false;
    }

    /**
     * Método que comprueba si una tabla existe
     * @param tabla
     * @return booleano que indica si la tabla existe
     */

    public static boolean existsTable(String tabla){

        try{
            Statement statement = conn.createStatement();
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

    /**
     * Método que borra una tabla que se pasa como parámetro
     * @param tabla a borrar
     */

    public static void borrarTabla(String tabla){

        try{         
            //Verificamos si la tabla existe            
            if(existsTable(tabla)){
                Statement delete_statement = conn.createStatement();
                String delete_table_query = "DROP TABLE " + tabla;
                delete_statement.executeQuery(delete_table_query);
                System.out.println(tabla + " ha sido eliminada!");
            }
            else{
                System.out.println(tabla + " no existe!");
            }
        }catch(SQLException e){
            e.printStackTrace();
        } 
    }

    /**
     * Método que obtiene la máxima jornada introducida (la de mayor valor numérico)
     * @return la máxima jornada
     */

    public static int obtenerMaximaJornada() {

        int maxJornada = -1; // No hay jornadas introducidas en la base de datos
    
        try {
            String sql = "SELECT MAX(ID_Jornada) AS MaxJornada FROM Jornada";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();


            if (rs.next()) {
                maxJornada = rs.getInt("MaxJornada");
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return maxJornada;
    }

    /**
     * Trigger que verifica si al asignar una jornada, en el proceso están involucradas
     * las tablas ocurre y jornada, es positiva
     */

    public static void triggerAsignarJornada() {

        try (Statement stmt = conn.createStatement()) {

            String triggerJornadaSQL = "CREATE TRIGGER VerificarJornadaPositivaEnJornada "
                                     + "BEFORE INSERT ON Jornada "
                                     + "FOR EACH ROW "
                                     + "BEGIN "
                                     + "    IF NEW.ID_Jornada <= 0 THEN "
                                     + "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El ID de la jornada debe ser positivo'; "
                                     + "    END IF; "
                                     + "END;";
    
            String triggerOcurreSQL = "CREATE TRIGGER VerificarJornadaPositivaEnOcurre "
                                    + "BEFORE INSERT ON Ocurre "
                                    + "FOR EACH ROW "
                                    + "BEGIN "
                                    + "    IF NEW.ID_Jornada <= 0 THEN "
                                    + "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El ID de la jornada debe ser positivo'; "
                                    + "    END IF; "
                                    + "END;";
    
            stmt.execute(triggerJornadaSQL);
            stmt.execute(triggerOcurreSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Método que asigna un partido a una jornada, rellenando una tupla de la tabla ocurre
     * y de la tabla jornada
     * @param IDJornada jornada a asignar
     * @param IDPartido partido a asignar
     */

    public static void asignarJornada(String IDJornada, String IDPartido){

        try{

            int idJornadaInt = Integer.parseInt(IDJornada);
            int idpar = Integer.parseInt(IDPartido);
            String query1 = "INSERT INTO Jornada (ID_Jornada) VALUES (?)";
            String query2 = "INSERT INTO Ocurre (ID_Partido, ID_Jornada) VALUES (?, ?)";

            PreparedStatement ptsmt1 = conn.prepareStatement(query1);
            PreparedStatement ptsmt2 = conn.prepareStatement(query2);

            ptsmt1.setInt(1, idJornadaInt);
            ptsmt2.setInt(1, idpar);
            ptsmt2.setInt(2, idJornadaInt);
            
            ptsmt1.executeUpdate(); 
            ptsmt2.executeUpdate();

            conn.commit();
           
        }catch(SQLException e){
            e.printStackTrace();
        }catch (NumberFormatException e) {
            System.out.println("El ID de la jornada debe ser numérico.");
        }
    } 

    /**
     * Método que elimina una jornada
     * @param iDJornada jornada a eliminar
     */

    public static void eliminarJornada(String iDJornada){

        try{

            int idJornadaInt = Integer.parseInt(iDJornada);

            String sql = "DELETE FROM Jornada WHERE ID_Jornada = (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idJornadaInt);

            int deleted = pstmt.executeUpdate();
            
            if(deleted > 0){
                System.out.println("Se ha borrado correctamente del sistema\n");
                conn.commit();
            }
            else{
                System.out.println("No se ha podido borrar del sistema\n");
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("El ID de la jornada debe ser numérico.");
        }
    }

    /**
     * Trigger que verifica que el id de jornada a modificar es mayor que 0
     * y no se ha introducido previamente
     */

    public static void triggerModificarJornada() {
        try {
            Statement stmt = conn.createStatement();
    
            String triggerSQL = "CREATE TRIGGER VerificarJornadaAntesDeModificar "
                              + "BEFORE UPDATE ON Ocurre "
                              + "FOR EACH ROW "
                              + "BEGIN "
                              + "IF NEW.ID_Jornada <= 0 THEN "
                              + "    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El ID de la jornada debe ser positivo'; "
                              + "END IF; "
                              + "IF NOT EXISTS (SELECT 1 FROM Jornada WHERE ID_Jornada = NEW.ID_Jornada) THEN "
                              + "    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La jornada no existe en la base de datos'; "
                              + "END IF; "
                              + "END;";
    
            stmt.execute(triggerSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Método que modifica el partido asociado a una jornada
     * @param IDJornada jornada que va a modificar su partido asociado
     */

    public static void modificarJornada(String IDJornada){

        try{
        //Input
        Scanner scanner = new Scanner(System.in);

        System.out.println("Introduzca el nuevo partido para asignar la jornada:");
        String idpartido = scanner.nextLine();
        idpartido = scanner.nextLine(); //Parseamos

        if (!idpartido.equals("")) {

            String query = "UPDATE Ocurre SET ID_Partido = ? WHERE ID_Jornada = ?";
            PreparedStatement stpartido = conn.prepareStatement(query); 
            stpartido.setInt(1, Integer.parseInt(idpartido));
            stpartido.setInt(2, Integer.parseInt(IDJornada));
            stpartido.executeUpdate();

            conn.commit();
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    /*public static void obtenerGoles_Jornada_Club(int idJornada, String nombreClub) {
        try {
            // Consulta para obtener goles marcados y recibidos en partidos locales
            String sqlLocal = "SELECT SUM(golesLocal) AS GolesMarcados, SUM(golesVisitante) AS GolesRecibidos FROM JuegaLocal JL JOIN Ocurre O ON JL.ID_Partido = O.ID_Partido WHERE O.ID_Jornada = ? AND JL.Nombre_Club = ?";
    
            // Consulta para obtener goles marcados y recibidos en partidos visitantes
            String sqlVisitante = "SELECT SUM(golesVisitante) AS GolesMarcados, SUM(golesLocal) AS GolesRecibidos FROM JuegaVisitante JV JOIN Ocurre O ON JV.ID_Partido = O.ID_Partido WHERE O.ID_Jornada = ? AND JV.Nombre_Club = ?";
    
            PreparedStatement pstmtLocal = conn.prepareStatement(sqlLocal);
            PreparedStatement pstmtVisitante = conn.prepareStatement(sqlVisitante);
    
            pstmtLocal.setInt(1, idJornada);
            pstmtLocal.setString(2, nombreClub);
    
            pstmtVisitante.setInt(1, idJornada);
            pstmtVisitante.setString(2, nombreClub);
    
            ResultSet rsLocal = pstmtLocal.executeQuery();
            ResultSet rsVisitante = pstmtVisitante.executeQuery();
    
            int golesMarcados = 0;
            int golesRecibidos = 0;
    
            if (rsLocal.next()) {
                golesMarcados += rsLocal.getInt("GolesMarcados");
                golesRecibidos += rsLocal.getInt("GolesRecibidos");
            }
    
            if (rsVisitante.next()) {
                golesMarcados += rsVisitante.getInt("GolesMarcados");
                golesRecibidos += rsVisitante.getInt("GolesRecibidos");
            }
    
            System.out.println(nombreClub + ": Goles Marcados: " + golesMarcados + ", Goles Recibidos: " + golesRecibidos);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
    
    /**
     * Método principal que actualiza la clasficación mediante los datos recoplidos del 
     * subsistema partido
     */

    public static void actualizarClasificacion(){

        int maxjornada = obtenerMaximaJornada();
        if (maxjornada >= 0){/*
            La idea sería recorrer todas las jornadas desde la primera hasta la última y en cada
            una pasar por todos los partidos jugados comprobando la cantidad de goles marcados y
            recibidos de cada club, así como si ha ganado el partido (comprobando los goles), lo 
            ha empatado o lo ha perdido. Estos datos se irían sumando por club conforme pasen las
            jornadas, en cada iteración del bucle habría que calcular la posición de cada club para
            poder rellenar la tabla posición y ocupa (ID_Posicion, Nombre_Club, ID_Jornada) que 
            identificaría univocamente la posición de cada club en cada jornada así como la cantidad
            de goles y partidos ganados, etc. Al completar el bucle habríamos rellenado todas las 
            tuplas de la tabla Posición. En mostrar clasificación lo que hacemos es mostrar las tuplas
            de la tabla posción de la jornada con el valor más grande (la última disputada). El índice
            de mejora se determinaría calculando si la posición de un club respecto a la jornada anterior
            ha mejorado, empeorado o se ha mantenido igual.
            Al final del método habría que hacer un commit para actualizar en la base de datos todas las
            tuplas generadas de las tablas ocupa y posición.
        */}
        
        else System.out.println("No se han introducido Jornadas...\n");
    }

    /**
     * Método que muestra la clasificación por pantalla (la última jornada)
     */

    public static void mostrarClasificacion(){

        try{

            System.out.println("CLASIFICACION:%n%n");
            
            // Ordenamos las posiciones desde la sentencia SQL, mostrando la última jornada disputada:
            String query = "SELECT * FROM Posicion WHERE ID_Jornada = (SELECT MAX(ID_Jornada) FROM Posicion) ORDER BY ID_Posicion";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            System.out.printf("%-10s %-20s %-5s %-5s %-5s %-5s %-5s %-10s %-10s %n",
                     "Posicion", "Club", "PG", "PP", "PE", "GA", "GR", "Jornada", "Mejora");

            while (resultSet.next()) {

                int idPosicion = resultSet.getInt("ID_Posicion");
                String idJornada = resultSet.getString("ID_Jornada");
                String nombreClub = resultSet.getString("Nombre_Club");
                int partidosGanados = resultSet.getInt("PartidosGanados");
                int partidosEmpatados = resultSet.getInt("PartidosEmpatados");
                int partidosPerdidos = resultSet.getInt("PartidosPerdidos");
                int golesMarcados = resultSet.getInt("GolesMarcados");
                int golesRecibidos = resultSet.getInt("GolesRecibidos");
                int IndiceMejora = resultSet.getInt("IndiceMejora");

                System.out.printf("%-5d %-20s %-5d %-5d %-5d %-5d %-5d %-10s %-10s", 
                idPosicion, nombreClub, partidosGanados, partidosPerdidos, partidosEmpatados, golesMarcados, golesRecibidos,
                idJornada, IndiceMejora);
                
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Menú principal del subsistema clasificación
     */

    public static void menuClasificacion(){

        // Ejecutamos los triggers necesarios para el correcto funcionamiento
        // del subsistema clasificación

        //triggerAsignarJornada();
        //triggerModificarJornada();

        //Presentacion del menu
        System.out.println("MENU DE CLASIFICACION");
        System.out.println("-----------------------");
        
        int opcion = -1; //Opcion que ha elegido el usuario
        
        Scanner scanner = new Scanner(System.in); //Entrada del usuario
        
        //Mostramos las opciones al usuario
        while (opcion != 5){ //Mientras no queramos salir del programa
            System.out.println("1.- Asignar un Partido a una Jornada");
            System.out.println("2.- Eliminar una Jornada");
            System.out.println("3.- Modificar una Jornada");
            System.out.println("4.- Mostrar la Clasificación");
            System.out.println("5.- Salir");
            
            //Pedimos una opcion al usuario
            System.out.print("Opcion: ");
            opcion = scanner.nextInt();
            
            switch(opcion){

               /* case 0:
                    crearTablas();
                    break;*/

                case 1:
                    System.out.print("\nIntroduce los datos del Jornada:\n\t-ID de la Jornada: ");
                    Scanner s = new Scanner(System.in); 
                    String idJornada = s.nextLine();
                    
//                    if(JornadaExiste(idJornada)){
//                        System.out.println("ED ID ya corresponde a una Jornada registrada, saliendo...");
//                        return;
//                    }
                    
                    System.out.print("\t-ID del Partido: ");
                    String idPartido = s.nextLine(); 
                    
                    // Comprobar si el partido existe -- Método en subsistema Partido?

                    asignarJornada(idJornada, idPartido);
                    
                    break;

                case 2:

                    System.out.print("\nIntroduce el ID de la Jornada que quieras eliminar: ");
                    Scanner sc = new Scanner(System.in); 
                    idJornada = sc.nextLine();
                    eliminarJornada(idJornada);
                    break;
                case 3:
                    System.out.print("\nIntroduce la Jornada que deseas modificar:");
                    Scanner sc1 = new Scanner(System.in); 
                    idJornada = sc1.nextLine();
                    modificarJornada(idJornada);
                    break;
                    
                case 4:
                    actualizarClasificacion();
                    mostrarClasificacion();
                    
                    break;

                case 5:
                    System.out.println("Saliendo del sistema de gestión de la clasificación");
                    break;
                    
                default:
                    System.out.println("Opción no válida! \n----------------------- \n");
                    break;
            }
        }
    }
}
