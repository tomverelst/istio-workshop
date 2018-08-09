package be.ordina.istio.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.sun.net.httpserver.HttpServer;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class WorkServer {
    
    static final String[] TRACE_HEADERS = new String[] {"x-request-id", "x-b3-traceid", "x-b3-spanid", "x-b3-parentspanid", "x-b3-sampled", "x-b3-flags", "x-ot-span-context"};
    
    public static void main(String[] args) throws IOException  {
        String endpoint = System.getenv().getOrDefault("ENDPOINT", "http://localhost:8081/meet");
       
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        
        String hostName = InetAddress.getLocalHost().getHostName();
    
        final HttpClient httpClient = HttpClient.newHttpClient();
        
        server.createContext("/work", exchange -> {
            long start = System.currentTimeMillis();
            
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint)).GET();
    
            copyTraceHeaders(exchange, builder);
    
            HttpRequest request = builder.build();
        
            int meetings = 0;
            for (int i = 0; i < 4; i++) {
                try {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandler.asString());
                    if (response.statusCode() == 200) {
                        meetings++;
                    }
                } catch (InterruptedException e) {
                }
            }
        
            long end = System.currentTimeMillis();
        
            final String response = "Worked for " + (end - start) + "ms, attended " + meetings + " meetings at " + hostName + "\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
        
            os.write(bytes);
            os.close();
        });
    
        server.start();
    }
    
    private static void copyTraceHeaders(HttpExchange exchange, HttpRequest.Builder builder) {
        Arrays.stream(TRACE_HEADERS).forEach(header -> {
            String value = exchange.getRequestHeaders().getFirst(header);
            if (value != null) {
                builder.setHeader(header, value);
            }
        });
    }
    
}
