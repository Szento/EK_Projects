package app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class HttpFetcher {
    private static final String ALL_CITIES_URL =
            "https://dawa.aws.dk/steder?hovedtype=Bebyggelse&undertype=by";
    private static final String CITY_URL =
            "https://dawa.aws.dk/steder?hovedtype=Bebyggelse&undertype=by&prim%C3%A6rtnavn=$";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
    HttpFetcher fetcher = new HttpFetcher();
    int cores = Runtime.getRuntime().availableProcessors();
    System.out.println("Available cores: " + cores);

    try (HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build()) {

        List<String> cityNames = fetcher.fetchAllCityNames(client);
        System.out.println("Got " + cityNames.size() + " unique cities");

        // ---- Phase 1: fetch once, politely, cache bodies locally ----
        Semaphore fetchLimiter = new Semaphore(10); // be gentle to DAWA
        List<String> bodies = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService fetchPool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (String city : cityNames) {
                futures.add(fetchPool.submit(() -> {
                    try {
                        fetchLimiter.acquire();
                        String url = CITY_URL.replace("$", URLEncoder.encode(city, StandardCharsets.UTF_8));
                        bodies.add(fetcher.fetch(client, url).content());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        fetchLimiter.release();
                    }
                }));
            }
            for (Future<?> f : futures) f.get();
        }
        System.out.println("Cached " + bodies.size() + " response bodies");

        // ---- Phase 2: pure CPU stress test, no network involved ----
        long start = System.currentTimeMillis();
        try (ExecutorService crunchPool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (String body : bodies) {
                futures.add(crunchPool.submit(() -> fetcher.crunch(body)));
            }
            for (Future<?> f : futures) f.get();
        }
        System.out.println("Virtual thread CPU crunch: " + (System.currentTimeMillis() - start) + " ms");

        // Now compare against a fixed platform-thread pool sized to cores
        start = System.currentTimeMillis();
        try (ExecutorService fixedPool = Executors.newFixedThreadPool(cores)) {
            List<Future<?>> futures = new ArrayList<>();
            for (String body : bodies) {
                futures.add(fixedPool.submit(() -> fetcher.crunch(body)));
            }
            for (Future<?> f : futures) f.get();
        }
        System.out.println("Fixed platform pool (cores=" + cores + ") CPU crunch: "
                + (System.currentTimeMillis() - start) + " ms");
    }
}

    public List<String> fetchAllCityNames(HttpClient client) throws Exception {
        FetchResult result = fetch(client, ALL_CITIES_URL);
        JsonNode root = objectMapper.readTree(result.content());
        List<String> names = new ArrayList<>();
        for (JsonNode node : root) {
            names.add(node.get("primærtnavn").asText());
        }
        return names;
    }

    // Fetch + do real CPU work on the response so the cores actually get loaded
    public void crunch(String body) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 500; i++) {
            data = digest.digest(data);
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

    public FetchResult fetch(HttpClient client, String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long end = System.currentTimeMillis();

            if (response.statusCode() != 200) {
                throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
            }
            String body = response.body();
            return new FetchResult(url, response.statusCode(), body.length(), end - start,
                    Thread.currentThread().getName(), body);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}