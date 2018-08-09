package be.ordina.istio.jdk;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MeetingServer {
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.setExecutor(Executors.newCachedThreadPool());
    
        server.createContext("/meet", exchange -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        });
    
        server.start();
    }
}
