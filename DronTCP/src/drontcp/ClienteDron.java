
package drontcp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pablo Robles Fuentes, Juan Muñoz Villalon
 */
public class ClienteDron {
    
    static final int anfitrionNoEncontrado=1;
    static final int imposibleEstablecerConexion=2;
    
    // Declaramos el socket:
    static Socket socket;
    
    static PrintWriter clientOut = null;
    static BufferedReader clientIn = null;
    
    @SuppressWarnings("empty-statement")
    public static void main(String[] args){
        int puerto = 9999;
        String DirServer = "127.0.0.1";
        
        Scanner conin;
        conin = new Scanner(System.in);
        
        // Por si quisieramos pasarle el puerto y la dirección por la consola;
        if (args.length == 2){
            DirServer = args[0];
            puerto = Integer.parseInt(args[1]);
        }
        
        int initCon = IniciarConexion(DirServer,puerto);
        
        if(initCon==0){
            
            System.out.println("Client: Conexión Establecida");
            
            while(Autenticacion(conin)!=0){
                     System.out.println("Client: Credenciales incorrectas");
            };
            System.out.println("Client: Autenticación correcta");
            
            while(EnviarOrden(conin)==0){
            }
            System.out.println("Client: Fin de Conexion");
            
        }else{
            if (initCon==1)
                System.out.println("Client: Server no encontrado");
            else
                System.out.println("Client: Imposible establecer conexion");
        }
        
        
        
    }
    
    static int IniciarConexion(String DirServer, int puerto){
        int error=0;
        
        
        // Creamos el socket y establecemos la conexio'n TCP con el servidor:
        try {            
            socket = new Socket(DirServer, puerto);
            
            // Abrimos los flujos de envi'o/recepcio'n:
            clientOut = new PrintWriter(socket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
        } catch (UnknownHostException e) {
                return anfitrionNoEncontrado;               
        } catch (IOException e) {
                return imposibleEstablecerConexion;
        }
        
        return error;
        
        
    }
    
    static String recibirRespuesta() {
     
        String mensaje;
        
        // Como sabesmo que solo va a venir un mensaje, solicitamos caracteres hasta '\n':
        
        try{
            mensaje=clientIn.readLine();
        } catch (IOException e){
            return null;
        }
        
        return mensaje;
    }
    
   
    
    static int  Autenticacion(Scanner conin){
        
        int error=0;
        String Resp="", usuario="", pass="";
        String credenciales[];
        
        System.out.println("Introduce usuario y contraseña separadas por un espacio: ");
       
        credenciales = conin.nextLine().split("[ ]");
        pass = credenciales[1];
        usuario = credenciales[0];
        
        String hash = DronTCP.md5(pass);
        byte[] hashBytes = hash.getBytes();
        
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] hashBytesCod = encoder.encode(hashBytes);
        
        String hashCodificado = new String(hashBytesCod);
        
        clientOut.println("HI " + usuario + " " + hashCodificado);
        clientOut.flush();
        
        
        Resp = recibirRespuesta();
        System.out.println("Server says: "+ Resp);
        if(Resp.equals("INITResp OK")){
            Resp = recibirRespuesta();
            System.out.println("Server says: "+ Resp);
            if (Resp.equals("LOGINResp OK"))
                error=0;
            else 
                error=1;
        }
        else{
            error=1;
        }
        
        return error;
    }
    
    static int EnviarOrden(Scanner conin){
        int fin=0;
        String orden;
        String Resp ="";
        
        System.out.println("Introduce Orden: \nEncender Motores --> E\nApagar --> R");
        
        orden = conin.nextLine();
        
        switch (orden) {
            case "E":
                clientOut.println("EncenderMotores");
                break;
            case "R":
                clientOut.println("Apagar");
                fin=1;
                break;
            default:
                System.out.println("Orden no valida");
                break;
        }
        
        Resp = recibirRespuesta();
        
        System.out.println("Server says: "+ Resp);
        
        return fin;
    }
}


