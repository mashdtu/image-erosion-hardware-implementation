#include <stdio.h>
#include <stdint.h>
#include <string.h>

// Instruction opcodes
#define OP_ADD      0b010000
#define OP_LOAD     0b100000
#define OP_STORE    0b100001
#define OP_ADDI     0b100010
#define OP_SUBI     0b100011
#define OP_BEQ      0b100100
#define OP_BGE      0b100101
#define OP_JUMP     0b110000
#define OP_END      0b000000

const char* get_opcode_name(uint8_t opcode) {
    switch(opcode) {
        case OP_ADD:  return "ADD";
        case OP_LOAD: return "LOAD";
        case OP_STORE: return "STORE";
        case OP_ADDI: return "ADDI";
        case OP_SUBI: return "SUBI";
        case OP_BEQ:  return "BEQ";
        case OP_BGE:  return "BGE";
        case OP_JUMP: return "JUMP";
        case OP_END:  return "END";
        default: return "UNKNOWN";
    }
}

void disassemble_instruction(uint32_t instr, int address) {
    uint8_t opcode = (instr >> 26) & 0x3F;
    
    printf("0x%04X: 0x%08X  ", address, instr);
    
    switch(opcode) {
        case OP_ADD: {
            uint8_t rd = (instr >> 21) & 0x1F;
            uint8_t rs1 = (instr >> 16) & 0x1F;
            uint8_t rs2 = (instr >> 11) & 0x1F;
            uint8_t shamt = (instr >> 6) & 0x1F;
            printf("ADD  $r%d, $r%d, $r%d", rd, rs1, rs2);
            if (shamt > 0) printf(", %d", shamt);
            break;
        }
        case OP_LOAD: {
            uint8_t rs = (instr >> 21) & 0x1F;
            uint8_t rt = (instr >> 16) & 0x1F;
            uint16_t imm = instr & 0xFFFF;
            printf("LOAD $r%d, %d($r%d)", rt, imm, rs);
            break;
        }
        case OP_STORE: {
            uint8_t rs = (instr >> 21) & 0x1F;
            uint8_t rt = (instr >> 16) & 0x1F;
            uint16_t imm = instr & 0xFFFF;
            printf("STORE $r%d, %d($r%d)", rt, imm, rs);
            break;
        }
        case OP_ADDI: {
            uint8_t rs = (instr >> 21) & 0x1F;
            uint8_t rt = (instr >> 16) & 0x1F;
            uint16_t imm = instr & 0xFFFF;
            printf("ADDI $r%d, $r%d, %d", rt, rs, imm);
            break;
        }
        case OP_SUBI: {
            uint8_t rs = (instr >> 21) & 0x1F;
            uint8_t rt = (instr >> 16) & 0x1F;
            uint16_t imm = instr & 0xFFFF;
            printf("SUBI $r%d, $r%d, %d", rt, rs, imm);
            break;
        }
        case OP_BEQ: {
            uint8_t rs = (instr >> 21) & 0x1F;
            uint8_t rt = (instr >> 16) & 0x1F;
            int16_t offset = (int16_t)(instr & 0xFFFF);
            printf("BEQ  $r%d, $r%d, %d (-> 0x%04X)", rs, rt, offset, address + 1 + offset);
            break;
        }
        case OP_BGE: {
            uint8_t rs = (instr >> 21) & 0x1F;
            uint8_t rt = (instr >> 16) & 0x1F;
            int16_t offset = (int16_t)(instr & 0xFFFF);
            printf("BGE  $r%d, $r%d, %d (-> 0x%04X)", rs, rt, offset, address + 1 + offset);
            break;
        }
        case OP_JUMP: {
            uint16_t addr = instr & 0xFFFF;
            printf("JUMP 0x%04X", addr);
            break;
        }
        case OP_END: {
            printf("END");
            break;
        }
        default: {
            printf("UNKNOWN (opcode: 0x%02X)", opcode);
            break;
        }
    }
    printf("\n");
}

int main(int argc, char* argv[]) {
    if (argc != 2) {
        printf("Usage: %s <binary_file>\n", argv[0]);
        return 1;
    }
    
    FILE* file = fopen(argv[1], "rb");
    if (!file) {
        printf("Error: Cannot open file %s\n", argv[1]);
        return 1;
    }
    
    uint32_t instruction;
    int address = 0;
    
    printf("Disassembly of %s:\n", argv[1]);
    printf("========================================\n");
    
    while (fread(&instruction, sizeof(uint32_t), 1, file) == 1) {
        disassemble_instruction(instruction, address);
        address++;
    }
    
    fclose(file);
    return 0;
}