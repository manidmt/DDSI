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
import Subsistemas.Jugadores;

/**
 *
 * @author Jesus Reyes de Toro
 */
public class Clubes {
    
    /**
     * Conexion a nuestra BD
     */
    private static Connection conn;
    
    /**
     * Metodo para setear nuestra conexion
     * @param conn1 Connection conexion a nuestra BD
     */
    public static void setConnection(Connection conn1){
        conn = conn1;
    }
    
    public static void crearTablas(){
        try {            
            
            // CREACION DE TABLAS
            System.out.println("Creando las tablas de clubes...");
            System.out.println("-----------------------");

            // Creamos la tabla Club
            String createClubSQL = "CREATE TABLE CLUB(Nombre_Club VARCHAR(50) PRIMARY KEY NOT NULL, Ciudad VARCHAR(50), Anio_Fundacion INT, Presidente VARCHAR(50), Patrocinadores VARCHAR(50))";
            PreparedStatement createClubStatement = conn.prepareStatement(createClubSQL);
            createClubStatement.executeUpdate();
            System.out.println("Tabla Club creada correctamente!");

            // Creamos la tabla PerteneceJugadores
            String createPerteneceJugadoresSQL = "CREATE TABLE PERTENECE_JUGADORES(Nombre_Club VARCHAR(50) NOT NULL, DNI_Jugador VARCHAR(9) NOT NULL, PRIMARY KEY (DNI_Jugador, Nombre_Club), FOREIGN KEY (DNI_Jugador) REFERENCES Jugadores(DNI_Jugador) ON DELETE CASCADE, FOREIGN KEY (Nombre_Club) REFERENCES Club(Nombre_Club))";
            PreparedStatement createPerteneceJugadoresStatement = conn.prepareStatement(createPerteneceJugadoresSQL);
            createPerteneceJugadoresStatement.executeUpdate();
            System.out.println("Tabla PerteneceJugadores creada correctamente!");

            // Creamos la tabla TieneEntrenador
            String createTieneEntrenadorSQL = "CREATE TABLE TIENE_ENTRENADOR(Nombre_Club VARCHAR(50) NOT NULL, DNI_Entrenadores VARCHAR(9) NOT NULL, PRIMARY KEY (DNI_Entrenadores, Nombre_Club), FOREIGN KEY (DNI_Entrenadores) REFERENCES Entrenadores(DNI_Entrenadores), FOREIGN KEY (Nombre_Club) REFERENCES Club(Nombre_Club))";
            PreparedStatement createTieneEntrenadorStatement = conn.prepareStatement(createTieneEntrenadorSQL);
            createTieneEntrenadorStatement.executeUpdate();
            System.out.println("Tabla TieneEntrenador creada correctamente!");


            // CREAMOS LA CREACION DEL TRIGGER DELETE AHORA: 

            String createTriggerSQL = 
                "DELIMITER //\n" +
                "CREATE TRIGGER before_delete_club\n" +
                "BEFORE DELETE ON CLUB\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "    DECLARE numJugadores INT;\n" +
                "    SELECT COUNT(*) INTO numJugadores\n" +
                "    FROM PERTENECE_JUGADORES\n" +
                "    WHERE Nombre_Club = OLD.Nombre_Club;\n" +
                "    IF numJugadores > 0 THEN\n" +
                "        SIGNAL SQLSTATE '45000'\n" +
                "        SET MESSAGE_TEXT = 'No se puede eliminar el club. Tiene jugadores asociados.';\n" +
                "    END IF;\n" +
                "END //\n" +
                "DELIMITER ;";

        PreparedStatement createTriggerStatement = conn.prepareStatement(createTriggerSQL);
        createTriggerStatement.executeUpdate();
        System.out.println("Trigger before_delete_club creado correctamente!");


            // Escribimos los cambios a la BD
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

    public static void borrarTabla(String nombreTabla) {
    try {
        // Preparación de la query
        String dropTableSQL = "DROP TABLE IF EXISTS " + nombreTabla;
        PreparedStatement dropTableStatement = conn.prepareStatement(dropTableSQL);

        // Ejecutar la query para borrar la tabla
        dropTableStatement.executeUpdate();
        System.out.println("Tabla " + nombreTabla + " borrada correctamente!");
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Error al borrar la tabla " + nombreTabla);
    }
}

    


    /**
     * Alta de nuevo Club 
     */
    public static void altaNuevoClub() {
    try {
        // Input
        Scanner sc = new Scanner(System.in);

        // Preparacion de la query
        String query = "INSERT INTO CLUB (Nombre_Club, Ciudad, Anio_Fundacion, Presidente, Patrocinadores) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ptsmt = conn.prepareStatement(query);

        // Toma de datos del usuario
        System.out.println("ALTA NUEVO CLUB");
        System.out.println("------------------\n");

        Savepoint st_inicial = conn.setSavepoint(); // Savepoint de inicio del proceso

        // Campos del club
        String nombreClub, ciudad, presidente, patrocinadores;
        int anioFundacion;

        System.out.print("Introduce el nombre del nuevo Club: ");
        nombreClub = sc.nextLine();

        // Verificar si el club ya existe
        if (clubExists(nombreClub)) {
            System.out.println("Ya existe un club con ese nombre, saliendo...\n");
            conn.rollback(st_inicial);
            return;
        }

        System.out.print("Introduzca la ciudad del club: ");
        ciudad = sc.nextLine();

        System.out.print("Introduce el año de fundación del club: ");
        anioFundacion = sc.nextInt();
        sc.nextLine(); // Limpiar el buffer de nueva línea

        System.out.print("Introduce el nombre del presidente del club: ");
        presidente = sc.nextLine();

        System.out.print("Introduce los patrocinadores del club: ");
        patrocinadores = sc.nextLine();

        // Completamos la query con los datos
        ptsmt.setString(1, nombreClub);
        ptsmt.setString(2, ciudad);
        ptsmt.setInt(3, anioFundacion);
        ptsmt.setString(4, presidente);
        ptsmt.setString(5, patrocinadores);

        // Realizamos la query y salimos
        ptsmt.executeUpdate();

        conn.commit();
        System.out.println("Club registrado correctamente!");

    } catch (SQLException e) {
        System.out.println(e.getErrorCode());
    }
}

// Método para verificar si un club ya existe
public static boolean clubExists(String nombreClub) {
    try {
        String query = "SELECT COUNT(*) FROM CLUB WHERE Nombre_Club = ?";
        PreparedStatement ptsmt = conn.prepareStatement(query);
        ptsmt.setString(1, nombreClub);

        ResultSet resultSet = ptsmt.executeQuery();
        resultSet.next();

        int count = resultSet.getInt(1);

        return count > 0;
    } catch (SQLException e) {
        System.out.println(e.getErrorCode());
        return false;
    }
}








   /**
     * Método para dar de baja un Club
    */

public static void bajaClub() {
    try {
        // Input
        Scanner sc = new Scanner(System.in);

        // Preparacion de la query
        String queryBorrado = "DELETE FROM CLUB WHERE Nombre_Club = ?";
        PreparedStatement pstmt = conn.prepareStatement(queryBorrado);

        // Toma el nombre del club del usuario
        System.out.println("BORRAR CLUB");
        System.out.println("--------------\n");

        System.out.print("Introduce el nombre del club a eliminar: ");
        String nombreClub = sc.nextLine();

        if (!clubExists(nombreClub)) {
            System.out.println("No existe un club con ese nombre, saliendo...\n");
            return;
        }

        // Obtenemos la información del club a eliminar
        String queryConsulta = "SELECT Ciudad, Anio_Fundacion, Presidente, Patrocinadores FROM CLUB WHERE Nombre_Club = ?";
        PreparedStatement consulta = conn.prepareStatement(queryConsulta);
        consulta.setString(1, nombreClub);
        ResultSet rs = consulta.executeQuery();

        // Borramos el club
        pstmt.setString(1, nombreClub);
        pstmt.executeUpdate();

        conn.commit();

        rs.next();
        System.out.print("Se ha eliminado el club " + nombreClub + " del sistema!\n");
        System.out.println("Información del club eliminado:");
        System.out.println("Ciudad: " + rs.getString("Ciudad"));
        System.out.println("Año de Fundación: " + rs.getInt("Anio_Fundacion"));
        System.out.println("Presidente: " + rs.getString("Presidente"));
        System.out.println("Patrocinadores: " + rs.getString("Patrocinadores"));
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}




/**Funcion para modificar algun dato de un club */

public static void modificarClub() {
    try {
        // Input
        Scanner sc = new Scanner(System.in);

        // Pedimos el nombre del club al usuario
        String nombre;
        System.out.print("Introduce el nombre del club a modificar: ");
        nombre = sc.nextLine();

        if (!clubExists(nombre)) {
            System.out.println("No existe el club con ese nombre, saliendo...\n");
            return;
        }

        // INICIO DE TRANSACCIÓN DE MODIFICACIÓN
        Savepoint st_inicial = conn.setSavepoint();

        // Savepoint después de modificación
        Savepoint st_modificacion_club = null;

        // Menú
        boolean keep = true;

        while (keep) {
            int option;

            System.out.println("\nMODIFICAR DATOS DE UN CLUB");
            System.out.println("--------------------------\n");

            System.out.println("1.- Modificar Patrocinadores");
            System.out.println("2.- Modificar Ciudad");
            System.out.println("3.- Modificar Año de Fundación");
            System.out.println("4.- Modificar Presidente");
            System.out.println("5.- Salir");
            System.out.print("Opción: ");

            option = sc.nextInt();
            sc.nextLine(); // Limpiar el buffer de nueva línea

            switch (option) {
                case 1:
                    System.out.println("Introduzca los nuevos patrocinadores del club:");

                    String patrocinadores = sc.nextLine();

                    if (modificarPatrocinadores(nombre, patrocinadores)) {
                        System.out.println("Patrocinadores del club modificados correctamente!");

                        // SAVEPOINT: Savepoint por si una modificación falla
                        st_modificacion_club = conn.setSavepoint();
                    } else {
                        System.out.println("ERROR: No ha sido posible modificar los patrocinadores del club!\n");

                        // Volvemos al punto de la anterior modificación en la base de datos
                        if (st_modificacion_club != null) {
                            try {
                                conn.rollback(st_modificacion_club);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;

                case 2:
                    System.out.println("Introduzca la nueva ciudad del club:");

                    String ciudad = sc.nextLine();

                    if (modificarCiudad(nombre, ciudad)) {
                        System.out.println("Ciudad del club modificada correctamente!");

                        // SAVEPOINT: Savepoint por si una modificación falla
                        st_modificacion_club = conn.setSavepoint();
                    } else {
                        System.out.println("ERROR: No ha sido posible modificar la ciudad del club!\n");

                        // Volvemos al punto de la anterior modificación en la base de datos
                        if (st_modificacion_club != null) {
                            try {
                                conn.rollback(st_modificacion_club);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;

                case 3:
                    System.out.println("Introduzca el nuevo año de fundación del club:");

                    int anioFundacion = sc.nextInt();
                    sc.nextLine(); // Limpiar el buffer de nueva línea

                    if (modificarAnioFundacion(nombre, anioFundacion)) {
                        System.out.println("Año de fundación del club modificado correctamente!");

                        // SAVEPOINT: Savepoint por si una modificación falla
                        st_modificacion_club = conn.setSavepoint();
                    } else {
                        System.out.println("ERROR: No ha sido posible modificar el año de fundación del club!\n");

                        // Volvemos al punto de la anterior modificación en la base de datos
                        if (st_modificacion_club != null) {
                            try {
                                conn.rollback(st_modificacion_club);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;

                case 4:
                    System.out.println("Introduzca el nuevo presidente del club:");

                    String presidente = sc.nextLine();

                    if (modificarPresidente(nombre, presidente)) {
                        System.out.println("Presidente del club modificado correctamente!");

                        // SAVEPOINT: Savepoint por si una modificación falla
                        st_modificacion_club = conn.setSavepoint();
                    } else {
                        System.out.println("ERROR: No ha sido posible modificar el presidente del club!\n");

                        // Volvemos al punto de la anterior modificación en la base de datos
                        if (st_modificacion_club != null) {
                            try {
                                conn.rollback(st_modificacion_club);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    break;

                case 5:
                    keep = false;
                    break;

                default:
                    System.out.println("Opción no válida. Intente nuevamente.");
                    break;
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


private static boolean modificarPatrocinadores(String nombreClub, String nuevosPatrocinadores) {
    try {
        // Preparación de la query
        String query = "UPDATE CLUB SET Patrocinadores = ? WHERE Nombre_Club = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);

        // Completar la query con los datos
        pstmt.setString(1, nuevosPatrocinadores);
        pstmt.setString(2, nombreClub);

        // Realizar la query
        pstmt.executeUpdate();

        // COMMIT de la transacción
        conn.commit();

        return true;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

private static boolean modificarCiudad(String nombreClub, String nuevaCiudad) {
    try {
        // Preparación de la query
        String query = "UPDATE CLUB SET Ciudad = ? WHERE Nombre_Club = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);

        // Completar la query con los datos
        pstmt.setString(1, nuevaCiudad);
        pstmt.setString(2, nombreClub);

        // Realizar la query
        pstmt.executeUpdate();

        // COMMIT de la transacción
        conn.commit();

        return true;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

private static boolean modificarAnioFundacion(String nombreClub, int nuevoAnioFundacion) {
    try {
        // Preparación de la query
        String query = "UPDATE CLUB SET Anio_Fundacion = ? WHERE Nombre_Club = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);

        // Completar la query con los datos
        pstmt.setInt(1, nuevoAnioFundacion);
        pstmt.setString(2, nombreClub);

        // Realizar la query
        pstmt.executeUpdate();

        // COMMIT de la transacción
        conn.commit();

        return true;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

private static boolean modificarPresidente(String nombreClub, String nuevoPresidente) {
    try {
        // Preparación de la query
        String query = "UPDATE CLUB SET Presidente = ? WHERE Nombre_Club = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);

        // Completar la query con los datos
        pstmt.setString(1, nuevoPresidente);
        pstmt.setString(2, nombreClub);

        // Realizar la query
        pstmt.executeUpdate();

        // COMMIT de la transacción
        conn.commit();

        return true;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}







/** 
 * Funcion que sea para asociar un club a un jugador 
 * 
*/

public static void asociarClubAJugador() {
    try {
        // Input
        Scanner sc = new Scanner(System.in);

        // Preparación de la query
        String queryAsociacion = "INSERT INTO PERTENECE_JUGADORES (Nombre_Club, DNI_Jugador) VALUES (?, ?)";
        PreparedStatement pstmtAsociacion = conn.prepareStatement(queryAsociacion);

        // Toma de datos del usuario
        System.out.println("ASOCIAR CLUB A UN JUGADOR");
        System.out.println("--------------------------\n");

        System.out.print("Introduce el nombre del club: ");
        String nombreClub = sc.nextLine();

        System.out.print("Introduce el DNI del jugador: ");
        String dniJugador = sc.nextLine();

        // Verificar si el jugador y el club existen
        if (!Jugadores.playerExists(dniJugador) || !clubExists(nombreClub)) {
            System.out.println("El jugador o el club no existe, saliendo...\n");
            return;
        }

//        if (!clubExists(nombreClub)) {
//            System.out.println("El jugador o el club no existe, saliendo...\n");
//            return;
//        }



        // Completamos la query con los datos
        pstmtAsociacion.setString(1, nombreClub);
        pstmtAsociacion.setString(2, dniJugador);

        // Realizamos la query y salimos
        pstmtAsociacion.executeUpdate();

        conn.commit();
        System.out.println("Club asociado al jugador correctamente!");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


/** 
 * Funcion que sea para asociar un club a un entrenador 
 * 
*/

public static void asociarClubAEntrenador() {
    try {
        // Input
        Scanner sc = new Scanner(System.in);

        // Preparación de la query
        String queryAsociacion = "INSERT INTO TIENE_ENTRENADOR (Nombre_Club, DNI_Entrenadores) VALUES (?, ?)";
        PreparedStatement pstmtAsociacion = conn.prepareStatement(queryAsociacion);

        // Toma de datos del usuario
        System.out.println("ASOCIAR CLUB A UN ENTRENADOR");
        System.out.println("-----------------------------\n");

        System.out.print("Introduce el nombre del club: ");
        String nombreClub = sc.nextLine();

        System.out.print("Introduce el DNI del entrenador: ");
        String dniEntrenador = sc.nextLine();

//        // Verificar si el entrenador y el club existen
//        if (!EntrenadorExiste(dniEntrenador) || !clubExists(nombreClub)) {
//            System.out.println("El entrenador o el club no existe, saliendo...\n");
//            return;
//        }

        if (!clubExists(nombreClub)) {
                    System.out.println("El jugador o el club no existe, saliendo...\n");
                    return;
                }


        // Completamos la query con los datos
        pstmtAsociacion.setString(1, nombreClub);
        pstmtAsociacion.setString(2, dniEntrenador);

        // Realizamos la query y salimos
        pstmtAsociacion.executeUpdate();

        conn.commit();
        System.out.println("Club asociado al entrenador correctamente!");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


/** 
 * Funcion que muestra el listado de clubes de la Base de Datos. 
 * 
 */

private static void mostrarListadoClubes() {
    try {
        System.out.println("\nMostrando listado de todos los clubes del sistema...\n");

        String query = "SELECT * FROM CLUB";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("Nombre\t\tCiudad\t\tAño de Fundación\tPresidente\tPatrocinadores\tJugadores\tEntrenadores");
        System.out.println("--------\t\t--------\t---------------------\t--------\t-----------------\t----------\t------------");

        while (rs.next()) {
            String nombreClub = rs.getString("Nombre_Club");
            String ciudad = rs.getString("Ciudad");
            int anioFundacion = rs.getInt("Anio_Fundacion");
            String presidente = rs.getString("Presidente");
            String patrocinadores = rs.getString("Patrocinadores");

            // Get the list of players associated with the club
            String queryJugadores = "SELECT DNI_Jugador FROM PERTENECE_JUGADORES WHERE Nombre_Club = ?";
            PreparedStatement pstmtJugadores = conn.prepareStatement(queryJugadores);
            pstmtJugadores.setString(1, nombreClub);
            ResultSet rsJugadores = pstmtJugadores.executeQuery();
            String listaJugadores = obtenerLista(rsJugadores, "DNI_Jugador");

            // Get the list of trainers associated with the club
            String queryEntrenadores = "SELECT DNI_Entrenadores FROM TIENE_ENTRENADOR WHERE Nombre_Club = ?";
            PreparedStatement pstmtEntrenadores = conn.prepareStatement(queryEntrenadores);
            pstmtEntrenadores.setString(1, nombreClub);
            ResultSet rsEntrenadores = pstmtEntrenadores.executeQuery();
            String listaEntrenadores = obtenerLista(rsEntrenadores, "DNI_Entrenadores");

            System.out.printf("%s\t\t%s\t\t%d\t\t\t%s\t\t%s\t\t%s\t\t%s\n",
                    nombreClub, ciudad, anioFundacion, presidente, patrocinadores, listaJugadores, listaEntrenadores);
        }

        System.out.println("----------------------------------------------------------------------------------------------");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


// Método para obtener una lista de valores de una columna de un ResultSet
private static String obtenerLista(ResultSet resultSet, String columna) throws SQLException {
    StringBuilder lista = new StringBuilder();

    while (resultSet.next()) {
        if (lista.length() > 0) {
            lista.append(", ");
        }
        lista.append(resultSet.getString(columna));
    }

    return lista.toString();
}


/**
 * MENU PRINCIPAL PARA LOS CLUBES
 */

public static void menuPrincipalClubes() {
    // Presentacion del menu
    System.out.println("SUBSISTEMA DE CLUBES");
    System.out.println("-----------------------");

    int opcion = -1; // Opcion que ha elegido el usuario

    Scanner scanner = new Scanner(System.in); // Entrada del usuario

    // Mostramos las opciones al usuario
    while (opcion != 8) { // Mientras no queramos salir del programa
        System.out.println("0.- Borrar tablas");
        System.out.println("1.- Crear tablas");
        System.out.println("2.- Dar de alta nuevo club");
        System.out.println("3.- Asociar club a un jugador");
        System.out.println("4.- Asociar club a un entrenador");
        System.out.println("5.- Modificar datos de un club");
        System.out.println("6.- Mostrar listado de todos los clubes");
        System.out.println("7.- Dar de baja a un club");
        System.out.println("8.- Salir");

        // Pedimos una opcion al usuario
        System.out.print("Opcion: ");
        opcion = scanner.nextInt();

        // Pasamos a la lógica
        switch (opcion) {
            
            case 0: 
                String[] nombresTablas = {"CLUB", "PERTENECE_JUGADORES", "TIENE_ENTRENADOR"};  

                for (String nombreTabla : nombresTablas) {
                    borrarTabla(nombreTabla);
                }                
                break; 
            case 1:
                crearTablas();
                break;
            case 2:
                altaNuevoClub();
                break;
            case 3:
                asociarClubAJugador();
                break;
            case 4:
                asociarClubAEntrenador();
                break;
            case 5:
                modificarClub();
                break;
            case 6:
                mostrarListadoClubes();
                break;
            case 7:
                bajaClub();
                break;
            case 8:
                break;
            default:
                System.out.println("Opción no válida! \n----------------------- \n");
                break;
        }
    }
}

}
