import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelTotalSum {

    static Random random = new Random(990208);
    private static final Object lock = new Object();

    public static Integer serialTotalSum(int[] array1, int[] array2) throws Exception {
        if (array1.length != array2.length) {
            throw new Exception("Arrays not same size");
        }

        int total = 0;
        for (int i = 0; i < array1.length; i++) {
            total = total + array1[i] + array2[i];
        }
        return total;
    }

    public static class ArraySumTask extends RecursiveAction {
        private final int[] array1;
        private final int[] array2;
        private final int start1;
        private final int end1;
        private final int start2;
        private final int end2;
        private final int chunkSize;

        public ArraySumTask(int[] array1, int start1, int end1, int[] array2, int start2, int end2, int chunkSize) {
            this.array1 = array1;
            this.array2 = array2;
            this.start1 = start1;
            this.end1 = end1;
            this.start2 = start2;
            this.end2 = end2;
            this.chunkSize = chunkSize;
        }

        @Override
        protected void compute() {
            if (end1 - start1 <= chunkSize && end2 - start2 <= chunkSize) {
                int arr1Sum = 0;
                for (int i = start1; i < end1; i++) {
                    arr1Sum += array1[i];
                }
                int arr2Sum = 0;
                for (int i = start2; i < end2; i++) {
                    arr2Sum += array2[i];
                }

                synchronized (lock) {
                    sum = sum + arr2Sum + arr1Sum;
                }
                //sum = sum + arr2Sum + arr1Sum;
            } else {
                int mid1 = (start1 + end1) / 2;
                int mid2 = (start2 + end2) / 2;
                ParallelTotalSum.ArraySumTask left = new ParallelTotalSum.ArraySumTask(array1, start1, mid1, array2, start2, mid2, chunkSize);
                ParallelTotalSum.ArraySumTask right = new ParallelTotalSum.ArraySumTask(array1, mid1, end1, array2, mid2, end2, chunkSize);
                invokeAll(left, right);
            }

        }
    }

    public static void parallelTotalSum(int[] array1, int[] array2, int chunkSize, ForkJoinPool fjp){
        synchronized (lock) {
            sum = 0;
        }
        ArraySumTask task = new ArraySumTask(array1, 0, array1.length, array2, 0, array2.length, chunkSize);
        fjp.invoke(task);
    }

    public static String parallelTotalSumComparison(int arraySize, int chunkSize, ForkJoinPool fjp) throws Exception {
        int[] array1 = createRandomArray(arraySize, 0, 1000);
        int[] array2 = createRandomArray(arraySize, 0, 1000);

        double startParallelTime = System.nanoTime();
        parallelTotalSum(array1, array2, chunkSize, fjp);
        double endParallelTime = System.nanoTime() - startParallelTime;

        double startSerialTime = System.nanoTime();
        int serialResult = serialTotalSum(array1, array2);
        double endSerialTime = System.nanoTime() -startSerialTime;

        System.out.print("parallel: " + sum +", serial: " + serialResult);

        try {
            //Files.write(Paths.get("ParallelTotalSumResults.txt"), (arraySize + "," + chunkSize + "," + endParallelTime + "," + endSerialTime + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            Files.write(Paths.get("PTSProg.txt"), (arraySize + "," + chunkSize + "," + endParallelTime + "," + endSerialTime + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("variables do not exist");
        }

        return "Parallel Result: " + sum + ", Serial Result: " + serialResult + ", Parallel Time: " + endParallelTime + ", Serial Time: " + endSerialTime + ", Speed Up: " + (endSerialTime/endParallelTime);
    }

    public static int[] createRandomArray(int sizeOfArray, int min, int max) {
        int[] array = new int[sizeOfArray];
        int minimum = min;
        int maximum = max;

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(maximum - minimum + 1) + minimum;
        }
        return array;
    }

    public static int[] threadSize(int arraySize, int[] threadCount) {
        int[] chunkSizes = new int[threadCount.length];

        for (int i = 0; i < threadCount.length; i++) {
            chunkSizes[i] = (int) Math.ceil( ((double) arraySize) /((double) threadCount[i]));
        }
        return chunkSizes;
    }

    private static volatile int sum = 0;

    public static void main(String[] args) throws Exception {

        int[] arraySize = {100,1000,10000,100000,1000000,10000000,100000000};
        int[] threadCount = {1,2,3,4,5,6,7,8};

        for (int size : arraySize) {
            int[] chunkSize = threadSize(size, threadCount);
            for (int chunk : chunkSize) {
                ForkJoinPool fjp = new ForkJoinPool(Math.round(size/chunk));
                for (int i = 0; i < 30; i++) {
                    try {
                        parallelTotalSumComparison(size, chunk, fjp);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
