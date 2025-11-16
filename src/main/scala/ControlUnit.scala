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
    val aluOp = Output(UInt(2.W))           // ALUOp - ALU operation select
    val memWrite = Output(Bool())           // MemWrite - Memory write enable
    val aluSrc = Output(Bool())             // ALUSrc - Select ALU input B source
    val regWrite = Output(Bool())           // RegWrite - Register write enable
    val halt = Output(Bool())               // END signal - Halt execution
    
    // ALU Zero flag input (for branch decisions)
    val aluZero = Input(Bool())
  })

  val opcode = io.instruction(31, 26)
  
  // Define opcodes
  val OP_LOAD   = "b100000".U   // LOAD
  val OP_STORE  = "b100001".U   // STORE
  val OP_ADDI   = "b100010".U   // ADDI
  val OP_SUBI   = "b100011".U   // SUBI
  val OP_BEQ    = "b100100".U   // BEQ
  val OP_BGE    = "b100101".U   // BGE
  val OP_JUMP   = "b110000".U   // JUMP
  val OP_END    = "b000000".U   // END
  
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
  
  // Set control signals
  switch(opcode) {
    is(OP_END) {
      // END
      io.halt := true.B
    }    
    is(OP_LOAD) {
      // LOAD
      io.regDst := false.B       // write to rt register
      io.jump := false.B         // no jump
      io.branch := false.B       // no branch
      io.memRead := true.B       // memory read
      io.memToReg := true.B      // write memory data to register
      io.aluOp := 0.U(2.W)       // ADD for address calculation
      io.memWrite := false.B     // no memory write
      io.aluSrc := true.B        // use immediate for address calculation
      io.regWrite := true.B      // enable register write
    }
    
    is(OP_STORE) {
      // STORE
      io.regDst := false.B       // no register write
      io.jump := false.B         // no jump
      io.branch := false.B       // no branch
      io.memRead := false.B      // no memory read
      io.memToReg := false.B     // no register write
      io.aluOp := 0.U(2.W)       // ADD for address calculation
      io.memWrite := true.B      // memory write
      io.aluSrc := true.B        // use immediate for address calculation
      io.regWrite := false.B     // no register write
    }
    
    is(OP_ADDI) {
      // ADDI
      io.regDst := false.B       // write to rt register
      io.jump := false.B         // no jump
      io.branch := false.B       // no branch
      io.memRead := false.B      // no memory read
      io.memToReg := false.B     // write ALU result to register
      io.aluOp := 0.U(2.W)       // ADD operation
      io.memWrite := false.B     // no memory write
      io.aluSrc := true.B        // use immediate value
      io.regWrite := true.B      // enable register write
    }
    
    is(OP_SUBI) {
      // SUBI
      io.regDst := false.B       // write to rt register
      io.jump := false.B         // no jump
      io.branch := false.B       // no branch
      io.memRead := false.B      // no memory read
      io.memToReg := false.B     // write ALU result to register
      io.aluOp := 1.U(2.W)       // SUB operation
      io.memWrite := false.B     // no memory write
      io.aluSrc := true.B        // use immediate value
      io.regWrite := true.B      // enable register write
    }
    
    is(OP_BEQ) {
      // BEQ
      io.regDst := false.B       // no register write
      io.jump := false.B         // no jump
      io.branch := true.B        // branch is true
      io.memRead := false.B      // no memory read
      io.memToReg := false.B     // no register write
      io.aluOp := 3.U(2.W)       // BEQ for comparison
      io.memWrite := false.B     // no memory write
      io.aluSrc := false.B       // compare two registers
      io.regWrite := false.B     // no register write
    }
    
    is(OP_BGE) {
      // BGE
      io.regDst := false.B       // no register write
      io.jump := false.B         // no jump
      io.branch := true.B        // branch is true
      io.memRead := false.B      // no memory read
      io.memToReg := false.B     // no register write
      io.aluOp := 2.U(2.W)       // less than or equal comparison (rs <= rt)
      io.memWrite := false.B     // no memory write
      io.aluSrc := false.B       // compare two registers
      io.regWrite := false.B     // no register write
    }
    
    is(OP_JUMP) {
      // JUMP
      io.regDst := false.B       // no register write
      io.jump := true.B          // unconditional jump
      io.branch := false.B       // not conditional branch
      io.memRead := false.B      // no memory read
      io.memToReg := false.B     // no register write
      io.aluOp := 0.U(2.W)       // no ALU
      io.memWrite := false.B     // no memory write
      io.aluSrc := false.B       // no ALU
      io.regWrite := false.B     // no register write
    }
  }

}