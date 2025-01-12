#include <stdio.h>
#include <stdlib.h>
#include <zmq.h>
#include <string.h>
#include <time.h>

#define MESSAGE_SIZE 256
#define ENDPOINT "ipc://my_endpoint"
#define LOG_FILE "latency_log.txt"

int main() {
    //Initialize ZeroMQ context
    void *zmq_context = zmq_ctx_new();
    void *socket = zmq_socket(zmq_context, ZMQ_PAIR);

    zmq_bind(socket, ENDPOINT);

    char buffer[MESSAGE_SIZE];
    memset(buffer, 0, sizeof(buffer));

    zmq_recv(socket, buffer, sizeof(buffer), 0);

    struct timespec recv_time;
    clock_gettime(CLOCK_MONOTONIC, &recv_time);  // Time when message is received

    printf("Receiver: Received message: %s\n", buffer);
    printf("Receiver timestamp: %ld.%09ld\n", recv_time.tv_sec, recv_time.tv_nsec);

    //Logging
    FILE *log_file = fopen(LOG_FILE, "a");  
    if (log_file) {
        fprintf(log_file, "Receiver timestamp: %ld.%09ld\n", recv_time.tv_sec, recv_time.tv_nsec);
        fclose(log_file); 
    } else {
        printf("Failed to open log file.\n");
    }

    //Cleanup
    zmq_close(socket);
    zmq_ctx_destroy(zmq_context);

    return 0;
}
