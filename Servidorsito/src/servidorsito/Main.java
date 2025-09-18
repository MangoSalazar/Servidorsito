package servidorsito;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {

        ServerSocket socketEspecialito = null;
        String rutaRegistros = "C:\\Users\\M1-MQ1-D\\Documents\\NetBeansProjects\\Servidorsito\\Servidorsito\\src\\servidorsito\\Registros.txt";
        String rutaMensajitos = "C:\\Users\\mango\\Documents\\Servidorsito\\Servidorsito\\src\\servidorsito\\Mensajitos.txt";
        try {
            socketEspecialito = new ServerSocket(8080);
        } catch (Exception e) {
            System.out.println("Hubro problema en la conexion de red");
            System.exit(1);
        }
        Socket cliente = null;
        try {
            cliente = socketEspecialito.accept();
        } catch (Exception e) {
            System.out.println("Hubro problemas con el cliente");
            System.exit(1);
        }

        PrintWriter escritor = null;
        try {
            escritor = new PrintWriter(cliente.getOutputStream(), true);
            BufferedReader lectorSocket = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

            File archivo = null;
            try {
                archivo = new File(rutaRegistros);
                FileReader lRegistros = new FileReader(archivo);
                BufferedReader lectorRegistros = new BufferedReader(lRegistros);
            } catch (FileNotFoundException e) {
                System.out.println("Hubo un problema con los registros.");
                e.printStackTrace();
            }

            String nombre;
            String contraseña;
            escritor.println("Tienes una sesion? (si/no)");
            String entrada = lectorSocket.readLine();
            boolean tieneSesion = false;
            if (entrada.equalsIgnoreCase("si")) {
                tieneSesion = true;
            }
            escritor.println("Dame tu nombre de usuario");
            nombre = lectorSocket.readLine();
            escritor.println("Dame la contraseña");
            contraseña = lectorSocket.readLine();
            boolean existe = false;
            if (!tieneSesion) {

                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        String[] partes = linea.split(":");
                        if (partes.length > 0 && partes[0].equalsIgnoreCase(nombre)) {
                            existe = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error al leer los registros.");
                    e.printStackTrace();
                }

                if (existe) {
                    escritor.println("El nombre de usuario ya existe.");

                } else {
                    try (FileWriter fescritor = new FileWriter(archivo, true)) {
                        fescritor.write(nombre + ":" + contraseña + "\n");
                        escritor.println("Usuario registrado");
                        System.out.println("Usuario registrado: " + nombre);
                    } catch (IOException e) {
                        System.out.println("Error al guardar el usuario.");
                        e.printStackTrace();
                    }
                }
            }

            if (tieneSesion) {

                try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        String[] partes = linea.split(":");
                        if (partes[1].equalsIgnoreCase(contraseña) && partes[0].equalsIgnoreCase(nombre)) {
                            existe = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error al leer los registros.");
                    e.printStackTrace();
                }
                while (existe) {
                    escritor.println("Que deseas hacer? (1 = leer mensajes, 2 = dejar mensajes, 3 = borrar mensaje) ");
                    String opcion = lectorSocket.readLine();
                    switch (opcion) {
                        case "1":

                            try (BufferedReader br = new BufferedReader(new FileReader(rutaRegistros))) {
                                String linea;
                                boolean hayMensajes = false;

                                while ((linea = br.readLine()) != null) {
                                    String[] partes = linea.split(":", 2);
                                    if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre)) {
                                        hayMensajes = true;
                                        escritor.println("📩 Mensaje: " + partes[1]);
                                        escritor.println("Escribe '1' para ver el siguiente o cualquier otra cosa para salir.");
                                        String respuesta = lectorSocket.readLine();
                                        if (!"1".equalsIgnoreCase(respuesta)) {
                                            break;
                                        }
                                    }
                                }

                                if (!hayMensajes) {
                                    escritor.println("No tienes mensajes nuevos.");
                                }
                            } catch (IOException e) {
                                System.out.println("Error al leer los registros.");
                                e.printStackTrace();
                            }

                            break;
                        case "2":
                            escritor.println("A quien va dirigido el mensaje?");
                            String nombreDestino = lectorSocket.readLine();
                            if (!usuarioExiste(rutaRegistros, nombreDestino)) {
                                escritor.println(" El usuario \"" + nombreDestino + "\" no está registrado. No puedes dejar mensajes. (pulsa enter para continuar)");
                                return;
                            }

                            escritor.println("Escribe tu mensaje: ");
                            String mensaje = lectorSocket.readLine();

                            try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaMensajitos, true))) {
                                bw.write(nombreDestino + ":" + nombre +" => "+ mensaje);
                                bw.newLine();
                                System.out.println("✅ Mensaje guardado con éxito.");
                            } catch (IOException e) {
                                System.out.println(" Error al guardar el mensaje: " + e.getMessage());
                            }

                            break;
                        case "3":
                            
                            
                            break;
                        default:
                            try {
                                cliente.close();
                            } catch (Exception e) {
                                System.out.println("Hubo problemas en la conexion de red");
                                System.exit(1);
                            }
                            throw new AssertionError();
                    }

                }

            }

        } catch (Exception e) {
            System.out.println("Hubo un problema con el lector");
            System.exit(1);
        }
        try {
            cliente.close();
        } catch (Exception e) {
            System.out.println("Hubo problemas en la conexion de red");
            System.exit(1);
        }
    }

    private static boolean usuarioExiste(String ruta, String nombre) {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return false;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println(" Error al verificar usuario: " + e.getMessage());
        }

        return false;
    }
    private static boolean usuarioEncontrado(String nombre, String contraseña) {
        File archivo = new File("C:\\Users\\M1-MQ1-D\\Documents\\NetBeansProjects\\Servidorsito\\Servidorsito\\src\\servidorsito\\Registros.txt");
        if (!archivo.exists()) {
            return false;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2 && partes[0].equalsIgnoreCase(nombre) && partes[1].equalsIgnoreCase(contraseña)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println(" Error al verificar usuario: " + e.getMessage());
        }

        return false;
    }
}
