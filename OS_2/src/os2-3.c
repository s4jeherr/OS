#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <zmq.h>
#include <string.h>
#include <time.h>
#include <math.h>

#define MESSAGE_SIZE 256
#define ENDPOINT "inproc://my_endpoint"
#define NUM_RUNS 1000  

void *zmq_context;

void calculate_confidence_interval(long *latencies, int n, long *min_latency, long *conf_interval) {
    for (int i = 0; i < n - 1; ++i) {
        for (int j = i + 1; j < n; ++j) {
            if (latencies[i] > latencies[j]) {
                long temp = latencies[i];
                latencies[i] = latencies[j];
                latencies[j] = temp;
            }
        }
    }

    *min_latency = latencies[0];

    int index = (int)(0.95 * n) - 1;
    *conf_interval = latencies[index];
}

void *sender(void *arg) {
    void *socket = zmq_socket(zmq_context, ZMQ_PAIR);
    zmq_connect(socket, ENDPOINT);

    const char *msg = "Hello, Receiver!";
    long *latencies = (long *)arg;

    for (int i = 0; i < NUM_RUNS; ++i) {
        struct timespec start, end;

        clock_gettime(CLOCK_MONOTONIC, &start);

        zmq_send(socket, msg, strlen(msg), 0);

        clock_gettime(CLOCK_MONOTONIC, &end);

        latencies[i] = (end.tv_sec - start.tv_sec) * 1e9 + (end.tv_nsec - start.tv_nsec);
    }

    zmq_close(socket);
    return NULL;
}

void *receiver(void *arg) {
    void *socket = zmq_socket(zmq_context, ZMQ_PAIR);
    zmq_bind(socket, ENDPOINT);

    char buffer[MESSAGE_SIZE];
    memset(buffer, 0, sizeof(buffer));

    for (int i = 0; i < NUM_RUNS; ++i) {
        zmq_recv(socket, buffer, sizeof(buffer), 0);
    }

    zmq_close(socket);
    return NULL;
}

int main() {
    zmq_context = zmq_ctx_new();

    pthread_t sender_thread, receiver_thread;

    long latencies[NUM_RUNS]; 

    pthread_create(&receiver_thread, NULL, receiver, NULL);
    pthread_create(&sender_thread, NULL, sender, latencies);

    pthread_join(sender_thread, NULL);
    pthread_join(receiver_thread, NULL);

    long min_latency, conf_interval;
    calculate_confidence_interval(latencies, NUM_RUNS, &min_latency, &conf_interval);

    printf("Minimal Latency: %ld ns\n", min_latency);
    printf("95%% Confidence Interval: %ld ns\n", conf_interval);

    zmq_ctx_destroy(zmq_context);

    return 0;
}
