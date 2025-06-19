package config;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BaseServer {
    public static HttpServer makeserver() throws IOException {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8089);
        System.out.printf("Запускаем сервер по адресу 'http://%s:%s'%n",
                address.getHostName(),
                address.getPort()
        );

        HttpServer server = HttpServer.create(address, 50);
        System.out.println("      удачно!");
        return server;
    }
}
