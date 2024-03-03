/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
/**
 *
 * @author carlosmatac
 */
public class Partidos {
    
    private static Connection conn = null;
    
    /**
     * Metodo para establcer nuestra conexion 
     * @param conn1 conexion a la bases de datos que se la pasas cuando se crea el objeto
     */
    public static void setConnection (Connection conn1){
        conn = conn1;
    }
    
    /**
     * Este metodo servirá para eliminar las tablas que tienen dependencias de otras
     */
    public static void borrarSistemaPartidosDependientes(){
        try{
            Statement smt = conn.createStatement();
            smt.execute("DELETE FROM Juega_Local");
            smt.execute("DELETE FROM Juega_Visitante");
            smt.execute("DELETE FROM Arbitra");
            
            smt.execute("DROP TABLE Juega_Local");
            smt.execute("DROP TABLE Juega_Visitante");
            smt.execute("DROP TABLE Arbitra");
            
            conn.commit();
        }catch(SQLException ex){
            ex.printStackTrace();
        }
        
    }   
    
    /**
     * Este metodo elimina las tablas que no tienen dependencias pero de la que dependen otras.
     * Es importante hacer el borrado de las tablas dependientes antes de ejecutar este método ya que si no producirá errores
     */
    public static void borrarSistemaPartidosIndependientes(){
        try{
            Statement smt = conn.createStatement();
            
            smt.execute("DELETE FROM Partido");
            smt.execute("DROP TABLE Partido");
            conn.commit();
        }catch(SQLException ex){
            System.out.println("No ha sido posible eliminar las tablas independientes del subsistema de Partidos!\n");
        }
        
    }   
    
    private static boolean partidoExists(String id_partido){
        try{
            String query = "SELECT ID_Partido FROM PARTIDO WHERE ID_Partido = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, Integer.parseInt(id_partido));
            
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
     * Inicializar las tablas que son dependientes de otras 
     * Este metodo se debe ejecutar despues de el de las tablas que son independientes
     */
    public static void inicializarSistemaPartidosDependientes(){
        
        
        try{
            
            
            System.out.println("");
        
            //CREACION DE TABLAS
            System.out.println("Creando las tablas...");
            System.out.println("-----------------------");
        
            //Creamos la tabla JuegaLocal
            String createJuegaLocalSQL = "CREATE TABLE Juega_Local(ID_Partido VARCHAR(20) NOT NULL, Nombre_Club VARCHAR(50) NOT NULL, golesLocal NUMBER(3) , tarjetasLocal NUMBER(3), asistenciasLocal NUMBER(3), posesionLocal NUMBER(3), rematesLocal NUMBER(3), rematesPorteriaLocal NUMBER(3) , saquesEsquinaLocal NUMBER(3), fuerasJuegoLocal NUMBER(3), pasesLocal NUMBER(4))"
                    + " ,PRIMARY KEY (ID_Partido , Nombre_Club ),FOREIGN KEY (ID_Partido ) REFERENCES PARTIDO (ID_Partido) ON DELETE CASCADE, FOREIGN KEY (Nombre_Club ) REFERENCES CLUB  (Nombre_Club ) ON DELETE CASCADE)";
            PreparedStatement createJuegaLocalStatement = conn.prepareStatement(createJuegaLocalSQL);
            createJuegaLocalStatement.executeUpdate();
            System.out.println("Tabla Juega_Local creada correctamente!");
            
             //Creamos la tabla JuegaVisitante
            String createJuegaVisitanteSQL = "CREATE TABLE Juega_Visitante(ID_Partido VARCHAR(20) NOT NULL, Nombre_Club VARCHAR(50) NOT NULL, golesVisitante NUMBER(3) , tarjetasVisitante NUMBER(3),  asistenciasVisitante NUMBER(3), posesionVisitante NUMBER(3), rematesVisitante NUMBER(3), rematesPorteriaVisitante NUMBER(3) , saquesEsquinaVisitante NUMBER(3), fuerasJuegoVisitante NUMBER(3), pasesVisitante NUMBER(4))"
                    + " PRIMARY KEY (ID_Partido , Nombre_Club ),FOREIGN KEY (ID_Partido ) REFERENCES PARTIDO (ID_Partido) ON DELETE CASCADE, FOREIGN KEY (Nombre_Club ) REFERENCES CLUB  (Nombre_Club ) ON DELETE CASCADE)";
            PreparedStatement createJuegaVisitanteStatement = conn.prepareStatement(createJuegaVisitanteSQL);
            createJuegaVisitanteStatement.executeUpdate();
            System.out.println("Tabla Juega_Visitante creada correctamente!");
            
            //Creamos la tabla Arbitra -- La tabla que relaciona un partido con un arbitro
            //Esta tabla se borra en cascada si el subsistema de arbitros borra el arbitro del sistema
            String createArbitraSQL = "CREATE TABLE Arbitra(ID_Partido VARCHAR2(20) NOT NULL, DNI_Arbitro VARCHAR(9), NOT NULL, PRIMARY KEY (ID_Partido, DNI_Arbitro), FOREIGN KEY (ID_Partido ) REFERENCES PARTIDO (ID_Partido) ON DELETE CASCADE, FOREIGN KEY (DNI_Arbitro) REFERENCES ARBITRO (DNI_Arbitro) ON DELETE CASCADE)";
            PreparedStatement arbitra = conn.prepareStatement(createArbitraSQL);
            arbitra.executeUpdate();
            System.out.println("Tabla Arbitra creada correctamente!");
            
            //Disparador
            Statement stm = conn.createStatement();
            stm.execute("create or replace TRIGGER verificar_posesion" +
                "AFTER INSERT ON Juega_Local" +
                "FOR EACH ROW" +
                    "DECLARE " +
                    "total_posesiones INT;" +
                    "pos_local INT;" +
                    "pos_vis INT; " +
                    "BEGIN" +

                    "SELECT PosesionLocal INTO pos_local" +
                    "FROM Juega_Local" +
                    "WHERE ID_Partido = :NEW.ID_Partido;" +

                    "-- Obtener la posesión visitante" +
                    "SELECT PosesionVisitante INTO pos_vis" +
                    "FROM Juega_Visitante" +
                    "WHERE ID_Partido = :NEW.ID_Partido;" +

                    "--Calculamos el total de la posesion" +
                    "total_posesiones := pos_local + pos_vis;" +

                    "-- Verificar que la suma es igual a 100" +
                    "IF total_posesiones <> 100 THEN" +
                        "RAISE_APPLICATION_ERROR(-20001, 'La suma de las posesiones de balón no es igual a 100');" +
                    "END IF;" +
                    "END;");
            
            conn.commit();
            
        }catch (SQLException e) {
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
     * Creación de las tablas independientes
     */
    public static void inicializarSistemaPartidosIndependientes(){
        try {            
            
            System.out.println("");
            
            
            
            
            //CREACION DE TABLAS
            System.out.println("Creando las tablas...");
            System.out.println("-----------------------");
            
            //Creamos la tabla Partido
            String createPartidoSQL = "CREATE TABLE Partido(ID_Partido VARCHAR(20) PRIMARY KEY NOT NULL, Lugar VARCHAR(20), Fecha DATE)";
            PreparedStatement createPartidoStatement = conn.prepareStatement(createPartidoSQL);
            createPartidoStatement.executeUpdate();
            System.out.println("Tabla Partido creada correctamente!");
     
            
            //Escribimos los cambios a la BD
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
     * Funcion para generar nuevos identificadores de partidos. Esta funcion se usa cada vez que damos de alta un nuevo partido
     * @return nuevo identificador
     */
    private static int obtenerNuevoIDPartido(){
          int nuevoIDPartido = 1; //valor para el primer pedido de todos
          
          try {
              String query = "SELECT MAX(ID_Partido)  FROM Partido";
              PreparedStatement stmt = conn.prepareStatement(query);
              ResultSet rs = stmt.executeQuery();
              
              if (rs.next()){
                  nuevoIDPartido = rs.getInt(1) + 1;
              }
          }catch (SQLException e){
              e.printStackTrace();
          }
          
          return nuevoIDPartido;
     }    
    
    /**
     * Dar de alta un nuevo partido
     * @param idPartido identificador unico del partido
     * @param equipoLocal nombre del equipo local
     * @param equipoVisitante nombre del equipo visitante
     * @param lugar lugar del partido 
     * @param fechaPedido  fecha del partido
     */
    private static void altaNuevoPartido(String equipoLocal, String equipoVisitante, String lugar, String dniArbitro,  java.sql.Date fechaPartido){
        try{
            
            //Scanner scanner = new Scanner (System.in);
            //Scanner scanner2 = new Scanner (System.in);
            
            
            int idPartido = obtenerNuevoIDPartido();
            
            
            //LO PRIMERO DE TODO SERÁ DAR DE ALTA LA TABLA PARTIDO COMO TAL
            String query = "INSERT INTO Partido (ID_Partido, Lugar, Fecha) VALUES (?, ? ,?)";
            PreparedStatement ptsmt = conn.prepareStatement(query);
            ptsmt.setInt(1, idPartido);
            ptsmt.setString(2, lugar);
            ptsmt.setDate(3, fechaPartido);
            
            ptsmt.executeUpdate();
            
            //AHORA PARA JUEGA LOCAL Y JUEGA VISITANTE AÑADIMOS EL CLUB Y ESTE MISMO ID DE PARTIDO
            String query2 = "INSERT INTO Juega_Local (ID_Partido, Nombre_Club) VALUES (?, ?)";
            PreparedStatement ptsmt2 = conn.prepareStatement(query2);
            ptsmt2.setInt(1, idPartido);
            ptsmt2.setString(2, equipoLocal);
            
            ptsmt2.executeUpdate();
            
            String query3 = "INSERT INTO Juega_Visitante (ID_Partido, Nombre_Club) VALUES (?, ?)";
            PreparedStatement ptsmt3 = conn.prepareStatement(query3);
            ptsmt3.setInt(1, idPartido);
            ptsmt3.setString(2, equipoVisitante);
            
            ptsmt3.executeUpdate();
            
            //FINALMENTE INTRODUCIMOS EL ARBIRTRO EN LA TABLA QUE RELACIONA AL ARBITRO Y AL PARTIDO
            String query4 = "INSERT INTO Arbitra (ID_Partido, DNI_Arbitro) VALUES (?, ?)";
            PreparedStatement ptsmt4 = conn.prepareStatement(query4);
            ptsmt4.setInt(1, idPartido);
            ptsmt4.setString(2, dniArbitro);
            
            ptsmt4.executeUpdate();
            
            //SAVEPOINT: Savepoint para volver al inicio del proceso
            Savepoint st_inicial = conn.setSavepoint();
            
            conn.commit();
            
   
            
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Este procedimiento sirve para introducir los datos estadisticos de un partido
     */
    private static void datosEstadisticos(){
        try{
        Scanner scanner = new Scanner (System.in);
        
        //Primero pedimos el id del partido al cual queremos añadir los datos
        System.out.print("\nIntroduce el identificador del partido: \n");
        String idPartido = scanner.nextLine();
        
        //Ahora pedimos y añadimos los datos que nos proporcionen
        
        //EQUIPO LOCAL
        System.out.print("\nNumero de goles: \n");
        String goles = scanner.nextLine();
        if (!goles.equals("")) {
            String querygoles = "UPDATE Juega_Local SET golesLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stgoles = conn.prepareStatement(querygoles);
            stgoles.setString(1, goles);
            stgoles.setString(2, idPartido);
            
            stgoles.executeUpdate();
        }
        
        System.out.print("\nNumero de tarjetas: \n");
        String tarjetas = scanner.nextLine();
        if (!tarjetas.equals("")) {
            String querytarjetas = "UPDATE Juega_Local SET tarjetasLocal = ? WHERE ID_Partido = ?";
            PreparedStatement sttarejtas = conn.prepareStatement(querytarjetas);
            sttarejtas.setString(1, tarjetas);
            sttarejtas.setString(2, idPartido);
            
            sttarejtas.executeUpdate();
        }
        
        System.out.print("\nNumero de asistencias: \n");
        String asistencias = scanner.nextLine();
        if (!asistencias.equals("")) {
            String queryasistencias = "UPDATE Juega_Local SET asistenciasLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stasistencias = conn.prepareStatement(queryasistencias);
            stasistencias.setString(1, asistencias);
            stasistencias.setString(2, idPartido);
            
            stasistencias.executeUpdate();
        }
        
        System.out.print("\nNumero de posesion: \n");
        String posesion = scanner.nextLine();
        if (!posesion.equals("")) {
            String queryposesion = "UPDATE Juega_Local SET posesionLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stposesion = conn.prepareStatement(queryposesion);
            stposesion.setString(1, posesion);
            stposesion.setString(2, idPartido);
            
            stposesion.executeUpdate();
        }
        
        System.out.print("\nNumero de remates: \n");
        String remates = scanner.nextLine();
        if (!remates.equals("")) {
            String queryremates = "UPDATE Juega_Local SET rematesLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stremates = conn.prepareStatement(queryremates);
            stremates.setString(1, remates);
            stremates.setString(2, idPartido);
            
            stremates.executeUpdate();
        }
        
        System.out.print("\nNumero de remates a porteria: \n");
        String rematesPorteria = scanner.nextLine();
        if (!rematesPorteria.equals("")) {
            String queryrematesPorteria= "UPDATE Juega_Local SET rematesPorteriaLocal = ? WHERE ID_Partido = ?";
            PreparedStatement strematesPorteria = conn.prepareStatement(queryrematesPorteria);
            strematesPorteria.setString(1, rematesPorteria);
            strematesPorteria.setString(2, idPartido);
            
            strematesPorteria.executeUpdate();
        }
        
        System.out.print("\nNumero de saques de esquina: \n");
        String saques = scanner.nextLine();
        if (!saques.equals("")) {
            String querySaques= "UPDATE Juega_Local SET saquesEsquinaLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stsaques = conn.prepareStatement(querySaques);
            stsaques.setString(1, saques);
            stsaques.setString(2, idPartido);
            
            stsaques.executeUpdate();
        }
        
        System.out.print("\nNumero de fueras de juego: \n");
        String fueras = scanner.nextLine();
        if (!fueras.equals("")) {
            String queryFueras= "UPDATE Juega_Local SET fuerasJuegoLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stfueras = conn.prepareStatement(queryFueras);
            stfueras.setString(1, fueras);
            stfueras.setString(2, idPartido);
            
            stfueras.executeUpdate();
        }
        
        System.out.print("\nNumero de pases: \n");
        String pases = scanner.nextLine();
        if (!pases.equals("")) {
            String queryPases = "UPDATE Juega_Local SET pasesLocal = ? WHERE ID_Partido = ?";
            PreparedStatement stpases = conn.prepareStatement(queryPases);
            stpases.setString(1, pases);
            stpases.setString(2, idPartido);
            
            stpases.executeUpdate();
        }
        
        //EQUIPO VISITANTE
        System.out.print("\nNumero de goles: \n");
        String golesv = scanner.nextLine();
        if (!golesv.equals("")) {
            String querygoles = "UPDATE Juega_Visitante SET golesVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stgoles = conn.prepareStatement(querygoles);
            stgoles.setString(1, golesv);
            stgoles.setString(2, idPartido);
            
            stgoles.executeUpdate();
        }
        
        System.out.print("\nNumero de tarjetas: \n");
        String tarjetasv = scanner.nextLine();
        if (!tarjetasv.equals("")) {
            String querytarjetas = "UPDATE Juega_Visitante SET tarjetasVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement sttarejtas = conn.prepareStatement(querytarjetas);
            sttarejtas.setString(1, tarjetasv);
            sttarejtas.setString(2, idPartido);
            
            sttarejtas.executeUpdate();
        }
        
        System.out.print("\nNumero de asistencias: \n");
        String asistenciasv = scanner.nextLine();
        if (!asistenciasv.equals("")) {
            String queryasistencias = "UPDATE Juega_Visitante SET asistenciasVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stasistencias = conn.prepareStatement(queryasistencias);
            stasistencias.setString(1, asistenciasv);
            stasistencias.setString(2, idPartido);
            
            stasistencias.executeUpdate();
        }
        
        System.out.print("\nNumero de posesion: \n");
        String posesionv = scanner.nextLine();
        if (!posesionv.equals("")) {
            String queryposesion = "UPDATE Juega_Visitante SET posesionVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stposesion = conn.prepareStatement(queryposesion);
            stposesion.setString(1, posesionv);
            stposesion.setString(2, idPartido);
            
            stposesion.executeUpdate();
        }
        
        System.out.print("\nNumero de remates: \n");
        String rematesv = scanner.nextLine();
        if (!rematesv.equals("")) {
            String queryremates = "UPDATE Juega_Visitante SET rematesVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stremates = conn.prepareStatement(queryremates);
            stremates.setString(1, rematesv);
            stremates.setString(2, idPartido);
            
            stremates.executeUpdate();
        }
        
        System.out.print("\nNumero de remates a porteria: \n");
        String rematesPorteriav = scanner.nextLine();
        if (!rematesPorteriav.equals("")) {
            String queryrematesPorteria= "UPDATE Juega_Visitante SET rematesVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement strematesPorteria = conn.prepareStatement(queryrematesPorteria);
            strematesPorteria.setString(1, rematesPorteriav);
            strematesPorteria.setString(2, idPartido);
            
            strematesPorteria.executeUpdate();
        }
        
        System.out.print("\nNumero de saques de esquina: \n");
        String saquesv = scanner.nextLine();
        if (!saquesv.equals("")) {
            String querySaques= "UPDATE Juega_Visitante SET saquesEsquinaVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stsaques = conn.prepareStatement(querySaques);
            stsaques.setString(1, saquesv);
            stsaques.setString(2, idPartido);
            
            stsaques.executeUpdate();
        }
        
        System.out.print("\nNumero de fueras de juego: \n");
        String fuerasv = scanner.nextLine();
        if (!fuerasv.equals("")) {
            String queryFueras= "UPDATE Juega_Visitante SET fuerasJuegoVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stfueras = conn.prepareStatement(queryFueras);
            stfueras.setString(1, fuerasv);
            stfueras.setString(2, idPartido);
            
            stfueras.executeUpdate();
        }
        
        System.out.print("\nNumero de pases: \n");
        String pasesv = scanner.nextLine();
        if (!pases.equals("")) {
            String queryPases = "UPDATE Juega_Visitante SET pasesVisitante = ? WHERE ID_Partido = ?";
            PreparedStatement stpases = conn.prepareStatement(queryPases);
            stpases.setString(1, pasesv);
            stpases.setString(2, idPartido);
            
            stpases.executeUpdate();
        }
        
        
        
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        
    }
    
    /**
     * Este procedimiento muestra todos los datos de un único partido: los asociados a Partido, juega local/visitante y arbitra
     * @param idPartido id del partido del que queremos mostrar los datos
     */
    private static void mostrarDatosPartido(String idPartido){
        
        try{
            
            
            if (!partidoExists(idPartido)){
                System.out.println("No existe partido con este id. Saliendo ... ");
                
                return;
            }
            
            System.out.println("\nMostrando los datos del partido...\n");
            Statement smt = conn.createStatement();
            ResultSet resultSet;
            
            //tenemos que mostrar 4 tablas --> partido, juega local, juega visitante y arbitra
            for (int i = 0; i < 4; i++)
            {
                
                String show_table_query;
                
                switch(i){
                    case 0: //Partido
                        show_table_query = "SELECT * FROM PARTIDO WHERE ID_Partido = ?";
                        
                        PreparedStatement stmPartido = conn.prepareStatement(show_table_query);
                        stmPartido.setInt(1, Integer.parseInt(idPartido));
                        resultSet = stmPartido.executeQuery();
                        
                        System.out.println("Tabla Partido");
                        System.out.println("-----------------------");
                        System.out.println("ID Partido\t\tLugar\t\tFecha");
                        System.out.println("---------\t\t--------\t------");
                        
                        while(resultSet.next()){
                    
                            System.out.println(resultSet.getString("ID_Partido") + "\t\t\t" + resultSet.getString("Lugar") + "\t" + resultSet.getString("Fecha"));
                        }
                   
                        break;
                        
                    case 1:  //Juega local
                        show_table_query = "SELECT * FROM Juega_Local WHERE ID_Partido = ?";
                        
                        PreparedStatement stmLocal = conn.prepareStatement(show_table_query);
                        stmLocal.setInt(1, Integer.parseInt(idPartido));
                        resultSet = stmLocal.executeQuery();
                        
                        System.out.println("Tabla Juega_Local");
                        System.out.println("-----------------------");
                        System.out.println("ID Partido\t\tEquipo\t\tGoles\t\tTarjetas\t\tAsistencias\t\tPosesion\t\tRemates\t\tRemates porteria\t\tSaques de esquina\t\tFueras de juego\t\tPases");
                        System.out.println("---------\t\t--------\t-----\t\t--------\t\t-----------\t\t--------\t\t-------\t\t----------------\t\t-----------------\t\t---------------\t\t-----");
                        
                        while(resultSet.next()){
                    
                            System.out.println(resultSet.getString("ID_Partido") + "\t\t\t" + resultSet.getString("Nombre_Club") + "\t" + resultSet.getString("golesLocal") + "\t\t" + resultSet.getString("tarjetasLocal") + "\t\t\t" + resultSet.getString("asistenciasLocal") + "\t\t\t" + resultSet.getString("posesionLocal") + "\t\t\t" + resultSet.getString("rematesLocal") + "\t\t" + resultSet.getString("rematesPorteriaLocal") + "\t\t\t\t" + resultSet.getString("saquesEsquinaLocal") + "\t\t\t\t" + resultSet.getString("fuerasJuegoLocal") + "\t\t\t" + resultSet.getString("pasesLocal"));
                        }
                        
                        break;
                        
                    case 2: //juega visitante
                        show_table_query = "SELECT * FROM Juega_Visitante WHERE ID_Partido = ?";
                        
                        PreparedStatement stmVisitante = conn.prepareStatement(show_table_query);
                        stmVisitante.setInt(1, Integer.parseInt(idPartido));
                        resultSet = stmVisitante.executeQuery();
                        
                        System.out.println("Tabla Juega_Visitante");
                        System.out.println("-----------------------");
                        System.out.println("ID Partido\t\tEquipo\t\tGoles\t\tTarjetas\t\tAsistencias\t\tPosesion\t\tRemates\t\tRemates porteria\t\tSaques de esquina\t\tFueras de juego\t\tPases");
                        System.out.println("---------\t\t--------\t-----\t\t--------\t\t-----------\t\t--------\t\t-------\t\t----------------\t\t-----------------\t\t---------------\t\t-----");
                        
                        while(resultSet.next()){
                    
                            System.out.println(resultSet.getString("ID_Partido") + "\t\t\t" + resultSet.getString("Nombre_Club") + "\t\t" + resultSet.getString("golesVisitante") + "\t\t" + resultSet.getString("tarjetasVisitante") + "\t\t\t" + resultSet.getString("asistenciasVisitante") + "\t\t\t" + resultSet.getString("posesionVisitante") + "\t\t\t" + resultSet.getString("rematesVisitante") + "\t\t" + resultSet.getString("rematesPorteriaVisitante") + "\t\t\t\t" + resultSet.getString("saquesEsquinaVisitante") + "\t\t\t\t" + resultSet.getString("fuerasJuegoVisitante") + "\t\t\t" + resultSet.getString("pasesVisitante"));
                        }
                        
                        break;
                        
                    case 3: //Arbitra 
                        show_table_query = "SELECT * FROM Arbitra WHERE ID_Partido = ?";
                        
                        PreparedStatement stmArbitro = conn.prepareStatement(show_table_query);
                        stmArbitro.setInt(1, Integer.parseInt(idPartido));
                        resultSet = stmArbitro.executeQuery();
                        
                        System.out.println("Tabla Arbitra");
                        System.out.println("-----------------------");
                        System.out.println("ID Partido\t\tDni Arbitro");
                        System.out.println("-----------\t\t-----------");
                        
                        while (resultSet.next()){
                            
                            System.out.println(resultSet.getString("ID_Partido") + "\t\t\t" + resultSet.getString("DNI_Arbitro"));
                        }
                        break;
                     
                        
                }
                
            }
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        
    }
    
    
    /**
     * Muestra una lista de los partidos que hay en el sistema. Solo con los datos de la tabla Partido (id, lugar y fecha) y los equipos que se enfrentan (tablas Juega local y visitante)
     * IMPORTANTE: Tengo que tener en cuenta que un partido se ha podido borrar del sistema, entonces para ese Id no habrá ninguna entrada en la base de datos
     * Para poder iterar sobre todos los partidos que hay en el sistema voy a tener que coger el id más grande 
     */
    private static void mostrarListadoPartidos(){
        
        try{
            
            System.out.println("\nMostrando listado de todos los partidos del sitema...\n");
            
            
            
            //Tengo que saber el numero de partidos que hay para poder ir cogiendo de uno en uno los datos 
            //Tengo en cuenta que el primer id es 1
            
            //OBTENCION DEL MAYOR ID DE PARTIDOS
            String sqlMax = "SELECT MAX(ID_Partido) FROM Partido";
            Statement statement = conn.createStatement();
            ResultSet partidos = statement.executeQuery(sqlMax);
            
            int cantidadPartidos = 0;
            
            if (partidos.next()){
                
                cantidadPartidos = partidos.getInt(1);
            }
            
            
            //RECORREMOS CADA PARTIDO DESDE 1 HASTA cantidadPartidos
            
            
            ResultSet resultSet;
            
            for (int i = 1; i <= cantidadPartidos; i++){
                
                //Solo mostramos los partidos para los que existe un id
                //Puede haber ocurrido que hayamos borrado un partido del sistema por eso hay que comprobar
                if (partidoExists(String.valueOf(i))){
                    String tabla_partido = "SELECT * FROM Partido WHERE ID_Partido = ?";
                    String local = "SELECT Nombre_Club FROM Juega_Local WHERE ID_Partido = ?";
                    String visitante = "SELECT Nombre_Club FROM Juega_Visitante WHERE ID_Partido = ?";



                    //Primero mostramos tabla partido
                    PreparedStatement smt = conn.prepareStatement(tabla_partido);
                    smt.setString(1, Integer.toString(i));
                    resultSet = smt.executeQuery();

                    System.out.println("------------------------------------------------------------------------------------");
                    System.out.println("ID_Partido\t\tLugar\t\tFecha");
                    System.out.println("---------\t\t--------\t-------");

                    while(resultSet.next()){

                        System.out.println(resultSet.getString("ID_Partido") + "\t\t\t" + resultSet.getString("Lugar") + "\t" + resultSet.getString("Fecha") + "\n");
                    }

                    //Tabla equipo local
                    smt = conn.prepareStatement(local);
                    smt.setString(1, Integer.toString(i));
                    resultSet = smt.executeQuery();

                    System.out.println("Equipo_Local\t\tEquipo_Visitante");
                    System.out.println("---------\t\t-----------");

                    while(resultSet.next()){

                        System.out.print(resultSet.getString("Nombre_Club") + "\t\t");
                    }


                    //Tabla equipo visitante
                    smt = conn.prepareStatement(visitante);
                    smt.setString(1, Integer.toString(i));
                    resultSet = smt.executeQuery();

                    while(resultSet.next()){

                        System.out.print(resultSet.getString("Nombre_Club"));
                    }
                    
                }
                
                
                           
            }
            System.out.println("\n");
            System.out.println("------------------------------------------------------------------------------------");
            System.out.print("\n\n");
            
            

            
            
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Borrar partido del sistema. 
     * @param id_partido id del partido a eliminar
     */
    private static void borrarPartido(String id_partido){
        
        
        try{
            
            
            if (!partidoExists(id_partido)){
                
                System.out.println("Id no asociado a ningun partido. Saliendo...");
                return;
            }
            
            //Solo me tengo que encargar de borrar la tabla de partido porque a la hora de crear las otras tres tablas se especifica el borrado en cascada
            String sqlDelete = "DELETE FROM Partido WHERE ID_Partido = ?";
            
            // Prepara la declaración SQL con el ID del partido a borrar
            PreparedStatement preparedStatement = conn.prepareStatement(sqlDelete);
            preparedStatement.setInt(1, Integer.parseInt(id_partido)); //con trim elimino los posibles espacios en blanco que pueda tener
            
            int deleted = preparedStatement.executeUpdate();
            
            if(deleted > 0){
                System.out.println("Se ha borrado correctamente del sistema\n");
            }
            else{
                System.out.println("No se ha podido borrar del sistema\n");
            }
            
            conn.commit();
            
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Menu principal del subsistema
     */
    public static void menuPrincipalPartidos(){
        //Presentacion del menu
        System.out.println("SUBSISTEMA DE PARTIDOS");
        System.out.println("-----------------------");
        
        int opcion = -1; //Opcion que ha elegido el usuario
        
        Scanner scanner = new Scanner(System.in); //Entrada del usuario
        
        //Mostramos las opciones al usuario
        while (opcion != 6){ //Mientras no queramos salir del programa
            
            System.out.println("1.- Dar de alta nuevo partido (equipos, arbitro, fecha y lugar)");
            System.out.println("2.- Mostrar los datos de un partido: ");
            System.out.println("3.- Modificar datos de un partido (datos estadisticos) ");
            System.out.println("4.- Mostrar listado de todos los partidos ");
            System.out.println("5.- Borrar un partido");
            System.out.println("6.- Salir");
            
            //Pedimos una opcion al usuario
            System.out.print("Opcion: ");
            opcion = scanner.nextInt();
            
            //Pasamos a la logica
            switch(opcion){
                
                case 1:
                    System.out.print("\nIntroduce los datos del partido:\n");
                    
                    Scanner scanner2 = new Scanner (System.in);
                    
                    System.out.print("\t-Lugar: ");
                    String lugar = scanner2.nextLine();
                    
                    System.out.print("\t-Nombre equipo local: ");
                    String equipoLocal = scanner2.nextLine();
                    
                    System.out.print("\t-Nombre equipo visitante: ");
                    String equipoVisitante = scanner2.nextLine();
                    
                    
                    System.out.print("\t-DNI del arbitro: ");
                    String dniArbitro = scanner2.nextLine();
                    
                    
                    System.out.print("\t-Fecha (dd mm yyyy): ");
                    String fecha = scanner2.nextLine(); //For parsing...
                    
                    
                    //Parseamos la fecha
                    Date fechaUtil;
                    try {
                        fechaUtil = new SimpleDateFormat("dd mm yyyy").parse(fecha);
                        java.sql.Date sqlDate = new java.sql.Date(fechaUtil.getTime());
                                          
                        //Pasamos a dar de alta el nuevo pedido
                        altaNuevoPartido(equipoLocal, equipoVisitante, lugar, dniArbitro ,sqlDate);
                    } catch (ParseException ex) {
                        System.out.println("Formato de la fecha incorrecto!");
                    }
                    
                    
                    break;
                case 2:
                    System.out.print("\t-Introduce el Identificador del partido: ");
                    
                    Scanner scanner3 = new Scanner (System.in);
                    
                    String idPartido = scanner3.nextLine();
                    mostrarDatosPartido(idPartido);
                    break;
                case 3:
                    datosEstadisticos();
                    break;
                case 4:
                    mostrarListadoPartidos();
                    break;
                case 5:
                    System.out.print("\t-Introduce el Identificador del partido: ");
                    String id = scanner.nextLine();
                    borrarPartido(id);
                    break;
                case 6: 
                    System.out.print("Saliendo del subsistema de Partidos... ");
                    break;
                default:
                    System.out.println("Opción no válida! \n----------------------- \n");
                    break;
            }
        }
    }
    
    
}
