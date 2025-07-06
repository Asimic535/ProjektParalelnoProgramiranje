
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <time.h>

#define ASCENDING 1
#define DESCENDING 0
#define MAX 1048576 // 2^20

int *array;
int num_threads;

typedef struct {
    int start;
    int end;
    int direction;
} thread_data;

void swap(int *a, int *b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

void compare_and_swap(int low, int cnt, int direction) {
    int k = cnt / 2;
    for (int i = low; i < low + k; i++) {
        if ((direction == ASCENDING && array[i] > array[i + k]) ||
            (direction == DESCENDING && array[i] < array[i + k])) {
            swap(&array[i], &array[i + k]);
        }
    }
}

void bitonic_merge(int low, int cnt, int direction) {
    if (cnt > 1) {
        compare_and_swap(low, cnt, direction);
        int k = cnt / 2;
        bitonic_merge(low, k, direction);
        bitonic_merge(low + k, k, direction);
    }
}

void bitonic_sort(int low, int cnt, int direction) {
    if (cnt > 1) {
        int k = cnt / 2;
        bitonic_sort(low, k, ASCENDING);
        bitonic_sort(low + k, k, DESCENDING);
        bitonic_merge(low, cnt, direction);
    }
}

void *thread_sort(void *arg) {
    thread_data *data = (thread_data *)arg;
    bitonic_sort(data->start, data->end - data->start, data->direction);
    pthread_exit(NULL);
}

void merge_chunks(int chunk_size, int num_chunks) {
    int step = 2;
    while (step <= num_chunks) {
        for (int i = 0; i < num_chunks; i += step) {
            int start = i * chunk_size;
            int cnt = step * chunk_size;
            bitonic_merge(start, cnt, ASCENDING);
        }
        step *= 2;
    }
}

void generate_array(int *arr, int n) {
    for (int i = 0; i < n; i++) {
        arr[i] = rand() % 1000000;
    }
}

int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf("Koristenje: %s <num_threads>\n", argv[0]);
        return 1;
    }

    num_threads = atoi(argv[1]);
    array = malloc(MAX * sizeof(int));
    generate_array(array, MAX);

    pthread_t threads[num_threads];
    thread_data td[num_threads];
    int chunk_size = MAX / num_threads;

    for (int i = 0; i < num_threads; i++) {
        td[i].start = i * chunk_size;
        td[i].end = (i + 1) * chunk_size;
        td[i].direction = (i % 2 == 0) ? ASCENDING : DESCENDING;
        pthread_create(&threads[i], NULL, thread_sort, &td[i]);
    }

    for (int i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }

    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);

    merge_chunks(chunk_size, num_threads);

    clock_gettime(CLOCK_MONOTONIC, &end);

    double elapsed = (end.tv_sec - start.tv_sec) + (end.tv_nsec - start.tv_nsec) / 1e9;

    printf("Vrijeme sortiranja: %.3f sekundi\n", elapsed);
    printf("Prvih 10 elemenata sortiranog niza: ");
    for (int i = 0; i < 10; i++) {
        printf("%d ", array[i]);
    }
    printf("\n");

    free(array);
    return 0;
}
