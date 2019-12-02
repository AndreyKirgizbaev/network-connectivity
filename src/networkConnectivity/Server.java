package networkConnectivity;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

/**
 * проект реализует консольный многопользовательский чат.
 * вход в программу запуска сервера - в классе Server.
 */

class ServerSomething extends Thread {

    private Socket socket; // сокет, через который сервер общается с клиентом
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток завписи в сокет

    /**
     * для общения с клиентом необходим сокет (адресные данные)
     */

    ServerSomething(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String word;
        try {
            // первое сообщение отправленное сюда - это никнейм
            word = decrypting(in.readLine());
            try {
                out.write(encrypting(word + "\n"));
                out.flush(); // flush() нужен для выталкивания оставшихся данных
                // если такие есть, и очистки потока для дьнейших нужд
            } catch (IOException ignored) {
            }
            try {
                while (true) {
                    word = decrypting(in.readLine());
                    System.out.println("Echoing: " + word);
                    Server.story.addStoryEl(word);
                    for (ServerSomething vr : Server.serverList) {
                        vr.send(word); // отослать принятое сообщение с привязанного клиента всем остальным влючая его
                    }
                }
            } catch (NullPointerException ignored) {
            }


        } catch (IOException e) {
            this.downService();
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

    /**
     * отсылка одного сообщения клиенту по указанному потоку
     */
    private void send(String msg) {
        try {
            out.write(encrypting(msg + "\n"));
            out.flush();
        } catch (IOException ignored) {
        }

    }

    /**
     * закрытие сервера
     * прерывание себя как нити и удаление из списка нитей
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (ServerSomething vr : Server.serverList) {
                    if (vr.equals(this)) vr.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {
        }
    }
}

/**
 * класс хранящий в ссылочном приватном
 * списке информацию о последних 10 (или меньше) сообщениях
 */

class Story {

    private LinkedList<String> story = new LinkedList<>();

    /**
     * добавить новый элемент в список
     */
    void addStoryEl(String el) {
        if (story.size() >= 10) {
            story.removeFirst();
            story.add(el);
        } else {
            story.add(el);
        }
    }
}

public class Server {

    private static final int PORT = 8080;
    static LinkedList<ServerSomething> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    static Story story; // история переписки

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            story = new Story();
            System.out.println("Server Started");
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomething(socket)); // добавить новое соединенние в список
                } catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                }
            }
        }
    }
}