import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class tcpmtser implements Runnable {

    public Integer porto;
    public String ipCliente;
    public Integer portoCliente;

    public DataOutputStream saida;
    public DataInputStream entrada;

    public static String ipServidor = "127.0.0.1";

    public long acumulador = 0;
    public String error;

    public Socket socket = null;
    public ServerSocket serverSocket = null;

    public int mensaxe[] = { 0, 0, 0, 0 };

    // Contructor para o fio principal
    public tcpmtser(Integer porto, ServerSocket serverSocket) {

        this.porto = porto;

        try {
            this.socket = serverSocket.accept();

            this.ipCliente = this.socket.getInetAddress().getHostAddress();
            this.portoCliente = this.socket.getPort();

        } catch (IOException e) {
            System.out.println("Non se puido crear o socket para o servidor.");
            System.out.println(e);
        }

    }

    public static void main(String[] args) {

        comprobaArgs(args);

        int porto = Integer.parseInt(args[0]);

        try {
            ServerSocket serverSocket = new ServerSocket(porto);

            while (true) {

                tcpmtser server = new tcpmtser(porto, serverSocket);

                Thread fio = new Thread(server);
                fio.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void run() {
        Thread.currentThread().setName(ipCliente + ":" + portoCliente);
        try {
            entrada = new DataInputStream(this.socket.getInputStream());
            saida = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                byte[] envio;
                byte[] tipo11error;
                byte[] tipo16acumulador;
                

                mensaxe[0] = entrada.readByte();
                mensaxe[1] = entrada.readByte();
                mensaxe[2] = entrada.readByte();
                if (mensaxe[0] != 6) {
                    mensaxe[3] = entrada.readByte();
                }

                this.acumulador = calculadoraAcumulador(this.acumulador);

                if (error != "") {
                    tipo11error = tipo11(this.error);
                    tipo16acumulador = tipo16(this.acumulador);
                    envio = tipo10(tipo11error, tipo16acumulador);

                } else {
                    byte[] tipo16 = tipo16(this.acumulador);

                    envio = tipo10(tipo16);
                }
                // System.out.println(Arrays.toString(envio));

                saida.write(envio);

            }
        } catch (Exception e) {
            //
        }

    }

    public static void comprobaArgs(String[] args) {

        if (args.length != 1) {
            System.out.println("Erro nos argumentos pasados ó servidor.\nA sintaxe válida é a seguinte:");
            System.out.println("\ttcpmser porto");
            System.exit(-1);
        }

    }

    public long calculadoraAcumulador(long acumulador) {
        String idCliente = Thread.currentThread().getName();
        switch (mensaxe[0]) {
            case 1:
                long suma = mensaxe[2] + mensaxe[3];

                System.out.println(idCliente + ": " + mensaxe[2] + " + " + mensaxe[3] + " = " + suma);
                acumulador += suma;
                this.error = "";

                break;
            case 2:
                long resta = mensaxe[2] - mensaxe[3];

                System.out.println(idCliente + ": " + mensaxe[2] + " - " + mensaxe[3] + " = " + resta);
                acumulador += resta;
                this.error = "";

                break;
            case 3:

                long producto = mensaxe[2] * mensaxe[3];

                System.out.println(idCliente + ": " + mensaxe[2] + " * " + mensaxe[3] + " = " + producto);
                acumulador += producto;
                this.error = "";

                break;
            case 4:

                if (mensaxe[3] == 0) {
                    this.error = "Dominio incorrecto.";
                    System.out.println(idCliente + ": Non se puido realizar a operación. " + this.error);
                    return acumulador;
                }

                long cociente = mensaxe[2] / mensaxe[3];
                System.out.println(idCliente + ": " + mensaxe[2] + " / " + mensaxe[3] + " = " + cociente);
                acumulador += cociente;
                this.error = "";

                break;
            case 5:

                if (mensaxe[3] == 0) {
                    this.error = "Dominio incorrecto.";
                    System.out.println(idCliente + ": Non se puido realizar a operación. " + this.error);
                    return acumulador;
                }

                long resto = mensaxe[2] % mensaxe[3];
                System.out.println(mensaxe[2] + " % " + mensaxe[3] + " = " + resto);
                acumulador += resto;
                this.error = "";
                
                break;
            case 6:
                long factorial = 0;

                if (mensaxe[2] > 20) {
                    this.error = "Resultado fora de rango.";
                    System.out.println(idCliente + ": Non se puido realizar a operación. " + this.error);
                    return acumulador;
                }

                factorial = factorial(mensaxe[2]);

                if (factorial == -1) {
                    this.error = "Dominio incorrecto.";
                    System.out.println(idCliente + ": Non se puido realizar a operación. " + this.error);
                    return acumulador;
                }

                System.out.println(mensaxe[2] + "!" + " = " + factorial);
                this.error = "";
                acumulador += factorial;

                break;
        }

        return acumulador;

    }

    public static long factorial(int N1) {

        if (N1 < 0)
            return -1;

        long factorial = 1;

        for (long i = 1; i <= N1; i++) {
            factorial *= i;
        }
        return factorial;
    }

    public byte[] tipo10(byte[] tipo11, byte[] tipo16) {

        byte[] bytes = { 10, (byte) (tipo11.length + tipo16.length) };

        byte[] data = new byte[tipo11.length + tipo16.length];
        System.arraycopy(tipo11, 0, data, 0, tipo11.length);
        System.arraycopy(tipo16, 0, data, tipo11.length, tipo16.length);

        byte[] result = new byte[bytes.length + data.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        System.arraycopy(data, 0, result, bytes.length, data.length);

        return result;
    }

    public byte[] tipo10(byte[] tipo16) {

        byte[] bytes = { 10, (byte) (tipo16.length) };

        byte[] result = new byte[bytes.length + tipo16.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        System.arraycopy(tipo16, 0, result, bytes.length, tipo16.length);

        return result;
    }

    public byte[] tipo11(String error) {

        byte[] bytes = { 11, (byte) (error.length()) };

        try {
            byte[] strBytes = error.getBytes("UTF-8");

            byte[] result = Arrays.copyOf(bytes, bytes.length + strBytes.length);

            System.arraycopy(strBytes, 0, result, bytes.length, strBytes.length);

            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public byte[] tipo16(long acumulador) {

        byte result[] = new byte[] {
                16,
                8,
                (byte) ((acumulador >> 56) & 0xff),
                (byte) ((acumulador >> 48) & 0xff),
                (byte) ((acumulador >> 40) & 0xff),
                (byte) ((acumulador >> 32) & 0xff),
                (byte) ((acumulador >> 24) & 0xff),
                (byte) ((acumulador >> 16) & 0xff),
                (byte) ((acumulador >> 8) & 0xff),
                (byte) ((acumulador >> 0) & 0xff),
        };
        return result;
    }

}
