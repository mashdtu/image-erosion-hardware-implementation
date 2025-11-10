import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    // Define the module interface here (inputs/outputs)
    val inputA = Input(UInt(32.W))        // First operand
    val inputB = Input(UInt(32.W))        // Second operand
    val ALUOp = Input(UInt(2.W))          // 2-bit control signal for different operations
    
    // Outputs
    val result = Output(UInt(32.W))       // ALU result
    val zero = Output(Bool())             // Zero flag for branch instructions
  })

  //Implement this module here
  
  // ALU Control Signals
  // 00: ADD
  // 01: SUB
  // 10: LESS THAN
  // 11: EQUAL
  
  val aluResult = Wire(UInt(32.W))
  
  // Default aluResult value to ensure it's always initialized
  aluResult := 0.U

  switch(io.ALUOp) {
    is(0.U) {         // ADD
      aluResult := io.inputA + io.inputB
    }
    is(1.U) {         // SUB
      aluResult := io.inputA - io.inputB
    }
    is(2.U) {         // GREATER THAN OR EQ (for BGE)
      aluResult := Mux(io.inputA.asSInt >= io.inputB.asSInt, 1.U, 0.U)
    }
    is(3.U) {         // EQUAL (for BEQ)
      aluResult := Mux(io.inputA === io.inputB, 1.U, 0.U)
    }
  }
  
  
  // Connect outputs
  io.result := aluResult
  io.zero := (aluResult === 0.U)        // Zero flag for BEQ and BGE
}