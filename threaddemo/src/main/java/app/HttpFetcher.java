package app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HttpFetcher {
    private static final String CITY_URL = "https://dawa.aws.dk/steder?hovedtype=Bebyggelse&undertype=by&prim%C3%A6rtnavn=$";

    private final ObjectMapper objectMapper = new ObjectMapper();

    static void main() {
        String[] cities = {"Roskilde","Ballerup","Hornbæk","Ringkøbing","Esbjerg", "Roskilde","Roskilde","Roskilde","Roskilde","Ballerup"};
        HttpFetcher httpFetcher = new HttpFetcher();
        try(HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()){
            FetchResult result = httpFetcher.fetch(client, CITY_URL);
            long start = System.currentTimeMillis();
            for(String city : cities){
                String url = CITY_URL.replace("$",city);
                FetchResult fetchResult = httpFetcher.fetch(client, url);
                System.out.println(fetchResult.url()+" "+fetchResult.durationMs());
            }
            long end = System.currentTimeMillis();
            long runningTime = end-start;
            System.out.println("Time to run: "+runningTime);
            System.out.println("STARTING WITH THREADS");
            List<Thread> threads = new ArrayList<>();
            start = System.currentTimeMillis();
            for(String city : cities){
                String url = CITY_URL.replace("$",city);
                Runnable task = ()->{
                    FetchResult fetchResult = httpFetcher.fetch(client, url);
                    System.out.println(fetchResult.url()+" "+fetchResult.durationMs());
                };
                Thread thread = new Thread(task);
                threads.add(thread);
                thread.start();
            }
            for(Thread thread : threads){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            end = System.currentTimeMillis();
            runningTime = end-start;
            System.out.println("Time to run with threads: "+runningTime);
        }
    }

    public FetchResult fetch(HttpClient client, String url){
        // 1. Build an HttpRequest for the URL.
        // Create a request
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            long start = System.currentTimeMillis();
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long end = System.currentTimeMillis();
        // Check the status code and print the response
        if (response.statusCode() != 200) {
            throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
        }
        String body = response.body();
        FetchResult fetchResult = new FetchResult(url, response.statusCode(), body.length(), end-start, Thread.currentThread().getName(), body);


//        System.out.println(body);
        // 2. Add a request timeout.
        // 3. Send the request.
        // 4. Calculate the request duration.
        // 5. Return a FetchResult.
            return fetchResult;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}