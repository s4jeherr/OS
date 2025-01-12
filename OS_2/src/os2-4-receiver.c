#include <stdio.h>
#include <stdlib.h>
#include <zmq.h>
#include <string.h>
#include <time.h>

#define MESSAGE_SIZE 256
#define ENDPOINT "tcp://*:5555"

typedef struct {
    char msg[MESSAGE_SIZE];
    struct timespec send_time;
} message_t;

int main() {
    printf("Init Receiver\n");
    fflush(stdout);

    void *zmq_context = zmq_ctx_new();
    void *socket = zmq_socket(zmq_context, ZMQ_PULL);  
    zmq_bind(socket, ENDPOINT);

    message_t buffer;
    memset(&buffer, 0, sizeof(buffer));

    zmq_recv(socket, &buffer, sizeof(buffer), 0);  
    zmq_send(socket, &buffer, sizeof(buffer), 0);  

    for (int i = 0; i < 1000; i++) {
        zmq_recv(socket, &buffer, sizeof(buffer), 0);

        struct timespec recv_time;
        clock_gettime(CLOCK_MONOTONIC, &recv_time);  

        zmq_send(socket, &buffer, sizeof(buffer), 0);
    }

    zmq_close(socket);
    zmq_ctx_destroy(zmq_context);
    return 0;
}
