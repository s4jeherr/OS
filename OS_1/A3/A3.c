#define _POSIX_C_SOURCE 199309L
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

#define NUM_SWITCHES 1000000

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
int flag = 0;

void* thread_func(void* arg) {
    for (int i = 0; i < NUM_SWITCHES; i++) {
        pthread_mutex_lock(&mutex);
        flag = 1;
        pthread_mutex_unlock(&mutex);
    }
    return NULL;
}

int main() {
    pthread_t thread;
    struct timespec start, end;

    pthread_mutex_lock(&mutex);
    pthread_create(&thread, NULL, thread_func, NULL);

    clock_gettime(CLOCK_MONOTONIC, &start);
    for (int i = 0; i < NUM_SWITCHES; i++) {
        while (!flag); // Wait for the flag
        flag = 0;
        pthread_mutex_unlock(&mutex);
        pthread_mutex_lock(&mutex);
    }
    clock_gettime(CLOCK_MONOTONIC, &end);

    pthread_join(thread, NULL);

    double elapsed = (end.tv_sec - start.tv_sec) * 1e9 + (end.tv_nsec - start.tv_nsec);
    printf("Average context switch time: %.2f ns\n", elapsed / (2 * NUM_SWITCHES));

    pthread_mutex_destroy(&mutex);
    return 0;
}