package drontcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan
 */
public class DronTCP {

    static final int EstadoInicial = 0;
    static final int EsperandoOrdenes = 1;
    static final int Esperandologin = 2;

    public static void main(String[] args) {
        new DronTCP(9999);
    }

    public static String md5(String clear) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] b = md.digest(clear.getBytes());
        int size = b.length;
        StringBuffer h = new StringBuffer(size);

        //algoritmo y arreglo md5
        for (int i = 0; i < size; i++) {
            int u = b[i] & 255;
            if (u < 16) {
                h.append("0" + Integer.toHexString(u));
            } else {
                h.append(Integer.toHexString(u));
            }
        }
        return h.toString();
    }

    private DronTCP(int puerto) {
        try {
            // Abrimos un socket de servidor para escuchar en un puerto.
            ServerSocket serverSocket = new ServerSocket(puerto);

            boolean salir = false;
            while (!salir) {

                // Esperamos una solicitud de conexión TCP de un cliente:
                Socket socket = serverSocket.accept();

                // Obtenemos un objeto para entrada y otro de salida:
                BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter bufferOut = new PrintWriter(socket.getOutputStream());

                bufferOut.println("Conexión Establecida");
                bufferOut.flush();

                // Variables
                int estado = EstadoInicial;
                boolean finConexion = false;
                boolean LeerMensaje = true;
                String linea = "";

                while (!finConexion) {

             

                    if (LeerMensaje) {
                        linea = bufferIn.readLine();
                    }

                    if (linea != null) {

                        switch (estado) {

                            case EstadoInicial:
                                if (linea.startsWith(Mensajes.login)) {
                                    bufferOut.println("INIT OK");
                                    bufferOut.flush();
                                    estado = Esperandologin;
                                    LeerMensaje = false;
                                }
                                else{
                                    bufferOut.println("INIT Error");
                                    bufferOut.flush();
                                    estado = EstadoInicial;
                                }
                                break;

                            case Esperandologin:

                                System.out.println("Server: Autenticacion recibida");
                                String[] vector_login = linea.split("[  ]");
                                    
                                System.out.println(vector_login[0]);
                                if (vector_login.length == 3) {

                                    byte[] ByteHash = Base64.getDecoder().decode(vector_login[2]);
                                    String hash_client = new String(ByteHash);

                                    //String hash_server = md5(Mensajes.password);
                                    
                                    if ((vector_login[1].equals(Mensajes.user)) && (vector_login[2].equals(Mensajes.password))) {
                                        bufferOut.println("LOGIN OK. Hola " + vector_login[1]);
                                        bufferOut.flush();
                                        LeerMensaje=true;
                                        estado = EsperandoOrdenes;
                                    } else {
                                        bufferOut.println("LOGIN WrongPass");
                                        bufferOut.println("TryAgain");
                                        bufferOut.flush();
                                        LeerMensaje=true;
                                        estado = EstadoInicial;
                                    }
                                } else {
                                    bufferOut.println("LOGIN BadFormat");
                                    bufferOut.println("TryAgain");
                                    bufferOut.flush();
                                    LeerMensaje=true;
                                    estado = EstadoInicial;

                                }

                                break;

                            case EsperandoOrdenes:
                                
                                

                        }

                    } else {
                        System.out.println("Server: error al leer mensaje: ¿conexión interrumpida?");
                        finConexion = true;
                    }
                }
                socket.close();
            }

        } catch (IOException ex) {
            System.out.println("Server: No se ha podido abrir el servidor en el puerto " + puerto);
        }

    }
}
