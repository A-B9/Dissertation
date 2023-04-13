public class RaceConditions {

    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        // Create two threads
        Thread thread1 = new Thread(RaceConditions::incrementCounter1);
        Thread thread2 = new Thread(RaceConditions::incrementCounter1);

        // Start both threads
        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        thread1.join();
        thread2.join();

        // Print the final value of the counter
        System.out.println("Counter value: " + counter);
    }

    private static void incrementCounter1() {
        for (int i = 0; i < 100000; i++) {
            counter++;
        }
    }

    /**
     * In this example, the incrementCounter method increments the shared counter variable 100,000 times.
     * Since both threads call this method simultaneously, they might try to access and update the counter variable at the same time,
     * resulting in unpredictable behavior and incorrect results.
     *
     * To fix this race condition, you can use synchronization mechanisms,
     * like the synchronized keyword or a lock, to ensure that only one thread can access the shared resource at a time.
     * Here's an example of how to fix the race condition using the synchronized keyword:
     */

    private static synchronized void incrementCounter2() {
        for (int i = 0; i < 100000; i++) {
            counter++;
        }
    }
    /*
    Now, the incrementCounter method is synchronized,
    ensuring that only one thread can access and modify the shared counter variable at a time, thus eliminating the race condition
     */
}
