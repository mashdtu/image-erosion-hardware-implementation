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
    
    // ALU Zero flag input (for branch decisions)
    val aluZero = Input(Bool())
  })

  //Implement this module here
  
  // Extract instruction fields
  val opcode = io.instruction(31, 26)     // Bits 31-26: Opcode
  val rs = io.instruction(25, 21)         // Bits 25-21: Source register 1
  val rt = io.instruction(20, 16)         // Bits 20-16: Source register 2
  val rd = io.instruction(15, 11)         // Bits 15-11: Destination register
  val funct = io.instruction(5, 0)        // Bits 5-0: Function code (for R-type)
  val immediate = io.instruction(15, 0)   // Bits 15-0: Immediate value
  val address = io.instruction(25, 0)     // Bits 25-0: Jump address
  
  // MIPS Instruction Opcodes
  val OP_RTYPE   = "b000000".U  // R-type instructions (add, sub, slt, etc.)
  val OP_LW      = "b100011".U  // Load Word
  val OP_SW      = "b101011".U  // Store Word  
  val OP_BEQ     = "b000100".U  // Branch Equal
  val OP_BNE     = "b000101".U  // Branch Not Equal
  val OP_ADDI    = "b001000".U  // Add Immediate
  val OP_LUI     = "b001111".U  // Load Upper Immediate
  val OP_ORI     = "b001101".U  // OR Immediate
  val OP_J       = "b000010".U  // Jump
  val OP_BGEZ    = "b000001".U  // Branch Greater Equal Zero
  
  // R-type Function Codes
  val FUNCT_ADD  = "b100000".U  // Add
  val FUNCT_SUB  = "b100010".U  // Subtract
  val FUNCT_SLT  = "b101010".U  // Set Less Than
  val FUNCT_AND  = "b100100".U  // AND
  val FUNCT_OR   = "b100101".U  // OR
  val FUNCT_XOR  = "b100110".U  // XOR
  val FUNCT_NOR  = "b100111".U  // NOR
  val FUNCT_SLL  = "b000000".U  // Shift Left Logical
  val FUNCT_SRL  = "b000010".U  // Shift Right Logical
  val FUNCT_SRA  = "b000011".U  // Shift Right Arithmetic
  
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
  
  // Main control logic based on opcode
  switch(opcode) {
    is(OP_RTYPE) {
      // R-type instructions (add, sub, slt, and, or, etc.)
      io.regWrite := true.B
      io.regDst := true.B        // Write to rd register
      io.aluSrc := false.B       // Use register for ALU input B
      io.memToReg := false.B     // Write ALU result to register
      
      // Set ALU control based on function field
      switch(funct) {
        is(FUNCT_ADD) { io.aluControl := "b0010".U }  // ADD
        is(FUNCT_SUB) { io.aluControl := "b0110".U }  // SUB
        is(FUNCT_SLT) { io.aluControl := "b0111".U }  // SLT
        is(FUNCT_AND) { io.aluControl := "b0000".U }  // AND
        is(FUNCT_OR)  { io.aluControl := "b0001".U }  // OR
        is(FUNCT_XOR) { io.aluControl := "b0011".U }  // XOR
        is(FUNCT_NOR) { io.aluControl := "b0100".U }  // NOR
        is(FUNCT_SLL) { io.aluControl := "b1000".U }  // SLL
        is(FUNCT_SRL) { io.aluControl := "b1001".U }  // SRL
        is(FUNCT_SRA) { io.aluControl := "b1010".U }  // SRA
      }
    }
    
    is(OP_LW) {
      // Load Word
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register
      io.aluSrc := true.B        // Use immediate for address calculation
      io.aluControl := "b0010".U // ADD for address calculation
      io.memRead := true.B
      io.memToReg := true.B      // Write memory data to register
    }
    
    is(OP_SW) {
      // Store Word
      io.regWrite := false.B     // Don't write to register
      io.aluSrc := true.B        // Use immediate for address calculation
      io.aluControl := "b0010".U // ADD for address calculation
      io.memWrite := true.B
    }
    
    is(OP_BEQ) {
      // Branch Equal
      io.regWrite := false.B
      io.aluSrc := false.B       // Compare two registers
      io.aluControl := "b0110".U // SUB for comparison
      io.branch := true.B
      io.pcSrc := io.aluZero     // Branch if ALU result is zero
    }
    
    is(OP_BNE) {
      // Branch Not Equal  
      io.regWrite := false.B
      io.aluSrc := false.B       // Compare two registers
      io.aluControl := "b0110".U // SUB for comparison
      io.branch := true.B
      io.pcSrc := !io.aluZero    // Branch if ALU result is not zero
    }
    
    is(OP_ADDI) {
      // Add Immediate
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register
      io.aluSrc := true.B        // Use immediate value
      io.aluControl := "b0010".U // ADD
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_LUI) {
      // Load Upper Immediate (for li instruction implementation)
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register  
      io.aluSrc := true.B        // Use immediate value
      io.aluControl := "b0010".U // ADD (with $zero)
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_ORI) {
      // OR Immediate (used with LUI for li instruction)
      io.regWrite := true.B
      io.regDst := false.B       // Write to rt register
      io.aluSrc := true.B        // Use immediate value
      io.aluControl := "b0001".U // OR
      io.memToReg := false.B     // Write ALU result to register
    }
    
    is(OP_J) {
      // Jump
      io.regWrite := false.B
      io.jump := true.B
    }
    
    is(OP_BGEZ) {
      // Branch Greater Equal Zero
      io.regWrite := false.B
      io.aluSrc := false.B       // Compare register with zero
      io.aluControl := "b0110".U // SUB for comparison
      io.branch := true.B
      // Branch if rs >= 0 (MSB of rs is 0)
      io.pcSrc := !io.instruction(25) // Check sign bit of rs
    }
  }

}