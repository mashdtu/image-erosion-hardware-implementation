#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>

#define MAX_INSTRUCTIONS 100
#define MAX_LINE_LENGTH 256
#define MAX_LABELS 100

// Instruction opcodes
#define OP_ADD      0b010000    // 010000 (R-Type)
#define OP_LOAD     0b100000    // 100000 (I-Type)
#define OP_STORE    0b100001    // 100001 (I-Type)
#define OP_ADDI     0b100010    // 100010 (I-Type)
#define OP_SUBI     0b100011    // 100011 (I-Type)
#define OP_BEQ      0b100100    // 100100 (I-Type)
#define OP_BGE      0b100101    // 100101 (I-Type)
#define OP_JUMP     0b110000    // 110000 (J-Type)
#define OP_END      0b000000    // 000000 (Special Type)


// Instruction types
typedef enum {
    R_TYPE,
    I_TYPE,
    J_TYPE,
    SPECIAL
} instruction_type_t;

// Instruction structure
typedef struct {
    char mnemonic[10];
    uint8_t opcode;
    instruction_type_t type;
} instruction_info_t;

// Label structure for jump/branch targets
typedef struct {
    char name[32];
    uint16_t address;
} label_t;

// Global arrays
uint32_t instructions[MAX_INSTRUCTIONS];
label_t labels[MAX_LABELS];
int instruction_count = 0;
int label_count = 0;

// Instruction lookup table
instruction_info_t instruction_table[] = {
    {"ADD",     OP_ADD,     R_TYPE},
    {"LOAD",    OP_LOAD,    I_TYPE},
    {"STORE",   OP_STORE,   I_TYPE},
    {"ADDI",    OP_ADDI,    I_TYPE},
    {"SUBI",    OP_SUBI,    I_TYPE},
    {"BEQ",     OP_BEQ,     I_TYPE},
    {"BGE",     OP_BGE,     I_TYPE},
    {"JUMP",    OP_JUMP,    J_TYPE},
    {"END",     OP_END,     SPECIAL}
};

// Function prototypes
instruction_info_t* find_instruction(const char* mnemonic);
uint32_t encode_r_type(uint8_t opcode, uint8_t rd, uint8_t rs1, uint8_t rs2);
uint32_t encode_i_type(uint8_t opcode, uint8_t rs, uint8_t rt, uint16_t immediate);
uint32_t encode_j_type(uint8_t opcode, uint16_t address);
int parse_register(const char* reg_str);
int add_label(const char* name, uint16_t address);
int find_label(const char* name);
void compile_file(const char* input_file, const char* output_file);

// Find instruction info by mnemonic
instruction_info_t* find_instruction(const char* mnemonic) {
    int table_size = sizeof(instruction_table) / sizeof(instruction_info_t);
    for (int i = 0; i < table_size; i++) {
        if (strcmp(instruction_table[i].mnemonic, mnemonic) == 0) {
            return &instruction_table[i];
        }
    }
    return NULL;
}

// Encode R-type instruction
uint32_t encode_r_type(uint8_t opcode, uint8_t rd, uint8_t rs1, uint8_t rs2) {
    return ((uint32_t)opcode << 26) | ((uint32_t)rd << 21) | ((uint32_t)rs1 << 16) | 
           ((uint32_t)rs2 << 11);
}

// Encode I-type instruction
uint32_t encode_i_type(uint8_t opcode, uint8_t rs, uint8_t rt, uint16_t immediate) {
    return ((uint32_t)opcode << 26) | ((uint32_t)rs << 21) | ((uint32_t)rt << 16) | immediate;
}

// Encode J-type instruction
uint32_t encode_j_type(uint8_t opcode, uint16_t address) {
    return ((uint32_t)opcode << 26) | address;
}

// Clean argument string
void clean_arg(char* arg) {
    int len = strlen(arg);
    while (len > 0 && (arg[len-1] == ',' || arg[len-1] == ' ' || arg[len-1] == '\t')) {
        arg[len-1] = '\0';
        len--;
    }
}

// Parse register string (R0, R1, $r0, $zero, etc.) to register number
int parse_register(const char* reg_str) {
    // Create clean copy of the register string
    char clean_reg[32];
    strcpy(clean_reg, reg_str);
    clean_arg(clean_reg);
    
    // Handle $zero register (maps to R0)
    if (strcmp(clean_reg, "$zero") == 0) {
        return 0;
    }
    
    // Handle $r0-$r7 format
    if (clean_reg[0] == '$' && (clean_reg[1] == 'r' || clean_reg[1] == 'R')) {
        int reg_num = atoi(&clean_reg[2]);
        if (reg_num >= 0 && reg_num <= 7) {
            return reg_num;
        }
    }
    
    // Handle R0-R7 format
    if (clean_reg[0] == 'R' || clean_reg[0] == 'r') {
        int reg_num = atoi(&clean_reg[1]);
        if (reg_num >= 0 && reg_num <= 7) {
            return reg_num;
        }
    }
    
    return -1; // Invalid register
}

// Add label to label table
int add_label(const char* name, uint16_t address) {
    if (label_count >= MAX_LABELS) return -1;
    strcpy(labels[label_count].name, name);
    labels[label_count].address = address;
    label_count++;
    return 0;
}

// Find label address by name
int find_label(const char* name) {
    for (int i = 0; i < label_count; i++) {
        if (strcmp(labels[i].name, name) == 0) {
            return labels[i].address;
        }
    }
    return -1; // Label not found
}

// Main compilation function
void compile_file(const char* input_file, const char* output_file) {
    FILE* input = fopen(input_file, "r");
    if (!input) {
        printf("Error: Cannot open input file %s\n", input_file);
        return;
    }
    
    char line[MAX_LINE_LENGTH];
    int line_number = 0;
    
    // First pass: collect labels
    while (fgets(line, sizeof(line), input)) {
        line_number++;
        
        // Remove newline and comments
        char* comment = strchr(line, ';');
        if (comment) *comment = '\0';
        comment = strchr(line, '#');
        if (comment) *comment = '\0';
        
        // Check for label
        char* colon = strchr(line, ':');
        if (colon) {
            *colon = '\0';
            // Remove leading/trailing whitespace
            char label_name[32];
            sscanf(line, "%s", label_name);
            add_label(label_name, instruction_count);
            continue;
        }
        
        // Skip empty lines
        char temp[MAX_LINE_LENGTH];
        if (sscanf(line, "%s", temp) != 1) continue;
        
        instruction_count++;
    }
    
    // Reset for second pass
    rewind(input);
    instruction_count = 0;
    line_number = 0;
    
    // Second pass: generate instructions
    while (fgets(line, sizeof(line), input)) {
        line_number++;
        
        // Remove newline and comments
        char* comment = strchr(line, ';');
        if (comment) *comment = '\0';
        comment = strchr(line, '#');
        if (comment) *comment = '\0';
        
        // Skip labels
        if (strchr(line, ':')) continue;
        
        char mnemonic[10], arg1[32], arg2[32], arg3[32], arg4[32];
        int args = sscanf(line, "%s %s %s %s %s", mnemonic, arg1, arg2, arg3, arg4);
        
        if (args < 1) continue; // Empty line
        
        instruction_info_t* instr = find_instruction(mnemonic);
        if (!instr) {
            printf("Error: Unknown instruction '%s' at line %d\n", mnemonic, line_number);
            continue;
        }
        
        uint32_t encoded = 0;
        
        switch (instr->type) {
            case SPECIAL: // END instruction
                encoded = 0; // All zeros
                break;
                
            case R_TYPE: // ADD
                if (args < 4) {
                    printf("Error: R-type instruction needs 3 registers at line %d\n", line_number);
                    continue;
                }
                int rd = parse_register(arg1);
                int rs1 = parse_register(arg2);
                int rs2 = parse_register(arg3);
                
                if (rd < 0 || rs1 < 0 || rs2 < 0) {
                    printf("Error: Invalid register at line %d\n", line_number);
                    continue;
                }
                
                encoded = encode_r_type(instr->opcode, rd, rs1, rs2);
                break;
                
            case I_TYPE: // LOAD, STORE, ADDI, BEQ, SUBI, BGE
                if (args < 3) {
                    printf("Error: I-type instruction needs at least 2 arguments at line %d\n", line_number);
                    continue;
                }
                
                int rs = parse_register(arg2);
                int rt = parse_register(arg1);
                int immediate = 0;
                
                // Handle different I-type formats
                if (strcmp(mnemonic, "LOAD") == 0 || strcmp(mnemonic, "STORE") == 0) {
                    // Format: LOAD R1, 100(R2) or STORE R1, 100(R2)
                    char* paren = strchr(arg2, '(');
                    if (paren) {
                        *paren = '\0';
                        immediate = atoi(arg2);
                        rs = parse_register(paren + 1);
                        char* close_paren = strchr(paren + 1, ')');
                        if (close_paren) *close_paren = '\0';
                    } else {
                        immediate = atoi(arg2);
                        rs = 0; // Default to R0
                    }
                } else if (strcmp(mnemonic, "BEQ") == 0 || strcmp(mnemonic, "BGE") == 0) {
                    // Format: BEQ R1, R2, label or BGE R1, R2, label
                    if (args >= 4) {
                        rs = parse_register(arg1);
                        rt = parse_register(arg2);
                        int label_addr = find_label(arg3);
                        immediate = (label_addr >= 0) ? label_addr - instruction_count - 1 : atoi(arg3);
                    } else {
                        rs = parse_register(arg1);
                        rt = 0; // Compare with R0 for single register format
                        int label_addr = find_label(arg2);
                        immediate = (label_addr >= 0) ? label_addr - instruction_count - 1 : atoi(arg2);
                    }
                } else {
                    // ADDI, SUBI: Format: ADDI R1, R2, immediate
                    rs = parse_register(arg2);
                    rt = parse_register(arg1);
                    immediate = atoi(arg3);
                }
                
                if (rs < 0 || rt < 0) {
                    printf("Error: Invalid register at line %d\n", line_number);
                    continue;
                }
                
                encoded = encode_i_type(instr->opcode, rs, rt, immediate & 0xFFFF);
                break;
                
            case J_TYPE: // JUMP
                if (args < 2) {
                    printf("Error: J-type instruction needs address at line %d\n", line_number);
                    continue;
                }
                
                int target_addr = find_label(arg1);
                if (target_addr < 0) target_addr = atoi(arg1);
                
                encoded = encode_j_type(instr->opcode, target_addr & 0xFFFF);
                break;
        }
        
        instructions[instruction_count++] = encoded;
        printf("0x%04X: 0x%08X  ; %s\n", instruction_count - 1, encoded, line);
    }
    
    fclose(input);
    
    // Write binary output
    FILE* output = fopen(output_file, "wb");
    if (output) {
        fwrite(instructions, sizeof(uint32_t), instruction_count, output);
        fclose(output);
    } else {
        printf("Error: Cannot create output file %s\n", output_file);
    }
}

int main(int argc, char const *argv[]) {
    if (argc != 3) {
        printf("Usage: %s <input.asm> <output.bin>\n", argv[0]);
        return 1;
    }
    
    printf("Compiling %s to %s...\n", argv[1], argv[2]);
    compile_file(argv[1], argv[2]);
    printf("Compilation complete. Generated %d instructions.\n", instruction_count);
    
    return 0;
}
