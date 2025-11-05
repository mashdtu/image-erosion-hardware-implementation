#define BMP_WIDTH 950
#define BMP_HEIGTH 950
#define BMP_CHANNELS 3

void erode (unsigned char binary_image[BMP_WIDTH][BMP_HEIGTH], unsigned char bmp_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]) {
    int se_size = 3;
    int se_center = 1;
    int structuringElement[3][3] = {
        {0, 1, 0},
        {1, 1, 1},
        {0, 1, 0}
    };
    unsigned char temp_image[BMP_WIDTH][BMP_HEIGTH];
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

int main(int argc, char const *argv[])
{
    unsigned char binary_image[BMP_WIDTH][BMP_HEIGTH];
    unsigned char bmp_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS];
    erode(binary_image, bmp_image);
    return 0;
}
