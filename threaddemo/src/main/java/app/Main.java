package app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.net.http.HttpClient;

//import java.util.concurrent.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() throws InterruptedException, ExecutionException {
        HttpFetcher fetcher = new HttpFetcher(); 
        HttpClient httpClient = HttpClient.newHttpClient();
        String[] urls = new String[]{
        "https://icanhazdadjoke.com/"
        ,"https://api.chucknorris.io/jokes/random"
        ,"https://api.kanye.rest"
        ,"https://api.whatdoestrumpthink.com/api/v1/quotes/random"
        // ,"https://v2.jokeapi.dev/joke/Coding?type=single"
        // ,"https://geek-jokes.sameerkumar.website/api?format=json"
        // ,"https://official-joke-api.appspot.com/random_joke"
        };
        List<Callable<String>> callableList = new ArrayList<>();
        List<Future<String>> futureList = new ArrayList<>();
        List<String> jokes = new ArrayList<>();

        for (String url : urls){
            Callable<String> callable = ()->{
                return fetcher.fetch(httpClient, url).content();
            };
            callableList.add(callable);
        }
        ExecutorService executorService  = Executors.newFixedThreadPool(4);
        for (Callable<String> callable : callableList){
            Future<String> future = executorService.submit(callable);
            futureList.add(future);
        } 
        for (Future<String> future : futureList) {
            String result = future.get();
            jokes.add(result);
        }
        
        
        // FetchResult fr = fetcher.fetch(urls[0]);
        // System.out.println(fr);
        
        
        new Main().test();
    }
    public void test() throws InterruptedException{

        Person person = new Person("Holger",33);
        System.out.println("Hi from " + Thread.currentThread().getName());
        Thread thread1 = new Thread(() -> System.out.println("Hi from " + Thread.currentThread().getName()));
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                System.out.println("Hi from " + Thread.currentThread().getName());
            }
        };
        thread1.start();
        Thread thread2 = new Thread(new MyRunnable(person));
        thread2.start();
        System.out.println(person);
        thread1.join();
        thread2.join();
        System.out.println("After join: "+person);
        System.out.println("Finish program");
    }

    private static class MyRunnable implements Runnable{
        Person person;
        public MyRunnable(Person person){
            this.person = person;
        }
        @Override
        public void run(){
            person.setName("Helga");
        }
    }

    @Data
    @AllArgsConstructor
    private static class Person{
//        @NonNull
        String name;
//        @NonNull
        int age;
    }

    @Data 
    @AllArgsConstructor 
    private static class JokesDTO{
        String dadJoke;
        String chuckJoke;
        String kanyeJoke;
        String trumpJoke;

    }
}