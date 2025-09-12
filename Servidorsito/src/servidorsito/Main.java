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
                archivo = new File("C:\\Users\\mango\\Documents\\Servidorsito\\Servidorsito\\src\\servidorsito\\Registros.txt");
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
                        String[] partes = linea.split("_");
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
                        fescritor.write(nombre + "_" + contraseña + "\n");
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
                        String[] partes = linea.split("_");
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
                    escritor.println("Que deseas hacer? (1 = leer mensajes, 2 = dejar mensajes) ");
                    String opcion = lectorSocket.readLine();
                    switch (opcion) {
                        case "si","SI":
                            
                            break;
                        case "no","NO":
                            
                            break;
                        default:
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
    
    private static void dejarMensaje(BufferedReader bufer,String nombre) throws IOException {
        String nombresito = nombre;

        if (true) {
            System.out.println("❌ El usuario \"" + nombre + "\" no está registrado. No puedes dejar mensajes.");
            return;
        }

        System.out.print("Escribe tu mensaje: ");
        String mensaje = bufer.readLine();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\mango\\Documents\\Servidorsito\\Servidorsito\\src\\servidorsito\\Mensajitos.txt", true))) {
            bw.write(nombre + "_" + mensaje);
            bw.newLine();
            System.out.println("✅ Mensaje guardado con éxito.");
        } catch (IOException e) {
            System.out.println("❌ Error al guardar el mensaje: " + e.getMessage());
        }
    }
    

}
