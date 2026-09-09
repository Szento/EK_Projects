package app;

//import java.util.concurrent.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() throws InterruptedException {
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
}