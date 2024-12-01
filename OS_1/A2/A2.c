#define _POSIX_C_SOURCE 199309L
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <time.h>

#define NUM_ITERATIONS 1000000  // Number of iterations for accurate results

double get_time() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec + ts.tv_nsec / 1e9;  // Return time in seconds
}

int main() {
    int fd = open("sample.txt", O_RDONLY);
    if (fd == -1) {
        perror("Error opening file");
        return 1;
    }

    char buffer[1];
    double start_time, end_time, total_time = 0;

    for (int i = 0; i < NUM_ITERATIONS; i++) {
        lseek(fd, 0, SEEK_SET);  // Reset file position
        start_time = get_time();
        ssize_t bytes_read = read(fd, buffer, 1);
        end_time = get_time();

        if (bytes_read == -1) {
            perror("Error reading file");
            close(fd);
            return 1;
        }

        total_time += (end_time - start_time);
    }

    close(fd);

    double average_latency = total_time / NUM_ITERATIONS;
    printf("Average latency of read() system call: %.9lf seconds\n", average_latency);

    return 0;
}