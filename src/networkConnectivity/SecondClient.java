package networkConnectivity;

public class SecondClient {

    /**
     * создание клиент-соединения с узананными адресом и номером порта
     */
    public static void main(String[] args) {
        String ipAddr = "localhost";
        int port = 8080;
        new ClientSomething(ipAddr, port);
    }
}
