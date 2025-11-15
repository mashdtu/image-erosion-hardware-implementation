#include <stdint.h>
#include <stdio.h>

#define BMP_WIDTH 20
#define BMP_HEIGTH 20
#define BMP_CHANNELS 1

void erode (volatile unsigned char binary_image[BMP_WIDTH][BMP_HEIGTH], volatile unsigned char bmp_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]) {
    int se_size = 3;
    int se_center = 1;
    int structuringElement[3][3] = {
        {0, 1, 0},
        {1, 1, 1},
        {0, 1, 0}
    };
    volatile unsigned char temp_image[BMP_WIDTH][BMP_HEIGTH];
    for (int x = 0; x < BMP_WIDTH; x++) {
        for (int y = 0; y < BMP_HEIGTH; y++) {
            temp_image[x][y] = binary_image[x][y];
        }
    }
    for (int x = 1; x < BMP_WIDTH - 1; x++) {
        for (int y = 1; y < BMP_HEIGTH - 1; y++) {
            int erosion_result = 0;
            if (temp_image[x][y]) {
                erosion_result = 1;
                for (int i = 0; i < se_size; i++) {
                    for (int j = 0; j < se_size; j++) {
                        if (structuringElement[i][j] == 1) {
                            int entry_x = x + i - 1;
                            int entry_y = y + j - 1;
                            if (temp_image[entry_x][entry_y] == 0) {
                                erosion_result = 0;
                            }
                        }
                    }
                }             
            }
            binary_image[x][y] = erosion_result;
        }
    }
    for (int x = 0; x < BMP_WIDTH; x++) {
        for (int i = 0; i < se_center; i++) {
            binary_image[x][i] = 0;
            binary_image[x][BMP_HEIGTH - 1 - i] = 0;
        }
    }
    for (int y = 0; y < BMP_HEIGTH; y++) {
        for (int i = 0; i < se_center; i++) {
            binary_image[i][y] = 0;
            binary_image[BMP_WIDTH - 1 - i][y] = 0;
        }
    }
}

uint64_t rdtsc() {
    unsigned int lo, hi;
    __asm__ __volatile__ (
        "rdtsc"
        : "=a" (lo), "=d" (hi)
    );
    return ((uint64_t)hi << 32) | lo;
}

int main() {
    volatile unsigned char binary_image[BMP_WIDTH][BMP_HEIGTH];
    volatile unsigned char bmp_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS];

    for (int x = 0; x < BMP_WIDTH; x++) {
        for (int y = 0; y < BMP_HEIGTH; y++) {
            binary_image[x][y] = 1; // or random values
            for (int c = 0; c < BMP_CHANNELS; c++)
                bmp_image[x][y][c] = 0;
        }
    }

    uint64_t start, end;
    start = rdtsc();

    erode(binary_image, bmp_image);

    end = rdtsc();
    printf("CPU cycles: %llu\n", (end - start));
}
