import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class tcpmtcli {

    public InetAddress ipServer;
    public Integer portoServer;
    public Integer porto;

    public static DataOutputStream saida;
    public static DataInputStream entrada;

    public static String[] msg = { "" };
    public static String charset = "latin1";
    public static Integer tipo, N1, N2, lonx;

    public static String msgError = "Non se puido interpretar a operacion. Proba outra vez";

    public tcpmtcli(String[] args) {
        comprobaArgs(args);

        try {

            this.ipServer = InetAddress.getByName(args[0]);
            this.portoServer = Integer.parseInt(args[1]);

        } catch (UnknownHostException e) {

            System.out.println("Erro na dirección do servidor, revise os argumentos introducidos");

        }
    }

    public static void main(String[] args) {

        tcpmtcli cliente = new tcpmtcli(args);
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(cliente.ipServer, cliente.portoServer), 15 * 1000);

            cliente.porto = socket.getLocalPort();

            saida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());

            int flag = 0;

            System.out.println("\nEscriba a operacion a enviar o servidor.");
            System.out.println("Separe mediante espacios en branco os operandos e a operacion.");
            System.out.println("As operacions soportadas son: +, -, /, *, % e !.\nPor exemplo: 5 ! ou -5 + 5.");
            System.out.println("Envie 'QUIT' para Sair");
            while (true) {

                flag = leeTeclado();

                if (flag == -1) {
                    System.out.println(msgError);
                } else {

                    if (flag == 1) {
                        tipo = 0;
                    } else
                        comprobaTipo();

                    if ((msg.length == 2 && tipo != 6) || tipo == -1) {
                        System.out.println(msgError);
                    } else if ((msg.length == 3 && tipo == 6)) {
                        System.out.println(msgError);
                    } else {
                        if (tipo == 6) {
                            N1 = Integer.parseInt(msg[0]);
                            lonx = 1;
                            N2 = 0;
                        } else if (tipo == 0) {
                            N1 = 0;
                            N2 = 0;
                            lonx = 0;
                            break;
                        } else {
                            N1 = Integer.parseInt(msg[0]);
                            lonx = 2;
                            N2 = Integer.parseInt(msg[2]);
                        }
                        if (comprobaNums() == -1) {
                            System.out.println(msgError);
                        } else {
                            byte[] envio = numerosTLV();

                            saida.write(envio);

                            int tipo10 = entrada.read();
                            int lonxTotal = entrada.read();
                            int tipo = entrada.read();
                            int lonxStr = entrada.read();
                            if (tipo == 16) {
                                byte[] datos = new byte[8];
                                entrada.readFully(datos);
                                //System.out.println(Arrays.toString(datos));
                                long longRecibido = ((datos[0] & 0xFFL) << 56) |
                                        ((datos[1] & 0xFFL) << 48) |
                                        ((datos[2] & 0xFFL) << 40) |
                                        ((datos[3] & 0xFFL) << 32) |
                                        ((datos[4] & 0xFFL) << 24) |
                                        ((datos[5] & 0xFFL) << 16) |
                                        ((datos[6] & 0xFFL) << 8) |
                                        ((datos[7] & 0xFFL) << 0);

                                System.out.println("Valor do acumulador: " + longRecibido);
                            } else {
                                byte[] datos = new byte[lonxTotal-2];
                                entrada.readFully(datos);
                                
                                byte[] stringBytes = Arrays.copyOfRange(datos, 0, lonxStr);
                                String error = new String(stringBytes, "UTF-8");

                                long longRecibido = ((datos[lonxStr + 2] & 0xFFL) << 56) |
                                        ((datos[lonxStr + 3] & 0xFFL) << 48) |
                                        ((datos[lonxStr + 4] & 0xFFL) << 40) |
                                        ((datos[lonxStr + 5] & 0xFFL) << 32) |
                                        ((datos[lonxStr + 6] & 0xFFL) << 24) |
                                        ((datos[lonxStr + 7] & 0xFFL) << 16) |
                                        ((datos[lonxStr + 8] & 0xFFL) << 8) |
                                        ((datos[lonxStr + 9] & 0xFFL) << 0);

                                System.out.println("Valor do acumulador: " + longRecibido + ". Error: " + error);

                            }

                        }
                    }
                }

            }

            socket.close();

        } catch (SocketTimeoutException e) {
            System.out.println("Excedeuse o tempo de espera de 15 segundos para establecer a conexion co servidor.");
        } catch (IOException e) {
            System.out.println("Conexion rexeitada, revise a direccion e o porto do servidor.");
        }

        return;
    }

    public static int leeTeclado() {

        String mensaxe = "";
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

        try {

            mensaxe = teclado.readLine();

            msg = mensaxe.split("\\s");

            if (mensaxe.equals("QUIT")) {
                return 1;
            }

            if (msg.length < 2 || msg.length > 3) {
                return -1;
            } else {
                msg[1] = msg[1].trim();
            }

        } catch (IOException e) {

            System.out.println("Erro na lectura.");
            System.exit(-1);

        }

        return 0;
    }

    public static void comprobaTipo() {

        switch (msg[1]) {
            case "+":
                tipo = 1;
                break;
            case "-":
                tipo = 2;
                break;
            case "*":
                tipo = 3;
                break;
            case "/":
                tipo = 4;
                break;
            case "%":
                tipo = 5;
                break;
            case "!":
                tipo = 6;
                break;
            default:
                tipo = -1;
                break;
        }

        return;
    }

    public static int comprobaNums() {

        if (N1 > 127 || N1 < -127) {
            return -1;
        } else if (N2 > 127 || N2 < -127) {
            return -1;
        } else {
            if (tipo == 6) {
                N2 = 0;
            }
            return 0;
        }

    }

    public void comprobaArgs(String[] args) {

        if (args.length != 2) {
            System.out.println("Erro nos argumentos pasados ó cliente.\nA sintaxe válida é a seguinte:");
            System.out.println("\ttcp1cli ip_servidor porto");
            System.exit(-1);
        }

    }

    public static byte[] numerosTLV() {

        if (tipo == 6) {
            byte bytes[] = { tipo.byteValue(), lonx.byteValue(), N1.byteValue() };
            return bytes;
        } else {
            byte bytes[] = { tipo.byteValue(), lonx.byteValue(), N1.byteValue(), N2.byteValue() };
            return bytes;
        }

    }

}
