#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <string.h>
#include <time.h>

#define NUM_RUNS 1000 

typedef struct {
    char message[256]; 
    sem_t sem_empty;    
    sem_t sem_full;     
} SharedMemory;

long calculate_minimal_latency(long latencies[], int count) {
    long min_latency = latencies[0];
    for (int i = 1; i < count; i++) {
        if (latencies[i] < min_latency) {
            min_latency = latencies[i];
        }
    }
    return min_latency;
}

void sort_latencies(long latencies[], int count) {
    for (int i = 0; i < count - 1; i++) {
        for (int j = i + 1; j < count; j++) {
            if (latencies[i] > latencies[j]) {
                long temp = latencies[i];
                latencies[i] = latencies[j];
                latencies[j] = temp;
            }
        }
    }
}

long calculate_95_percentile(long latencies[], int count) {
    sort_latencies(latencies, count);
    int index = (int)(0.95 * count);
    return latencies[index];  // 95th percentile latency
}

void *sender(void *arg) {
    SharedMemory *shared_mem = (SharedMemory *)arg;
    const char *msg = "Hello, Receiver!";
    struct timespec start, end;

    clock_gettime(CLOCK_MONOTONIC, &start);

    sem_wait(&shared_mem->sem_empty);

    strncpy(shared_mem->message, msg, sizeof(shared_mem->message));

    sem_post(&shared_mem->sem_full);

    clock_gettime(CLOCK_MONOTONIC, &end); 

    long latency_ns = (end.tv_sec - start.tv_sec) * 1e9 + (end.tv_nsec - start.tv_nsec);
    //printf("Sender: Message sent. Latency = %ld ns\n", latency_ns);

    return (void *)latency_ns;
}

void *receiver(void *arg) {
    SharedMemory *shared_mem = (SharedMemory *)arg;

    sem_wait(&shared_mem->sem_full);

    //printf("Receiver: Received message: %s\n", shared_mem->message);

    sem_post(&shared_mem->sem_empty);

    return NULL;
}

int main() {
    SharedMemory shared_mem;
    memset(shared_mem.message, 0, sizeof(shared_mem.message));

    //Initialize semaphores
    sem_init(&shared_mem.sem_empty, 0, 1);  // Initially empty
    sem_init(&shared_mem.sem_full, 0, 0);   // Initially not full

    long latencies[NUM_RUNS];

    for (int run = 0; run < NUM_RUNS; run++) {
        pthread_t sender_thread, receiver_thread;
        pthread_create(&sender_thread, NULL, sender, &shared_mem);
        pthread_create(&receiver_thread, NULL, receiver, &shared_mem);

        void *sender_latency;
        pthread_join(sender_thread, &sender_latency);
        pthread_join(receiver_thread, NULL);

        latencies[run] = (long)sender_latency;

        //printf("Sender: Run %d, Latency = %ld ns\n", run + 1, latencies[run]);
    }

    sort_latencies(latencies, NUM_RUNS);
    long minimal_latency = latencies[0];
    long conf_interval = calculate_95_percentile(latencies, NUM_RUNS);

    printf("Sender: Minimal Latency = %ld ns\n", minimal_latency);
    printf("Sender: 95%% Confidence Interval for Minimal Latency = %ld ns\n", conf_interval);

    //Cleanup semaphores
    sem_destroy(&shared_mem.sem_empty);
    sem_destroy(&shared_mem.sem_full);

    return 0;
}
