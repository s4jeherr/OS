#include <stdio.h>
#include <stdlib.h>
#include <zmq.h>
#include <string.h>
#include <time.h>
#include <math.h>

#define MESSAGE_SIZE 256
#define ENDPOINT "ipc://my_endpoint"
#define LOG_FILE "latency_log.txt"
#define NUM_RUNS 1000  

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
    return latencies[index];
}

int main() {
    //Initialize ZeroMQ context
    void *zmq_context = zmq_ctx_new();
    void *socket = zmq_socket(zmq_context, ZMQ_PAIR);

    zmq_connect(socket, ENDPOINT);

    long latencies[NUM_RUNS];

    for (int run = 0; run < NUM_RUNS; run++) {
        const char *msg = "Hello, Receiver!";

        struct timespec send_time;
        clock_gettime(CLOCK_MONOTONIC, &send_time);

        zmq_send(socket, msg, strlen(msg), 0);

        struct timespec sent_time;
        clock_gettime(CLOCK_MONOTONIC, &sent_time); 

        long latency_ns = (sent_time.tv_sec - send_time.tv_sec) * 1e9 + (sent_time.tv_nsec - send_time.tv_nsec);
        latencies[run] = latency_ns;

        //Latency logger
        FILE *log_file = fopen(LOG_FILE, "a");
        if (log_file) {
            fprintf(log_file, "Sender timestamp: %ld.%09ld\n", send_time.tv_sec, send_time.tv_nsec);
            fprintf(log_file, "Sent timestamp: %ld.%09ld\n", sent_time.tv_sec, sent_time.tv_nsec);
            fprintf(log_file, "Calculated latency: %ld ns\n", latency_ns);
            fclose(log_file);
        } else {
            printf("Failed to open log file.\n");
        }

        //printf("Sender: Run %d, Latency = %ld ns\n", run + 1, latency_ns);
    }

    sort_latencies(latencies, NUM_RUNS);
    long minimal_latency = latencies[0];
    long conf_interval = calculate_95_percentile(latencies, NUM_RUNS);

    printf("Sender: Minimal Latency = %ld ns\n", minimal_latency);
    printf("Sender: 95%% Confidence Interval for Minimal Latency = %ld ns\n", conf_interval);

    //Cleanup
    zmq_close(socket);
    zmq_ctx_destroy(zmq_context);

    return 0;
}
