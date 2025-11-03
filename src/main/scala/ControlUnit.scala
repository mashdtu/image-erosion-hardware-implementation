import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    
    // Input: Instruction from program memory
    val instruction = Input(UInt(32.W))
    
    // Outputs to other CPU components
    // Register File Control
    val regWrite = Output(Bool())           // Enable writing to register file
    val regDst = Output(Bool())             // Select destination register (rd vs rt)
    
    // ALU Control
    val aluSrc = Output(Bool())             // Select ALU source B (register vs immediate)
    val aluControl = Output(UInt(4.W))      // ALU operation control
    
    // Memory Control
    val memRead = Output(Bool())            // Enable memory read (load instructions)
    val memWrite = Output(Bool())           // Enable memory write (store instructions)
    val memToReg = Output(Bool())           // Select register write data (ALU result vs memory)
    
    // Program Counter Control
    val branch = Output(Bool())             // Branch signal
    val jump = Output(Bool())               // Jump signal
    val pcSrc = Output(Bool())              // PC source select (branch vs normal increment)
    val halt = Output(Bool())               // Halt signal for END instruction
    
    // ALU Zero flag input (for branch decisions)
    val aluZero = Input(Bool())
    // ALU Negative flag input (for BNNE - Branch if Non-Negative)
    val aluNegative = Input(Bool())
  })

  //Implement this module here
  
  // Extract instruction fields based on assignment format
  val opcode = io.instruction(31, 26)     // Bits 31-26: Opcode (6 bits)
  val rd = io.instruction(25, 21)         // Bits 25-21: Destination register (5 bits) - R-type
  val rs1 = io.instruction(20, 16)        // Bits 20-16: Source register 1 (5 bits) - R-type
  val rs2 = io.instruction(15, 11)        // Bits 15-11: Source register 2 (5 bits) - R-type
  val shamt = io.instruction(10, 6)       // Bits 10-6: Shift amount (5 bits) - R-type
  
  // I-type fields
  val rs = io.instruction(25, 21)         // Bits 25-21: Source register (5 bits) - I-type
  val rt = io.instruction(20, 16)         // Bits 20-16: Target register (5 bits) - I-type  
  val immediate = io.instruction(15, 0)   // Bits 15-0: Immediate value (16 bits) - I-type
  
  // J-type fields
  val address = io.instruction(15, 0)     // Bits 15-0: Target address (16 bits) - J-type
  
  // Assignment 2 Instruction Opcodes
  val OP_END     = "b000000".U  // END - Terminate execution
  val OP_ADD     = "b010000".U  // ADD - R-type
  val OP_BSL     = "b010001".U  // BSL (Bitshift Left) - R-type
  val OP_LOAD    = "b100000".U  // LOAD - I-type
  val OP_STORE   = "b100001".U  // STORE - I-type
  val OP_ADDI    = "b100010".U  // ADDI - I-type
  val OP_BEQ     = "b100011".U  // BEQ - I-type
  val OP_SUBI    = "b100100".U  // SUBI - I-type
  val OP_BNNE    = "b100101".U  // BNNE (Branch if Non-Negative) - I-type
  val OP_BRANCH  = "b110000".U  // BRANCH (Unconditional) - J-type
  
  // Default control signals
  io.regWrite := false.B
  io.regDst := false.B
  io.aluSrc := false.B
  io.aluControl := "b0000".U
  io.memRead := false.B
  io.memWrite := false.B
  io.memToReg := false.B
  io.branch := false.B
  io.jump := false.B
  io.pcSrc := false.B
  io.halt := false.B
  
  // Main control logic based on opcode
  switch(opcode) {
    is(OP_END) {
      // END - Terminate execution
      io.halt := true.B
      // All other signals remain at default (false)
    }
    
    is(OP_ADD) {
      // ADD - R-type (rd = rs1 + rs2)
      io.regWrite := true.B
      io.regDst := true.B        // Write to rd register
      io.aluSrc := false.B       // Use register for ALU input B
      io.aluControl := "b0010".U // ADD operation
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_BSL) {
      // BSL - R-type Bitshift Left (rd = rs1 << shamt)
      io.regWrite := true.B
      io.regDst := true.B        // Write to rd register
      io.aluSrc := false.B       // Use shift amount (treated as register input)
      io.aluControl := "b1000".U // Shift Left Logical
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_LOAD) {
      // LOAD - I-type (rt = memory[rs + immediate])
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register
      io.aluSrc := true.B        // Use immediate for address calculation
      io.aluControl := "b0010".U // ADD for address calculation
      io.memRead := true.B
      io.memToReg := true.B      // Write memory data to register
    }
    
    is(OP_STORE) {
      // STORE - I-type (memory[rs + immediate] = rt)
      io.regWrite := false.B     // Don't write to register
      io.aluSrc := true.B        // Use immediate for address calculation
      io.aluControl := "b0010".U // ADD for address calculation
      io.memWrite := true.B
    }
    
    is(OP_ADDI) {
      // ADDI - I-type (rt = rs + immediate)
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register
      io.aluSrc := true.B        // Use immediate value
      io.aluControl := "b0010".U // ADD
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_BEQ) {
      // BEQ - I-type (if rs == rt then PC = PC + 4 + immediate)
      io.regWrite := false.B
      io.aluSrc := false.B       // Compare two registers
      io.aluControl := "b0110".U // SUB for comparison
      io.branch := true.B
      io.pcSrc := io.aluZero     // Branch if ALU result is zero
    }
    
    is(OP_SUBI) {
      // SUBI - I-type (rt = rs - immediate)
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register
      io.aluSrc := true.B        // Use immediate value
      io.aluControl := "b0110".U // SUB
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_BNNE) {
      // BNNE - I-type Branch if Non-Negative (if rs >= 0 then PC = PC + 4 + immediate)
      io.regWrite := false.B
      io.aluSrc := true.B        // Compare register with zero (immediate = 0)
      io.aluControl := "b0110".U // SUB for comparison with zero
      io.branch := true.B
      io.pcSrc := !io.aluNegative // Branch if ALU result is non-negative
    }
    
    is(OP_BRANCH) {
      // BRANCH - J-type Unconditional branch (PC = address)
      io.regWrite := false.B
      io.jump := true.B
    }
  }

}