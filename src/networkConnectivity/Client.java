package networkConnectivity;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * создание клиента со всеми необходимыми утилитами, точка входа в программу в классе Client
 */
class ClientSomething {

    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    private BufferedReader inputUser; // поток чтения с консоли
    private String nickname; // имя клиента

    /**
     * для создания необходимо принять адрес и номер порта
     */
    ClientSomething(String addr, int port) {
        try {
            this.socket = new Socket(addr, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            inputUser = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pressNickname(); // перед началом необходимо спросит имя
            new ReadMsg().start(); // нить читающая сообщения из сокета в бесконечном цикле
            new WriteMsg().start(); // нить пишущая сообщения в сокет приходящие с консоли в бесконечном цикле
        } catch (IOException e) {
            ClientSomething.this.downService();
        }
    }

    /**
     * просьба ввести имя,
     * и отсылка эхо с приветсвием на сервер
     */
    private void pressNickname() {
        System.out.print("Press your nick: ");
        try {
            nickname = inputUser.readLine();
            out.write(encrypting("Hello " + nickname + "\n"));
            out.flush();
        } catch (IOException ignored) {
        }

    }

    /**
     * закрытие сокета
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
    }

    // нить чтения сообщений с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {

            String str;
            try {
                while (true) {
                    str = decrypting(in.readLine()); // ждем сообщения с сервера
                    if (str.equals("stop")) {
                        ClientSomething.this.downService();
                        break; // выходим из цикла если пришло "stop"
                    }
                    System.out.println(str); // пишем сообщение с сервера на консоль
                }
            } catch (IOException e) {
                ClientSomething.this.downService();
            }
        }
    }

    // нить отправляющая сообщения приходящие с консоли на сервер
    public class WriteMsg extends Thread {

        @Override
        public void run() {
            while (true) {
                String userWord;
                try {
                    Date time = new Date();
                    SimpleDateFormat dt1 = new SimpleDateFormat("HH:mm:ss");
                    String dtime = dt1.format(time);
                    userWord = inputUser.readLine(); // сообщения с консоли
                    if (userWord.equals("stop")) {
                        out.write(encrypting("stop" + "\n"));
                        ClientSomething.this.downService();
                        break; // выходим из цикла если пришло "stop"
                    } else {
                        String message = "(" + dtime + ") " + nickname + ": " + userWord + "\n";
                        out.write(encrypting(message)); // отправляем на сервер
                    }
                    out.flush(); // чистим
                } catch (IOException e) {
                    ClientSomething.this.downService();

                }

            }
        }
    }

    private String encrypting(String message) {
        int innerKey = 3;
        StringBuilder charBox = new StringBuilder();
        for (char tmpChar : message.toLowerCase().toCharArray())
            charBox.append((char) (tmpChar + innerKey));
        return charBox.toString();
    }

    private String decrypting(String message) {
        int innerKey = 3;
        innerKey = -innerKey;
        StringBuilder charBox = new StringBuilder();
        for (char tmpChar : message.toLowerCase().toCharArray())
            charBox.append((char) (tmpChar + innerKey));
        return charBox.toString();
    }
}

public class Client {

    /**
     * создание клиент-соединения с узананными адресом и номером порта
     */
    public static void main(String[] args) {
        String ipAddr = "localhost";
        int port = 8080;
        new ClientSomething(ipAddr, port);
    }
}



