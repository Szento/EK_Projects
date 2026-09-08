package app;

import lombok.Data;
import lombok.NonNull;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Person person = new Person("Holger", 33);
        System.out.println("hi From "+ Thread.currentThread().getName());
        Thread thread1 = new Thread(()-> System.out.println("hi From "+ Thread.currentThread().getName()));
        Runnable runnable = new Runnable(){

            @Override
            public void run(){
                System.out.println("hi From "+ Thread.currentThread().getName());
            }
        };
        thread1.start();
        Thread thread2 = new Thread(new MyRunnable(person));
        thread2.start();
        System.out.println(person);
        thread1.join();
        thread2.join();
        System.out.println("After join "+ person );
        System.out.println("finish program");
    }

    private static class MyRunnable implements Runnable{
        Person person;
        public MyRunnable(Person person ){
            this.person = person;
        }
        @Override 
        public void run(){
            person.setName("Helga");

        }
    }

    @Data
    private static class Person{
        @NonNull
        String name;
        @NonNull 
        int age;
    }
}