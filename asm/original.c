#define BMP_WIDTH 950
#define BMP_HEIGTH 950
#define BMP_CHANNELS 3

// Apply the erosion algorithm to the binary image using a structuring element.
void erode (unsigned char binary_image[BMP_WIDTH][BMP_HEIGTH], unsigned char bmp_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]) {

    // Define a 3x3 structuring element (cross shape for cell detection).
    // 1 means the pixel is part of the structuring element, 0 means it's ignored.

    // Define size of structuring element.
    int se_size = 3;

    // Define center of the structuring element.
    int se_center = 1; // Used for making sure the pixel is not at the border. Dividing integers automatically rounds down.

    // Define the structuring element itself.
    int structuringElement[3][3] = {
        {0, 1, 0},
        {1, 1, 1},
        {0, 1, 0}
    };
    
    // Create a temporary array to store the result.
    unsigned char temp_image[BMP_WIDTH][BMP_HEIGTH];
    
    // Copy the original binary image to the temporary array.
    for (int x = 0; x < BMP_WIDTH; x++) {
        for (int y = 0; y < BMP_HEIGTH; y++) {
            temp_image[x][y] = binary_image[x][y];
        }
    }

    printf("Starting erosion with cross-shaped structuring element...\n");

    // Apply the erosion algorithm for non-border pixels
    for (int x = 1; x < BMP_WIDTH - 1; x++) {
        for (int y = 1; y < BMP_HEIGTH - 1; y++) {


            int erosion_result = 0;
            

            if (temp_image[x][y]) {

                // Assume erosion passes initially, i.e. that there are no black pixels within the structuring element.
                erosion_result = 1;
            
                // Check if the selected pixel should be eroding by comparing the surrounding grid with the structuring element.
                
                // Check each position in the structuring element projected on the binary image.
                for (int i = 0; i < se_size; i++) {
                    for (int j = 0; j < se_size; j++) {

                        // Only check positions where the structuring element has a 1.
                        if (structuringElement[i][j] == 1) {

                            // Calculate the position of the selected entry of the structuring element relative to the selected pixel.
                            int entry_x = x + i - 1;
                            int entry_y = y + j - 1;
                            
                            // If any black pixel is contain on an entry of the structuring element equal to 1, the erosion fails.
                            if (temp_image[entry_x][entry_y] == 0) {
                                erosion_result = 0;
                            }
                        }
                    }
                }             
            }

            // Set the pixel to the result of the erosion, i.e. if the erosion failed (0), then set the pixel to 0 (black). If it succeeded set it to 1 (white).
            binary_image[x][y] = erosion_result;
        }
    }
    
    // Set border pixels to black to avoid boundary issues.

    // Horizontal borders:
    for (int x = 0; x < BMP_WIDTH; x++) {
        for (int i = 0; i < se_center; i++) {
            binary_image[x][i] = 0;  // Top border
            binary_image[x][BMP_HEIGTH - 1 - i] = 0;  // Bottom border
        }
    }

    // Vertical borders:
    for (int y = 0; y < BMP_HEIGTH; y++) {
        for (int i = 0; i < se_center; i++) {
            binary_image[i][y] = 0;  // Left border
            binary_image[BMP_WIDTH - 1 - i][y] = 0;  // Right border
        }
    }
}