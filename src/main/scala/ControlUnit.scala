import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    
    // Input: Instruction from program memory
    val instruction = Input(UInt(32.W))
    
    // Outputs matching the diagram exactly
    val regDst = Output(Bool())             // RegDst - Select destination register 
    val jump = Output(Bool())               // Jump - Unconditional jump signal
    val branch = Output(Bool())             // Branch - Branch signal
    val memRead = Output(Bool())            // MemRead - Memory read enable
    val memToReg = Output(Bool())           // MemToReg - Select write data source
    val aluOp = Output(UInt(2.W))           // ALUOp - ALU operation select (2-bit)
    val memWrite = Output(Bool())           // MemWrite - Memory write enable
    val aluSrc = Output(Bool())             // ALUSrc - Select ALU input B source
    val regWrite = Output(Bool())           // RegWrite - Register write enable
    val halt = Output(Bool())               // END signal - Halt execution
    
    // ALU Zero flag input (for branch decisions)
    val aluZero = Input(Bool())
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
  
  // Assignment 2 Instruction Opcodes (matching assembler)
  val OP_END     = "b000000".U  // END - Terminate execution
  val OP_ADD     = "b010000".U  // ADD - R-type
  val OP_LOAD    = "b100000".U  // LOAD - I-type
  val OP_STORE   = "b100001".U  // STORE - I-type
  val OP_ADDI    = "b100010".U  // ADDI - I-type
  val OP_SUBI    = "b100011".U  // SUBI - I-type
  val OP_BEQ     = "b100100".U  // BEQ - I-type
  val OP_BGE     = "b100101".U  // BGE (Branch Greater or Equal) - I-type
  val OP_JUMP    = "b110000".U  // JUMP (Unconditional) - J-type
  
  // Default control signals
  io.regDst := false.B
  io.jump := false.B
  io.branch := false.B
  io.memRead := false.B
  io.memToReg := false.B
  io.aluOp := 0.U(2.W)
  io.memWrite := false.B
  io.aluSrc := false.B
  io.regWrite := false.B
  io.halt := false.B
  
  // Main control logic based on opcode - matching diagram exactly
  switch(opcode) {
    is(OP_END) {
      // END - Terminate execution
      io.halt := true.B
      // All other signals remain at default (false)
    }
    
    is(OP_ADD) {
      // ADD - R-type (rd = rs1 + rs2)
      io.regDst := true.B        // 1: Write to rd register (R-type)
      io.jump := false.B         // 0: No jump
      io.branch := false.B       // 0: No branch
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Write ALU result to register
      io.aluOp := 0.U(2.W)       // 00: ADD operation
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := false.B       // 0: Use register for ALU input B
      io.regWrite := true.B      // 1: Enable register write
    }
    
    is(OP_LOAD) {
      // LOAD - I-type (rt = memory[rs + immediate])
      io.regDst := false.B       // 0: Write to rt register (I-type)
      io.jump := false.B         // 0: No jump
      io.branch := false.B       // 0: No branch
      io.memRead := true.B       // 1: Memory read
      io.memToReg := true.B      // 1: Write memory data to register
      io.aluOp := 0.U(2.W)       // 00: ADD for address calculation
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := true.B        // 1: Use immediate for address calculation
      io.regWrite := true.B      // 1: Enable register write
    }
    
    is(OP_STORE) {
      // STORE - I-type (memory[rs + immediate] = rt)
      io.regDst := false.B       // 0: Don't care (no register write)
      io.jump := false.B         // 0: No jump
      io.branch := false.B       // 0: No branch
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Don't care (no register write)
      io.aluOp := 0.U(2.W)       // 00: ADD for address calculation
      io.memWrite := true.B      // 1: Memory write
      io.aluSrc := true.B        // 1: Use immediate for address calculation
      io.regWrite := false.B     // 0: No register write
    }
    
    is(OP_ADDI) {
      // ADDI - I-type (rt = rs + immediate)
      io.regDst := false.B       // 0: Write to rt register (I-type)
      io.jump := false.B         // 0: No jump
      io.branch := false.B       // 0: No branch
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Write ALU result to register
      io.aluOp := 0.U(2.W)       // 00: ADD operation
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := true.B        // 1: Use immediate value
      io.regWrite := true.B      // 1: Enable register write
    }
    
    is(OP_SUBI) {
      // SUBI - I-type (rt = rs - immediate)
      io.regDst := false.B       // 0: Write to rt register (I-type)
      io.jump := false.B         // 0: No jump
      io.branch := false.B       // 0: No branch
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Write ALU result to register
      io.aluOp := 1.U(2.W)       // 01: SUB operation
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := true.B        // 1: Use immediate value
      io.regWrite := true.B      // 1: Enable register write
    }
    
    is(OP_BEQ) {
      // BEQ - I-type (if rs == rt then branch)
      io.regDst := false.B       // 0: Don't care (no register write)
      io.jump := false.B         // 0: No jump
      io.branch := io.aluZero    // Branch if ALU result is zero (equal)
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Don't care (no register write)
      io.aluOp := 1.U(2.W)       // 01: SUB for comparison
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := false.B       // 0: Compare two registers
      io.regWrite := false.B     // 0: No register write
    }
    
    is(OP_BGE) {
      // BGE - I-type (if rs >= rt then branch)
      io.regDst := false.B       // 0: Don't care (no register write)
      io.jump := false.B         // 0: No jump
      io.branch := !io.aluZero   // Branch if ALU result is 1 (rs >= rt)
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Don't care (no register write)
      io.aluOp := 2.U(2.W)       // 10: Less than or equal comparison (rs <= rt)
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := false.B       // 0: Compare two registers
      io.regWrite := false.B     // 0: No register write
    }
    
    is(OP_JUMP) {
      // JUMP - J-type (unconditional jump)
      io.regDst := false.B       // 0: Don't care (no register write)
      io.jump := true.B          // 1: Unconditional jump
      io.branch := false.B       // 0: No conditional branch
      io.memRead := false.B      // 0: No memory read
      io.memToReg := false.B     // 0: Don't care (no register write)
      io.aluOp := 0.U(2.W)       // 00: Don't care
      io.memWrite := false.B     // 0: No memory write
      io.aluSrc := false.B       // 0: Don't care
      io.regWrite := false.B     // 0: No register write
    }
  }

}