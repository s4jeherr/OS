#include <stdio.h>
#include <stdlib.h>
#include <zmq.h>
#include <string.h>
#include <time.h>
#include <math.h>

#define MESSAGE_SIZE 256
#define ENDPOINT "tcp://receiver:5555"
#define NUM_MESSAGES 1000 

typedef struct {
    char msg[MESSAGE_SIZE];
    struct timespec send_time;
} message_t;

long calculate_latency(struct timespec *send_time, struct timespec *recv_time) {
    return (recv_time->tv_sec - send_time->tv_sec) * 1e9 + (recv_time->tv_nsec - send_time->tv_nsec);
}

double calculate_confidence_interval(long *latencies, int count, double *mean) {
    long sum = 0;
    for (int i = 0; i < count; i++) {
        sum += latencies[i];
    }
    *mean = sum / (double)count;

    double squared_diff_sum = 0;
    for (int i = 0; i < count; i++) {
        squared_diff_sum += pow(latencies[i] - *mean, 2);
    }
    double std_dev = sqrt(squared_diff_sum / count);

    return 1.96 * (std_dev / sqrt(count));
}

int main() {
    printf("Init Sender\n");
    fflush(stdout);

    void *zmq_context = zmq_ctx_new();
    void *socket = zmq_socket(zmq_context, ZMQ_PUSH); 
    zmq_connect(socket, ENDPOINT);

    message_t buffer;
    memset(&buffer, 0, sizeof(buffer));
    snprintf(buffer.msg, MESSAGE_SIZE, "Test Message");

    struct timespec start_time, end_time;
    clock_gettime(CLOCK_MONOTONIC, &start_time);
    zmq_send(socket, &buffer, sizeof(buffer), 0);

    zmq_recv(socket, &buffer, sizeof(buffer), 0); 
    clock_gettime(CLOCK_MONOTONIC, &end_time);

    long rtt = calculate_latency(&start_time, &end_time);
    printf("Sender: RTT test message received, RTT = %ld ns\n", rtt);
    fflush(stdout);

    long latencies[NUM_MESSAGES];
    double mean_latency = 0;

    for (int i = 0; i < NUM_MESSAGES; i++) {
        memset(&buffer, 0, sizeof(buffer));
        snprintf(buffer.msg, MESSAGE_SIZE, "Latency test message %d", i);
        clock_gettime(CLOCK_MONOTONIC, &buffer.send_time);

        zmq_send(socket, &buffer, sizeof(buffer), 0);

        zmq_recv(socket, &buffer, sizeof(buffer), 0);

        struct timespec recv_time;
        clock_gettime(CLOCK_MONOTONIC, &recv_time);

        latencies[i] = calculate_latency(&buffer.send_time, &recv_time);
    }

    double confidence_interval = calculate_confidence_interval(latencies, NUM_MESSAGES, &mean_latency);
    printf("Sender: Minimal Latency = %ld ns, 95%% Confidence Interval = %.2f ns\n", latencies[0], confidence_interval);

    zmq_close(socket);
    zmq_ctx_destroy(zmq_context);
    return 0;
}
