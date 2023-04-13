import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MergeSort {

    static Random random = new Random(990208);

    public static void mergeSortSerial(int[] array) {
        if (array.length> 1) {
            int mid = array.length / 2;
            int end = array.length;
            int[] leftArray = new int[mid];
            for (int i = 0; i < mid; i++) {
                leftArray[i] = array[i];
            }
            int[] rightArray = new int[end - mid];
            for (int i = 0; i + mid < end; i++) {
                rightArray[i] = array[i+mid];
            }
            mergeSortSerial(leftArray);
            mergeSortSerial(rightArray);
            merge(leftArray, rightArray, array);
        }
    }
    public static void merge(int[] leftArray, int[] rightArray, int[] array) {
        int leftIndex = 0;
        int rightIndex = 0;
        int arrayIndex = 0;
        while ((leftIndex < leftArray.length) && (rightIndex < rightArray.length)) {
            if (leftArray[leftIndex] < rightArray[rightIndex]) {
                array[arrayIndex] = leftArray[leftIndex];
                arrayIndex++;
                leftIndex++;
            }
            else {
                array[arrayIndex] = rightArray[rightIndex];
                arrayIndex++;
                rightIndex++;
            }
        }
        while (leftIndex < leftArray.length) {
            array[arrayIndex] = leftArray[leftIndex];
            arrayIndex++;
            leftIndex++;
        }
        while (rightIndex < rightArray.length) {
            array[arrayIndex] = rightArray[rightIndex];
            arrayIndex++;
            rightIndex++;
        }
    }
    public static void parallelMergeSort(int[] array, int chunkSize, ForkJoinPool fjp) {
        MergeTask mergeTask = new MergeTask(array, chunkSize);
        fjp.invoke(mergeTask);
    }

    public static class MergeTask extends RecursiveAction {
        private int[] array;
        private int chunkSize;
        MergeTask(int[] array, int chunkSize) {

            this.array = array;
            this.chunkSize = chunkSize;
        }
        @Override
        protected void compute() {
            if (array.length > chunkSize) { //if the array can be broken down further
                int mid = array.length / 2;
                int end = array.length;
                int[] leftArray = new int[mid];
                for (int i = 0; i < mid; i++) {
                    leftArray[i] = array[i];
                }
                int[] rightArray = new int[end - mid];
                for (int i = 0; i + mid < end; i++) {
                    rightArray[i] = array[i+mid];
                }
                MergeTask leftTask = new MergeTask(leftArray, chunkSize);
                MergeTask rightTask = new MergeTask(rightArray, chunkSize);
                invokeAll(leftTask, rightTask);
                MergeSortFinal.merge(leftArray, rightArray, array);
            }
            else {
                mergeSortSerial(array);
            }
        }
    }

    public static String mergeSortComparison(boolean sorted, int sizeOfArray, int chunkSize, ForkJoinPool fjp) throws FileNotFoundException {
        if (sorted == true) {
            int[] arrayParallel = new int[sizeOfArray];
            int[] arraySerial = new int[sizeOfArray];
            for (int i = 0; i < sizeOfArray; i++) {
                arrayParallel[i] = i;
                arraySerial[i] = i;
            }

            long startParallel = System.nanoTime();
            parallelMergeSort(arrayParallel, chunkSize, fjp);
            long endParallel = System.nanoTime() - startParallel;

            long startSerial = System.nanoTime();
            mergeSortSerial(arraySerial);
            long endSerial = System.nanoTime() - startSerial;


            try {
                Files.write(Paths.get("MSProg.txt"), (sorted+","+sizeOfArray+","+chunkSize+","+endParallel+","+endSerial+"\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            }
            catch (IOException e) {
                System.err.println("variables do not exist");
            }

            return "For an sorted array of size " + sizeOfArray +" the parallel merge sort took -> " + endParallel + "ns, serial merge sort took -> " + endSerial + "ns. Speed-Up = " + (endSerial/endParallel);
        }
        else {
            int[] arrayParallel = createRandomArray(sizeOfArray, 0, 1000);
            int[] arraySerial = new int[arrayParallel.length];
            for (int i = 0; i < arrayParallel.length; i++) {
                arraySerial[i] = arrayParallel[i];
            }

            long startParallel = System.nanoTime();
            parallelMergeSort(arrayParallel, chunkSize, fjp);
            long endParallel = System.nanoTime() - startParallel;

            long startSerial = System.nanoTime();
            mergeSortSerial(arraySerial);
            long endSerial = System.nanoTime() - startSerial;

            try {
                Files.write(Paths.get("MSProg.txt"), (sorted+","+sizeOfArray+","+chunkSize+","+endParallel+","+endSerial+"\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            }
            catch (IOException e) {
                System.err.println("variables do not exist");
            }
            return "For an unsorted array of size " + sizeOfArray + " the parallel merge sort took -> " + endParallel + "ns, serial merge sort took -> " + endSerial + "ns. Speed-Up = " + (endSerial/endParallel);
        }
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

    public static void main(String[] args) {

        boolean[] sorted = {false, true};
        int[] arraySize = {100,1000,10000,100000,1000000,10000000,100000000};
        int[] threadCount = {1,2,3,4,5,6,7,8};

        for (boolean bool : sorted) {
            for (int size : arraySize) {
                int[] chunkSize = threadSize(size, threadCount);
                for (int chunk : chunkSize) {
                    ForkJoinPool fjp = new ForkJoinPool(Math.round(size/chunk));
                    for (int i = 0; i < 30; i++) {
                        try {
                            mergeSortComparison(bool, size, chunk, fjp);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
