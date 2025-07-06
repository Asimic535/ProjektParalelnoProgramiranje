import java.util.Arrays;
import java.util.Random;

public class BitonicSortJava {
    static final int MAX = 1 << 20; // 2^20
    static final int ASCENDING = 1;
    static final int DESCENDING = 0;
    static int[] array;

    static class SortThread extends Thread {
        int start, end, direction;

        SortThread(int start, int end, int direction) {
            this.start = start;
            this.end = end;
            this.direction = direction;
        }

        @Override
        public void run() {
            bitonicSort(start, end - start, direction);
        }
    }

    static void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    static void compareAndSwap(int low, int cnt, int direction) {
        int k = cnt / 2;
        for (int i = low; i < low + k; i++) {
            if ((direction == ASCENDING && array[i] > array[i + k]) ||
                (direction == DESCENDING && array[i] < array[i + k])) {
                swap(i, i + k);
            }
        }
    }

    static void bitonicMerge(int low, int cnt, int direction) {
        if (cnt > 1) {
            compareAndSwap(low, cnt, direction);
            int k = cnt / 2;
            bitonicMerge(low, k, direction);
            bitonicMerge(low + k, k, direction);
        }
    }

    static void bitonicSort(int low, int cnt, int direction) {
        if (cnt > 1) {
            int k = cnt / 2;
            bitonicSort(low, k, ASCENDING);
            bitonicSort(low + k, k, DESCENDING);
            bitonicMerge(low, cnt, direction);
        }
    }

    static void mergeChunks(int chunkSize, int numChunks) {
        int step = 2;
        while (step <= numChunks) {
            for (int i = 0; i < numChunks; i += step) {
                int start = i * chunkSize;
                int cnt = step * chunkSize;
                bitonicMerge(start, cnt, ASCENDING);
            }
            step *= 2;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Korištenje: java BitonicSortJava <broj_niti>");
            return;
        }

        int numThreads = Integer.parseInt(args[0]);

        // Provjera je li broj niti potencija broja 2
        if ((numThreads & (numThreads - 1)) != 0) {
            System.out.println("Broj niti mora biti potencija broja 2 (npr. 2, 4, 8, 16...)");
            return;
        }

        array = new int[MAX];
        Random rand = new Random();
        for (int i = 0; i < MAX; i++) {
            array[i] = rand.nextInt(1_000_000);
        }

        int chunkSize = MAX / numThreads;
        SortThread[] threads = new SortThread[numThreads];

        long startTime = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i + 1) * chunkSize;
            int direction = (i % 2 == 0) ? ASCENDING : DESCENDING;
            threads[i] = new SortThread(start, end, direction);
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mergeChunks(chunkSize, numThreads);

        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1e9;

        System.out.printf("Vrijeme sortiranja: %.3f sekundi\n", elapsedSeconds);
        System.out.print("Prvih 10 elemenata sortiranog niza: ");
        for (int i = 0; i < 10; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }
}
