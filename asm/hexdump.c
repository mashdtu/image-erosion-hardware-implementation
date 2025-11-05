#include <stdio.h>
#include <stdint.h>

int main(int argc, char* argv[]) {
    if (argc != 3) {
        printf("Usage: %s <binary_file> <output_txt_file>\n", argv[0]);
        return 1;
    }
    
    FILE* file = fopen(argv[1], "rb");
    if (!file) {
        printf("Error: Cannot open input file %s\n", argv[1]);
        return 1;
    }
    
    FILE* output = fopen(argv[2], "w");
    if (!output) {
        printf("Error: Cannot create output file %s\n", argv[2]);
        fclose(file);
        return 1;
    }
    
    uint32_t instruction;
    int address = 0;
    
    printf("Converting %s to %s...\n", argv[1], argv[2]);
    
    while (fread(&instruction, sizeof(uint32_t), 1, file) == 1) {
        // Write binary representation to file
        for (int i = 31; i >= 0; i--) {
            fprintf(output, "%d", (instruction >> i) & 1);
        }
        fprintf(output, "\n");
        
        address++;
    }
    
    fclose(file);
    fclose(output);
    
    printf("Successfully converted %d instructions to %s\n", address, argv[2]);
    return 0;
}