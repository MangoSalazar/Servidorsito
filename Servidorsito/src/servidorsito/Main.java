package servidorsito;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private static final String RUTA_BASE = "C:\\Users\\mango\\Documents\\Servidorsito\\Servidorsito\\src\\servidorsito\\";
    private static final String RUTA_REGISTROS = RUTA_BASE + "Registros.txt";
    private static final String RUTA_MENSAJES = RUTA_BASE + "Mensajitos.txt";
    private static final String RUTA_TEMP = RUTA_BASE + "temp_registros.txt";
    private static final String RUTA_BLOQUEOS = RUTA_BASE + "Bloqueos.txt";

    public static void main(String[] args) throws IOException {
        try (ServerSocket servidor = new ServerSocket(8080)) {
            System.out.println("Servidor esperando conexión...");
            Socket cliente = servidor.accept();

            try (PrintWriter escritor = new PrintWriter(cliente.getOutputStream(), true); BufferedReader lectorSocket = new BufferedReader(new InputStreamReader(cliente.getInputStream()))) {

                escritor.println("Bienvenido Dame tu nombre de usuario");
                String nombre = lectorSocket.readLine();
                escritor.println("Dame la contraseña:");
                String contraseña = lectorSocket.readLine();

                if (!usuarioExiste(nombre,RUTA_REGISTROS)) {
                    escritor.println("el usuario no existe, Deseas registrartlo? (si/no)");
                    if ((lectorSocket.readLine()).equalsIgnoreCase("si")) {
                        registrarUsuario(nombre, contraseña,RUTA_REGISTROS);
                        escritor.println("Usuario registrado con éxito.");
                    }
                }
                if (validarUsuario(nombre, contraseña)) {
                    boolean cuentaActiva = true;
                    while (cuentaActiva) {
                        escritor.println("¿Qué deseas hacer? (1 = leer mensajes, 2 = ver usuarios, 3 = borrar cuenta, 4 = salir)");
                        String opcion = lectorSocket.readLine();

                        switch (opcion) {
                            case "1":
                                leerMensajes(nombre, escritor, lectorSocket,RUTA_MENSAJES);
                                break;
                            case "2":
                                escritor.println("Usa @(usuario) (mensaje) para mandar un mensaje o "
                                        + "#(usuario) para bloquearlo. "
                                        + imprimirUsuarios());
                                break;
                            case "3":
                                escritor.println("Seguro que quieres borrar tu cuenta? (si/no)");
                                if (lectorSocket.readLine().equalsIgnoreCase("si")) {
                                    if (estaEliminado(nombre, contraseña)) {
                                        escritor.println(" Cuenta eliminada.");
                                        cuentaActiva = false;
                                    } else {
                                        escritor.println(" Error al eliminar la cuenta.");
                                    }
                                }
                                break;
                            case "4":
                                escritor.println(" Cerrando sesión...");
                                cuentaActiva = false;
                                break;
                            default:
                                if (opcion.startsWith("@")) {
                                    mandarMensaje(opcion, nombre);
                                }
                                if (opcion.startsWith("#")) {
                                    bloquearUsuario(nombre,opcion);
                                }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error en el servidor: " + e.getMessage());
            }
        }
    }
    
    private static boolean estaBloqueado(String usuario){
        if (usuarioExiste(usuario, RUTA_BLOQUEOS)) {
            return true;
        }
        return false;
    }
    
    private static void bloquearUsuario(String nombre,String usuarioB) {
        if (usuarioExiste(obtenerUsuario(usuarioB),RUTA_REGISTROS) && !estaBloqueado(obtenerUsuario(usuarioB))) {
            registrarUsuario(obtenerUsuario(usuarioB), nombre, RUTA_BLOQUEOS);
        }
    }

    private static String imprimirUsuarios() {
        String linea;
        String nombres = "";
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_REGISTROS))) {
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                nombres = nombres + partes[0] + ", ";
            }
            return nombres;
        } catch (IOException ignored) {
        }
        return nombres;
    }

    private static String obtenerUsuario(String mensaje) {
        String nombre = "";
        String[] partes = mensaje.split(" ");
        nombre = partes[0].substring(1);
        return nombre;
    }

    private static void mandarMensaje(String mensaje, String origen) {
        String destino = obtenerUsuario(mensaje);
        if (usuarioExiste(destino,RUTA_REGISTROS) && !estaBloqueado(destino)) {
            guardarMensaje(destino, origen, arreglarMensaje(mensaje),RUTA_MENSAJES);
            return;
        }
        
    }

    private static String arreglarMensaje(String mensaje) {
        String mensajeArreglado = mensaje.substring(mensaje.indexOf(" ") + 1, mensaje.length());
        return mensajeArreglado;
    }

    private static boolean usuarioExiste(String nombre, String rutaFinal) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaFinal))) {
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

    private static void registrarUsuario(String nombre, String contraseña,String rutaFinal) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaFinal, true))) {
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

    private static boolean estaEliminado(String nombre, String contraseña) {
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

    private static void leerMensajes(String nombre, PrintWriter escritor, BufferedReader lectorSocket,String rutaFinal) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaFinal))) {
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

    private static void guardarMensaje(String destino, String origen, String mensaje,String rutaFinal) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaFinal, true))) {
            bw.write(destino + ":" + origen + " => " + mensaje);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar mensaje: " + e.getMessage());
        }
    }
}
