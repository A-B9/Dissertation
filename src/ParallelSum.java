import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelSum {

    static Random random = new Random(990208);
    public static int[] array1;
    public static int[] array2;
    public static int[] empty;

    public static void linearSum(int[] array1, int[] array2, int[] empty) {
        for (int i = 0; i < array1.length; i++) {
            empty[i] = array1[i] + array2[i];
        }
    }

    public class SumTask extends RecursiveAction {
        private int start;
        private int end;
        private int chunkSize;

        public SumTask(int start, int end, int chunkSize) {
            this.start = start;
            this.end = end;
            this.chunkSize = chunkSize;
        }

        @Override
        protected void compute() {
            if (end - start <= chunkSize) {
                for (int i = start; i <= end; i++) {
                    empty[i] = array1[i] + array2[i];
                }
            }
            else {
                int midpoint = (start + end) / 2;
                ParallelSum.SumTask left = new ParallelSum.SumTask(start, midpoint, chunkSize);
                ParallelSum.SumTask right = new ParallelSum.SumTask(midpoint + 1, end, chunkSize);
                invokeAll(left, right);
            }
        }
    }

    public ParallelSum(int[] array1, int[] array2, int[] empty, int chunkSize, ForkJoinPool fjp) {
        if (array1.length != array2.length) {
            throw new RuntimeException("Array 1 and 2 are not the same size");
        }
        if (empty.length != array2.length) {
            throw new RuntimeException("Array 2 and the empty array are not the same size");
        }
        else if (array1.length != empty.length) {
            throw new RuntimeException("Array 1 and the empty array are not the same size");
        }

        ParallelSum.array1 = array1;
        ParallelSum.array2 = array2;
        ParallelSum.empty = empty;

        ParallelSum.SumTask task = new ParallelSum.SumTask(0, array1.length - 1, chunkSize);
        fjp.invoke(task);
    }


    public static int[] threadSize(int arraySize, int[] threadCount) {
        int[] chunkSizes = new int[threadCount.length];

        for (int i = 0; i < threadCount.length; i++) {
            chunkSizes[i] = (int) Math.ceil( ((double) arraySize) /((double) threadCount[i]));
        }
        return chunkSizes;
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

    public static String parallelSumComparison(int arraySize, int threadSize, ForkJoinPool fjp) {
        int[] array1 = createRandomArray(arraySize, 0, 1000);
        int[] array2 = createRandomArray(arraySize, 0, 1000);

        int[] parallelArray = new int[arraySize];
        int[] serialArray = new int[arraySize];

        double startParallelTime = System.nanoTime();
        new ParallelSum(array1, array2, parallelArray, arraySize, fjp);
        double endParallelTime = System.nanoTime() - startParallelTime;

        double startSerialTime = System.nanoTime();
        linearSum(array1, array2, serialArray);
        double endSerialTime = System.nanoTime() - startSerialTime;


        try {
            //Files.write(Paths.get("ParallelSumResults.txt"), (arraySize + ", " + threadSize + ", " + endParallelTime + ", " + endSerialTime +"\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            Files.write(Paths.get("PSProgChange.txt"), (arraySize + ", " + threadSize + ", " + endParallelTime + ", " + endSerialTime +"\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.err.println("Path does not exist.");
        }

        System.out.println("Time taken to complete the parallel array sum: " + endParallelTime + "ns, time taken to complete the serial array sum: " + endSerialTime + "ns. Array size: " + arraySize + ". Thread size: " + threadSize + ". Correct implementation: " + Arrays.equals(parallelArray,serialArray));

        return "Time taken to complete the parallel array sum: " + endParallelTime + "ns, time taken to complete the serial array sum: " + endSerialTime + "ns. Array size: " + arraySize + ". Thread size: " + threadSize + ". Correct implementation: " + Arrays.equals(parallelArray,serialArray);
    }


    public static void main(String[] args) {

        int[] arraySize = {100,1000,10000,100000,1000000,10000000,100000000};
        int[] threadCount = {1,2,3,4,5,6,7,8};
        for (int size : arraySize) {
            int[] chunkSize = threadSize(size, threadCount);
            for (int chunk : chunkSize) {
                ForkJoinPool fjp = new ForkJoinPool(Math.round(size/chunk));
                for (int i = 0; i < 30; i++) {
                    try {
                        parallelSumComparison(size, chunk, fjp);
                    }
                    catch (Exception e) {
                        throw new RuntimeException();
                    }
                }
            }
        }
    }
}
