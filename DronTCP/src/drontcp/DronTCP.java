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

    public static String md5(String text){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DronTCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        byte[] textbytes = md.digest(text.getBytes());
        md.update(textbytes);
        byte[] hashMd5 = md.digest();

        StringBuilder sb = new StringBuilder();
        
        for (byte b : hashMd5) {
            sb.append(String.format("%02x", b));
        }
        String hashMd5Texto = sb.toString();
        
        return hashMd5Texto;
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
                                    bufferOut.println("INITResp OK");
                                    bufferOut.flush();
                                    estado = Esperandologin;
                                    LeerMensaje = false;
                                } else {
                                    bufferOut.println("INITResp Error");
                                    bufferOut.flush();
                                    estado = EstadoInicial;
                                }
                                break;

                            case Esperandologin:

                                System.out.println("Server: Autenticacion recibida");
                                String[] vector_login = linea.split("[  ]");

                                
                                if (vector_login.length == 3) {

                                    byte[] ByteHash = Base64.getDecoder().decode(vector_login[2]);
                                    String hash_client = new String(ByteHash);
                                    System.out.println("Server: Hash codificado: " + vector_login[2]);
                                    
                                    String hash_server = md5(Mensajes.password);
                                    System.out.println("Server: Hash decodificado: " + hash_server);
                                    
                                    if ((vector_login[1].equals(Mensajes.user)) && (hash_client.equals(hash_server))) {
                                        bufferOut.println("LOGINResp OK");
                                        bufferOut.flush();
                                        LeerMensaje = true;
                                        estado = EsperandoOrdenes;
                                        System.out.println("Server: Autenticacion correcta");
                                    } else {
                                        bufferOut.println("LOGINResp WrongPass");
                                        bufferOut.flush();
                                        LeerMensaje = true;
                                        estado = EstadoInicial;
                                        System.out.println("Server: Autenticacion erronea");
                                    }
                                } else {
                                    bufferOut.println("LOGINResp BadFormat");
                                    bufferOut.flush();
                                    LeerMensaje = true;
                                    estado = EstadoInicial;
                                    System.out.println("Server: Autenticacion BadFormat");

                                }

                                break;

                            case EsperandoOrdenes:

                                if (linea.startsWith(Mensajes.EncM)) {
                                    bufferOut.println("ORDENResp OK ");
                                    bufferOut.flush();
                                    estado = EsperandoOrdenes;
                                    LeerMensaje = true;
                                } else 
                                    if (linea.startsWith(Mensajes.apagar)) {
                                        bufferOut.println("ORDENResp OK");
                                        bufferOut.flush();
                                        estado = EsperandoOrdenes;
                                        LeerMensaje = false;
                                        finConexion = true;
                                    } else {
                                        bufferOut.println("ORDENResp NOK");
                                        bufferOut.flush();
                                        estado = EsperandoOrdenes;
                                        LeerMensaje = true;
                                    }

                                break;

                        }

                        }else {
                        System.out.println("Server: error al leer mensaje: ¿conexión interrumpida?");
                        finConexion = true;
                    }
                    }
                    System.out.println("Server: Cerrando conexión ... ");
                    bufferOut.println("BYE");
                    bufferOut.flush();
                    socket.close();
                }

            }catch (IOException ex) {
            System.out.println("Server: No se ha podido abrir el servidor en el puerto " + puerto);
        }

        }
    }
