package servidorsito;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final String RUTA_BASE = "C:\\Users\\M1-MQ1-D\\Documents\\NetBeansProjects\\Servidorsito\\Servidorsito\\src\\servidorsito\\";
    private static final String RUTA_REGISTROS = RUTA_BASE + "Registros.txt";
    private static final String RUTA_MENSAJES = RUTA_BASE + "Mensajitos.txt";
    private static final String RUTA_TEMP = RUTA_BASE + "temp_registros.txt";

    public static void main(String[] args) throws IOException {
        try (ServerSocket servidor = new ServerSocket(8080)) {
            System.out.println("Servidor esperando conexión...");
            Socket cliente = servidor.accept();

            try (PrintWriter escritor = new PrintWriter(cliente.getOutputStream(), true); BufferedReader lectorSocket = new BufferedReader(new InputStreamReader(cliente.getInputStream()))) {

                escritor.println("Bienvenido Dame tu nombre de usuario");
                String nombre = lectorSocket.readLine();
                escritor.println("Dame la contraseña:");
                String contraseña = lectorSocket.readLine();

                if (!usuarioExiste(nombre)) {
                    escritor.println("el usuario no existe, Deseas registrartlo? (si/no)");
                    if ((lectorSocket.readLine()).equalsIgnoreCase("si")) {
                        registrarUsuario(nombre, contraseña);
                        escritor.println("✅ Usuario registrado con éxito.");
                    }
                }

                if (validarUsuario(nombre, contraseña)) {
                    boolean activo = true;
                    while (activo) {
                        escritor.println("¿Qué deseas hacer? (1 = leer mensajes, 2 = dejar mensajes, 3 = borrar cuenta, 4 = salir)");
                        String opcion = lectorSocket.readLine();

                        switch (opcion) {
                            case "1":
                                leerMensajes(nombre, escritor, lectorSocket);
                                break;
                            case "2":
                                escritor.println("A quién va dirigido el mensaje?");
                                String destino = lectorSocket.readLine();
                                if (!usuarioExiste(destino)) {
                                    escritor.println("️ El usuario no está registrado.");
                                    break;
                                }
                                escritor.println("Escribe tu mensaje:");
                                String mensaje = lectorSocket.readLine();
                                guardarMensaje(destino, nombre, mensaje);
                                escritor.println(" Mensaje guardado con éxito.");
                                break;
                            case "3":
                                escritor.println("Seguro que quieres borrar tu cuenta? (si/no)");
                                if (lectorSocket.readLine().equalsIgnoreCase("si")) {
                                    if (borrarUsuario(nombre, contraseña)) {
                                        escritor.println(" Cuenta eliminada.");
                                        activo = false; // salir del loop
                                    } else {
                                        escritor.println(" Error al eliminar la cuenta.");
                                    }
                                }
                                break;
                            case "4":
                                escritor.println(" Cerrando sesión...");
                                activo = false;
                                break;
                            default:
                                //Añadir el mensaje dirigido con un solo estado
                                if (opcion.startsWith("@")) {
                                    mandarMensaje(opcion , nombre);
                                }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error en el servidor: " + e.getMessage());
            }
        }
    }

    private static String obtenerUsuario(String mensaje) {
        String nombre = "";
        if (mensaje.contains(" ")) {
            nombre = mensaje.substring(1, mensaje.indexOf(" "));
        }
        return nombre;
    }
    
    private static void mandarMensaje(String mensaje, String origen){
        if (usuarioExiste(obtenerUsuario(mensaje))) {
            
        } 
    }

    private static boolean usuarioExiste(String nombre) {
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_REGISTROS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static void registrarUsuario(String nombre, String contraseña) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_REGISTROS, true))) {
            bw.write(nombre + ":" + contraseña);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
        }
    }

    private static boolean validarUsuario(String nombre, String contraseña) {
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_REGISTROS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre) && partes[1].equalsIgnoreCase(contraseña)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private static boolean borrarUsuario(String nombre, String contraseña) {
        File original = new File(RUTA_REGISTROS);
        File temp = new File(RUTA_TEMP);
        boolean eliminado = false;
        try (BufferedReader br = new BufferedReader(new FileReader(original)); BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre) && partes[1].equalsIgnoreCase(contraseña)) {
                    eliminado = true; // saltamos esta línea
                } else {
                    bw.write(linea);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            return false;
        }
        return eliminado && original.delete() && temp.renameTo(original);
    }

    private static void leerMensajes(String nombre, PrintWriter escritor, BufferedReader lectorSocket) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_MENSAJES))) {
            String linea;
            boolean hayMensajes = false;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre)) {
                    hayMensajes = true;
                    escritor.println(" Mensaje: " + partes[1]);
                    escritor.println("Escribe '1' para ver el siguiente o cualquier otra cosa para salir.");
                    if (!"1".equalsIgnoreCase(lectorSocket.readLine())) {
                        break;
                    }
                }
                if (!hayMensajes) {
                    escritor.println("No tienes mensajes nuevos.");
                }
            }
        }
    }

    private static void guardarMensaje(String destino, String origen, String mensaje) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_MENSAJES, true))) {
            bw.write(destino + ":" + origen + " => " + mensaje);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar mensaje: " + e.getMessage());
        }
    }
}
