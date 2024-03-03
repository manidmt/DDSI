/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import Subsistemas.Arbitros;
import Subsistemas.Entrenadores;
import Subsistemas.Jugadores;
import Subsistemas.Partidos;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;


/**
 *
 * @author javi5454
 */
public class Pruebas {
    public static void main(String[] args){
        try {
            //Conexion BD
            Connection conn1 = null;
            
            Class.forName("oracle.jdbc.OracleDriver");
            
            String dbURL = "jdbc:oracle:thin:@oracle0.ugr.es:1521/practbd.oracle0.ugr.es";
            String username = "x3949965";
            String password = "x3949965";
            
            conn1 = DriverManager.getConnection(dbURL,username, password);
            
            if(conn1 == null){
                System.out.println("Imposible conectar con la base de datos!");
            }
            else{
                conn1.setAutoCommit(false);

                //Arbitros.setConnection(conn1);
                //Arbitros.menuPrincipalArbitro();
                //Entrenadores.setConnection(conn1);
                //Entrenadores.MenuEntrenadores();

                

                //Clubes.setConnection(conn1);


                Jugadores.setConnection(conn1);
                Jugadores.menuJugadores();
                //Partidos.setConnection(conn1);
                //Partidos.menuPrincipalPartidos();



                //Jugadores.setConnection(conn1);
                //Jugadores.menuJugadores();
                //Partidos.setConnection(conn1);
                //Partidos.menuPrincipalPartidos();


                
                try{
                    conn1.close();
                    System.out.println("Conexion cerrada");
                }catch (SQLException ex){
                    System.out.println("Error al cerrar la conexion a la BD");
                }
            }
            
            
            
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch(SQLException ex){
            ex.printStackTrace();
        }
    }
}
