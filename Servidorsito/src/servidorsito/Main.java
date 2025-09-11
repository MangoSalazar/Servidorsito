
package servidorsito;

import java.io.BufferedReader;
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
            escritor = new PrintWriter(cliente.getOutputStream(),true);
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
            
            if (entrada.equals("no") || (entrada.toUpperCase()).equals("NO")) {
                escritor.println("Dame tu nombre de usuario");
                nombre = lectorSocket.readLine();
                escritor.println("Dame la contraseña");
                contraseña = lectorSocket.readLine();
                
                boolean existe = false;

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
            
            if (entrada.equals("si") || (entrada.toUpperCase()).equals("SI")) {                
                int numerito = (int) (Math.random() * 10);
                System.out.println("Numero secreto: " + numerito); 
                int intentos = 0;
                boolean adivino = false;

                while ((entrada = lectorSocket.readLine()) != null && intentos < 3) {

                    int numero;
                    try {
                        numero = Integer.parseInt(entrada);
                    } catch (NumberFormatException e) {
                        escritor.println("Eso no es un número válido.");
                        continue;
                    }

                    if (numero > numerito) {
                        escritor.println("Te pasaste");
                        intentos++;
                    } else if (numero < numerito) {
                        escritor.println("Te faltó");
                        intentos++;
                    } else {
                        escritor.println("Acertaste pa! ¿Quieres jugar de nuevo?");
                        adivino = true;
                        break;
                    }
                }
                if (!adivino) {
                    escritor.println("El número era: " + numerito);
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
}
