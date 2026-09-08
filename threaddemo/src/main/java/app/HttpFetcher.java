package app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import jackson.databind.ObjectMapper;
import jackson.core.JsonProcessingException;


public class HttpFetcher{
    private static final String  CITY_URL = "https://dawa.aws.dk/steder?hovedtype=Bebyggelse&undertype=by&prim%C3%A6rtnavn=Roskilde";
    private final ObjectMapper objectMapper = new ObjectMapper();
    static void main() {
        String[] cities = {"Roskilde", "Ballerup", "Hornbæk", "Ringkøbing", "Esbjerg"};
        HttpFetcher httpFetcher = new HttpFetcher();
        try(HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()){
            FetchResult result = httpFetcher.fetch(client, CITY_URL);
            long start = System.currentTimeMillis();
            for(String city : cities){
                String url = CITY_URL.replace("$", city);
                FetchResult fetchResult = httpFetcher.fetch(client, url);
            }
            long end = System.currentTimeMillis();
            long runningTime = end-start;
            System.out.println("Time to run" + runningTime);
        }

            
    }
    

   public FetchResult fetch(HttpClient client, String url) {
    
    HttpRequest request = null;
    try{
        request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            long start = System.currentTimeMillis();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
        long end = System.currentTimeMillis();
        if (response.statusCode() != 200) {
            throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
        }
        String body = response.body();
        FetchResult fetchResult = new FetchResult(url, response.statusCode(), body.length(), end-start, Thread.currentThread().getName(), body);

        System.out.println(body);
    

    // 2. Add a request timeout.
    // 3. Send the request.
    // 4. Calculate the request duration.
    // 5. Return a FetchResult.

       } catch (URISyntaxException | InterruptedException | IOException e) { 
        throw new RuntimeException(e);
        }   
    }
}
