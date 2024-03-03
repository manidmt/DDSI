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
 
/**
 *
 * @author Valeria Borrajo Yusty
 */

public class Entrenadores {

    private static Connection conn;

    public static void setConnection(Connection conn1) {
        conn = conn1;
    }

    public static void crearTablas() {
        try {
            // Código para crear la tabla de entrenadores
            System.out.println("Creando la tabla de entrenadores...");
            String createEntrenadoresSQL = "CREATE TABLE ENTRENADORES(" +
                    "DNI_Entrenadores VARCHAR2(9) PRIMARY KEY," +
                    "Nombre VARCHAR2(20) NOT NULL," +
                    "Apellido_Paterno VARCHAR(20) NOT NULL," +
                    "Apellido_Materno VARCHAR(20)," +
                    "Fecha_Nacimiento Date" +
                    ")";
            PreparedStatement createEntrenadoresStatement = conn.prepareStatement(createEntrenadoresSQL);
            createEntrenadoresStatement.executeUpdate();
            System.out.println("Tabla de entrenadores creada correctamente!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void borrarTabla(String nombreTabla) {
        try {
            // Código para borrar la tabla de entrenadores
            System.out.println("Borrando la tabla de entrenadores...");
            String dropTableSQL = "DROP TABLE IF EXISTS " + nombreTabla;
            PreparedStatement dropTableStatement = conn.prepareStatement(dropTableSQL);
            dropTableStatement.executeUpdate();
            System.out.println("Tabla de entrenadores borrada correctamente!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al borrar la tabla de entrenadores");
        }
    }

    public static void altaNuevoEntrenador() {
        try {
            Scanner sc = new Scanner(System.in);

            System.out.println("ALTA NUEVO ENTRENADOR");
            System.out.println("-----------------------\n");

            // Toma de datos del usuario
            System.out.print("Introduce el DNI del nuevo entrenador: ");
            String dniEntrenador = sc.nextLine();

            // Verificar si el entrenador ya existe
            if (entrenadorExists(dniEntrenador)) {
                System.out.println("Ya existe un entrenador con ese DNI, saliendo...\n");
                return;
            }

            System.out.print("Introduce el nombre del nuevo entrenador: ");
            String nombre = sc.nextLine();

            System.out.print("Introduce el apellido paterno del nuevo entrenador: ");
            String apellidoPaterno = sc.nextLine();

            System.out.print("Introduce el apellido materno del nuevo entrenador: ");
            String apellidoMaterno = sc.nextLine();

            System.out.print("Introduce la fecha de nacimiento del nuevo entrenador (yyyy-MM-dd): ");
            String fechaNacimientoStr = sc.nextLine();
            

            
            
      
                    //Parseamos la fecha
                    Date fechaUtil;
                    java.sql.Date sqlDate = null;
                    try {
                        fechaUtil = new SimpleDateFormat("dd mm yyyy").parse(fechaNacimientoStr);
                         sqlDate =  new java.sql.Date(fechaUtil.getTime());
                    }catch (ParseException e){
                                System.out.println("Formato de la fecha incorrecto!");
                    }
                    
            
            
            
            
            // Preparación de la query
            String query = "INSERT INTO Entrenadores (DNI_Entrenadores, Nombre, Apellido_Paterno, Apellido_Materno, Fecha_Nacimiento) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);

            // Completamos la query con los datos
            pstmt.setString(1, dniEntrenador);
            pstmt.setString(2, nombre);
            pstmt.setString(3, apellidoPaterno);
            pstmt.setString(4, apellidoMaterno);
            pstmt.setDate(5, sqlDate);

            // Realizamos la query y salimos
            pstmt.executeUpdate();
            conn.commit();
            System.out.println("Entrenador registrado correctamente!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean entrenadorExists(String dniEntrenador) {
        try {
            String query = "SELECT COUNT(*) FROM Entrenadores WHERE DNI_Entrenadores = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, dniEntrenador);

            ResultSet resultSet = pstmt.executeQuery();
            resultSet.next();

            int count = resultSet.getInt(1);

            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void bajaEntrenador() {
        try {
            Scanner sc = new Scanner(System.in);

            System.out.println("BORRAR ENTRENADOR");
            System.out.println("------------------\n");

            // Toma el DNI del entrenador del usuario
            System.out.print("Introduce el DNI del entrenador a eliminar: ");
            String dniEntrenador = sc.nextLine();

            if (!entrenadorExists(dniEntrenador)) {
                System.out.println("No existe un entrenador con ese DNI, saliendo...\n");
                return;
            }

            // Preparacion de la query para obtener información del entrenador antes de eliminarlo
            String queryConsulta = "SELECT Nombre, Apellido_Paterno, Apellido_Materno, Fecha_Nacimiento FROM Entrenadores WHERE DNI_Entrenadores = ?";
            PreparedStatement consulta = conn.prepareStatement(queryConsulta);
            consulta.setString(1, dniEntrenador);
            ResultSet rs = consulta.executeQuery();

            // Preparacion de la query para borrar el entrenador
            String queryBorrado = "DELETE FROM Entrenadores WHERE DNI_Entrenadores = ?";
            PreparedStatement pstmtBorrado = conn.prepareStatement(queryBorrado);
            pstmtBorrado.setString(1, dniEntrenador);
            pstmtBorrado.executeUpdate();

            conn.commit();

            rs.next();
            System.out.println("Se ha eliminado el entrenador " + dniEntrenador + " del sistema!");
            System.out.println("Información del entrenador eliminado:");
            System.out.println("Nombre: " + rs.getString("Nombre"));
            System.out.println("Apellido Paterno: " + rs.getString("Apellido_Paterno"));
            System.out.println("Apellido Materno: " + rs.getString("Apellido_Materno"));
            System.out.println("Fecha de Nacimiento: " + rs.getDate("Fecha_Nacimiento"));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void mostrarListadoEntrenadores() {
        try {
            System.out.println("\nMostrando listado de todos los entrenadores del sistema...\n");

            String query = "SELECT * FROM Entrenadores";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("-------------------------------------------------------------------");
            System.out.println("DNI_Entrenadores\tNombre\t\tApellido_Paterno\tApellido_Materno\tFecha_Nacimiento");
            System.out.println("--------------\t--------\t-------------------\t----------------\t------------------");

            while (rs.next()) {
                String dniEntrenador = rs.getString("DNI_Entrenadores");
                String nombre = rs.getString("Nombre");
                String apellidoPaterno = rs.getString("Apellido_Paterno");
                String apellidoMaterno = rs.getString("Apellido_Materno");
                java.sql.Date fechaNacimiento = rs.getDate("Fecha_Nacimiento");

                System.out.printf("%-15s\t%-15s\t%-20s\t%-20s\t%-15s\n", dniEntrenador, nombre, apellidoPaterno, apellidoMaterno, fechaNacimiento);
            }

            System.out.println("-------------------------------------------------------------------");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void modificarDatosEntrenador() {
        try {
            Scanner sc = new Scanner(System.in);

            System.out.println("MODIFICAR DATOS DE ENTRENADOR");
            System.out.println("---------------------------------\n");

            // Toma el DNI del entrenador del usuario
            System.out.print("Introduce el DNI del entrenador a modificar: ");
            String dniEntrenador = sc.nextLine();

            if (!entrenadorExists(dniEntrenador)) {
                System.out.println("No existe un entrenador con ese DNI, saliendo...\n");
                return;
            }

            // Preparacion de la query para obtener información del entrenador antes de modificarlo
            String queryConsulta = "SELECT Nombre, Apellido_Paterno, Apellido_Materno, Fecha_Nacimiento FROM Entrenadores WHERE DNI_Entrenadores = ?";
            PreparedStatement consulta = conn.prepareStatement(queryConsulta);
            consulta.setString(1, dniEntrenador);
            ResultSet rs = consulta.executeQuery();

            rs.next();
            System.out.println("Información actual del entrenador:");
            System.out.println("Nombre: " + rs.getString("Nombre"));
            System.out.println("Apellido Paterno: " + rs.getString("Apellido_Paterno"));
            System.out.println("Apellido Materno: " + rs.getString("Apellido_Materno"));
            System.out.println("Fecha de Nacimiento: " + rs.getDate("Fecha_Nacimiento"));

            // Toma de nuevos datos
            System.out.print("Introduce el nuevo nombre del entrenador (dejar en blanco para no modificar): ");
            String nuevoNombre = sc.nextLine();

            System.out.print("Introduce el nuevo apellido paterno del entrenador (dejar en blanco para no modificar): ");
            String nuevoApellidoPaterno = sc.nextLine();

            System.out.print("Introduce el nuevo apellido materno del entrenador (dejar en blanco para no modificar): ");
            String nuevoApellidoMaterno = sc.nextLine();

            System.out.print("Introduce la nueva fecha de nacimiento del entrenador (yyyy-MM-dd, dejar en blanco para no modificar): ");
            String nuevaFechaNacimientoStr = sc.nextLine();

            // Preparación de la query para modificar los datos
            String queryModificacion = "UPDATE Entrenadores SET Nombre = COALESCE(?, Nombre), Apellido_Paterno = COALESCE(?, Apellido_Paterno), Apellido_Materno = COALESCE(?, Apellido_Materno), Fecha_Nacimiento = COALESCE(?, Fecha_Nacimiento) WHERE DNI_Entrenadores = ?";
            PreparedStatement pstmtModificacion = conn.prepareStatement(queryModificacion);

            // Completamos la query con los datos
            pstmtModificacion.setString(1, nuevoNombre.isEmpty() ? null : nuevoNombre);
            pstmtModificacion.setString(2, nuevoApellidoPaterno.isEmpty() ? null : nuevoApellidoPaterno);
            pstmtModificacion.setString(3, nuevoApellidoMaterno.isEmpty() ? null : nuevoApellidoMaterno);
            pstmtModificacion.setString(4, nuevaFechaNacimientoStr.isEmpty() ? null : nuevaFechaNacimientoStr);
            pstmtModificacion.setString(5, dniEntrenador);

            // Realizamos la query y salimos
            pstmtModificacion.executeUpdate();
            conn.commit();
            System.out.println("Datos del entrenador modificados correctamente!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void entrenadorDeClub() {
        try {
            Scanner sc = new Scanner(System.in);

            // Toma de datos del usuario
            System.out.println("ENTRENADOR DE UN CLUB");
            System.out.println("----------------------\n");

            System.out.print("Introduce el nombre del club: ");
            String nombreClub = sc.nextLine();

//            // Verificar si el club existe
//            if (!clubExists(nombreClub)) {
//                System.out.println("El club no existe, saliendo...\n");
//                return;
//            }

            // Query para obtener el entrenador del club
            String query = "SELECT e.DNI_Entrenadores, e.Nombre, e.Apellido_Paterno, e.Apellido_Materno, e.Fecha_Nacimiento " +
                           "FROM Entrenadores e " +
                           "JOIN TIENE_ENTRENADOR t ON e.DNI_Entrenadores = t.DNI_Entrenadores " +
                           "WHERE t.Nombre_Club = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nombreClub);

            // Ejecutar la query
            ResultSet rs = pstmt.executeQuery();

            // Mostrar resultados
            if (rs.next()) {
                System.out.println("\nEntrenador del club " + nombreClub + ":");
                System.out.println("DNI: " + rs.getString("DNI_Entrenadores"));
                System.out.println("Nombre: " + rs.getString("Nombre"));
                System.out.println("Apellido Paterno: " + rs.getString("Apellido_Paterno"));
                System.out.println("Apellido Materno: " + rs.getString("Apellido_Materno"));
                System.out.println("Fecha de Nacimiento: " + rs.getDate("Fecha_Nacimiento"));
            } else {
                System.out.println("\nNo hay un entrenador asociado al club " + nombreClub);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void menuPrincipalEntrenadores() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMENÚ PRINCIPAL - SUBSISTEMA DE ENTRENADORES");
            System.out.println("--------------------------------------------");
            System.out.println("1. Alta de nuevo entrenador");
            System.out.println("2. Baja de entrenador");
            System.out.println("3. Modificar datos de entrenador");
            System.out.println("4. Mostrar listado de entrenadores");
            System.out.println("5. Consultar entrenador de un club en temporada");
            System.out.println("6. Salir");

            System.out.print("\nSeleccione una opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar el buffer del scanner

            switch (opcion) {
                case 1:
                    altaNuevoEntrenador();
                    break;
                case 2:
                    bajaEntrenador();
                    break;
                case 3:
                    modificarDatosEntrenador();
                    break;
                case 4:
                    mostrarListadoEntrenadores();
                    break;
                case 5:
                    entrenadorDeClub();
                    break;
                case 6:
                    System.out.println("Saliendo del subsistema de entrenadores...");
                    return;
                default:
                    System.out.println("Opción no válida. Inténtelo de nuevo.");
            }
        }
    }
}
