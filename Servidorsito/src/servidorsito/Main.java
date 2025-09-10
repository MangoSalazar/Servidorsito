
package servidorsito;

import java.io.BufferedReader;
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
 
            //BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
 
            String entrada;
            String mensajito = "";
            int numerito = (int)(10*(Math.random()));
            System.out.println(numerito);
            int intentos = 0;
            boolean adivino = false;
 
            while ((entrada= lectorSocket.readLine()) != null && intentos < 3) {
                //System.out.println(entrada.toUpperCase());
                //mensajito= teclado.readLine();
 
                if(Integer.valueOf(entrada) > numerito){
                    escritor.println("te pasaste");
                    intentos++;
                }
                if (Integer.valueOf(entrada) < numerito) {
                    escritor.println("te falto");
                    intentos++;
                }
                if (Integer.valueOf(entrada) == numerito) {
                    escritor.println("Acertaste pa"+"\nQuieres jugar de nuevo?");
                    adivino = true;
                }
 
            }
            if (adivino = false) {
                escritor.println("El numero era: " + numerito);
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
