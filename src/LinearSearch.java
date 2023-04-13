import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class LinearSearch {

    private static Random random = new Random(990208);
    public static int[] array;
    public static int target;

    public static Integer serialLinearSearch(int[] array, int target) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static class LinearSearchParallelTask extends RecursiveTask<Integer> {

        public int start; public int end;
        public int sizeOfThread;
        public LinearSearchParallelTask(int start, int end, int chunkSize) {
            this.start = start;
            this.end = end;
            this.sizeOfThread = chunkSize;
        }

        @Override
        protected Integer compute() {
            if (end - start <= sizeOfThread) {
                for (int i = start; i < end; i++) {
                    if (array[i] == target) {
                        return i;
                    }
                }
                return -1;
            }
            else {
                int midpoint = (end + start) / 2;

                LinearSearch.LinearSearchParallelTask left = new LinearSearch.LinearSearchParallelTask(start, midpoint, sizeOfThread);
                LinearSearch.LinearSearchParallelTask right = new LinearSearch.LinearSearchParallelTask(midpoint + 1, end, sizeOfThread);

                left.fork();
                int rightResult = right.compute();
                int leftResult = left.join();
                if (rightResult != -1) {
                    return rightResult;
                }
                else {
                    return leftResult;
                }
            }
        }
    }

    public static Integer parallelLinearSearch(int[] array, int target, int chunkSize, ForkJoinPool fjp) {
        LinearSearch.array = array;
        LinearSearch.target = target;

        return fjp.invoke(new LinearSearch.LinearSearchParallelTask(0, array.length - 1, chunkSize));
    }

    public static String linearSearchComparison(boolean sorted, int arraySize, int minNumber, int maxNumber, int target, int chunkSize, ForkJoinPool fjp) {
        if (sorted == true) {

            int[] sortedArraySerial = new int[arraySize];
            int[] sortedArrayParallel = createRandomArray(arraySize, minNumber, maxNumber);

            MergeSortFinal.parallelMergeSort(sortedArrayParallel);
            for (int i = minNumber; i < arraySize; i++) {
                sortedArraySerial[i] = sortedArrayParallel[i];
            }

            long startParallel = System.nanoTime();
            int indexParallel = parallelLinearSearch(sortedArrayParallel, target, chunkSize, fjp);
            long endParallel = System.nanoTime() - startParallel;

            long startSerial = System.nanoTime();
            int indexSerial = serialLinearSearch(sortedArraySerial, target);
            long endSerial = System.nanoTime() - startSerial;

            if (indexParallel == -1 && indexSerial == -1) {

                try {
                    //Files.write(Paths.get("LinearResults.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+","+ "N"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                    Files.write(Paths.get("LSProg.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+","+ "N"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
                catch (IOException e) {
                    System.err.println("File does not exist");
                }

                System.out.println("Speed-up: " + endSerial/endParallel +", target not found");
                return "Target was not found in the sorted array, parallel linear search took -> " + endParallel + "ms and serial linear search took -> " + endSerial + "ms.";
            }
            else {

                try {
                    //Files.write(Paths.get("LinearResults.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+"," +  "Y"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);


                    Files.write(Paths.get("LSProg.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+"," +  "Y"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
                catch (IOException e) {
                    System.err.println("File does not exist");
                }

                System.out.println("Speed-up: " + endSerial/endParallel +", target found at parallel: " + indexParallel + ", serial: " + indexSerial +", value of parallel and serial: " + sortedArrayParallel[indexParallel] +", " + sortedArraySerial[indexSerial]);
                return "Target was found in the sorted array at index " + indexParallel + " parallel linear search took " + endParallel + "ms with a thread size of " + chunkSize +" and serial linear search took -> " + endSerial + "ms.";
            }

        }
        else {
            int[] randomArrayParallel = createRandomArray(arraySize, minNumber, maxNumber);
            int[] randomArraySerial = new int[randomArrayParallel.length];
            for (int i =0; i < randomArrayParallel.length; i++) {
                randomArraySerial[i] = randomArrayParallel[i];
            }

            long startParallel = System.nanoTime();
            int indexParallel = parallelLinearSearch(randomArrayParallel, target, chunkSize, fjp);
            long endParallel = System.nanoTime() - startParallel;

            long startSerial = System.nanoTime();
            int indexSerial = serialLinearSearch(randomArraySerial, target);
            long endSerial = System.nanoTime() - startSerial;

            if (indexParallel == -1 && indexSerial == -1) {

                try {
                    //Files.write(Paths.get("LinearResults.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+"," + "N"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);


                    Files.write(Paths.get("LSProg.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+"," + "N"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
                catch (IOException e) {
                    System.err.println("File does not exist");
                }

                System.out.println("Speed-up: " + endSerial/endParallel +", target not found");
                return "Target was not found in the random array, parallel linear search took -> " + endParallel + "ms and serial linear search took -> " + endSerial + "ms.";
            }
            else {

                try {
                    //Files.write(Paths.get("LinearResults.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+"," + "Y"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                    Files.write(Paths.get("LSProg.txt"), (sorted+","+ arraySize +","+ chunkSize +"," +endParallel+","+endSerial+"," + "Y"+ "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
                catch (IOException e) {
                    System.err.println("File does not exist");
                }

                System.out.println("Speed-up: " + endSerial/endParallel +", target found at parallel: " + indexParallel + ", serial: " + indexSerial +", value of parallel and serial: " + randomArrayParallel[indexParallel] +", " + randomArraySerial[indexSerial]);
                return "Target was found in the random array at index " + indexParallel + " parallel linear search took " + endParallel + "ms with a thread size of " + chunkSize + " and serial linear search took -> " + endSerial + "ms.";
            }
        }
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

    public static Integer randomNumber(int max) {
        int rand = random.nextInt(max);
        return rand;
    }

    public static int[] threadSize(int arraySize, int[] threadCount) {
        int[] chunkSizes = new int[threadCount.length];

        for (int i = 0; i < threadCount.length; i++) {
            chunkSizes[i] = (int) Math.ceil( ((double) arraySize) /((double) threadCount[i]));
        }
        return chunkSizes;
    }

    public static void main(String[] args) {

        int[] arraySize = {1000,10000,100000,1000000,10000000,100000000};
        boolean[] sorted = {false, true};
        int[] threadCount = {1,2,3,4,5,6,7,8};


        for (boolean sort : sorted) {
            for (int size : arraySize) {
                int[] chunkSize = threadSize(size, threadCount);
                for (int chunk: chunkSize) {
                    ForkJoinPool fjp = new ForkJoinPool(Math.round(size/chunk));
                    for (int i = 0; i < 30; i++) {
                        int minNum = 0;
                        int maxNum = 0;
                        while (maxNum - minNum <= 0) {
                            minNum = randomNumber(500);
                            maxNum = randomNumber(1000);
                        }
                        try {
                            linearSearchComparison(sort, size, minNum, maxNum, randomNumber(1000), chunk, fjp);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
