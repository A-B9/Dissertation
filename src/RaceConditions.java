public class RaceConditions {

    private static int counter1 = 0;
    private static int counter2 = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Implementation with a Race Condition");

        Thread thread1 = new Thread(RaceConditions::incrementCounter1);
        Thread thread2 = new Thread(RaceConditions::incrementCounter1);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("Counter: " + counter1);

        System.out.println("Implementation without a Race Condition");

        Thread thread3 = new Thread(RaceConditions::incrementCounter2);
        Thread thread4 = new Thread(RaceConditions::incrementCounter2);

        thread3.start();
        thread4.start();

        thread3.join();
        thread4.join();

        System.out.println("Counter: " + counter2);
    }

    private static void incrementCounter1() {
        for (int i = 0; i < 100000; i++) {
            counter1++;
        }
    }

    private static synchronized void incrementCounter2() {
        for (int i = 0; i < 100000; i++) {
            counter2++;
        }
    }

}
