public class DeadLock {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) {

        // Thread 1
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Acquired lock1");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread 1: Waiting for lock2");
                synchronized (lock2) {
                    System.out.println("Thread 1: Acquired lock1 and lock2");
                }
            }
        });

        // Thread 2
        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2: Acquired lock2");

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread 2: Waiting for lock1");
                synchronized (lock1) {
                    System.out.println("Thread 2: Acquired lock2 and lock1");
                }
            }
        });

        // Start both threads
        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    In this example, we have two threads and two locks: lock1 and lock2.
    The first thread acquires lock1 and then attempts to acquire lock2.
    The second thread acquires lock2 and then attempts to acquire lock1.
    Both threads try to acquire the locks in different orders, resulting in a circular wait, causing a deadlock.

    To avoid deadlocks, you can implement strategies such as acquiring locks in the same order in all threads,
    using a timeout while acquiring locks, or using the tryLock() method from the java.util.concurrent.locks package.
     */
}
