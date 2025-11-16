import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val inputA = Input(UInt(32.W))        // First operand
    val inputB = Input(UInt(32.W))        // Second operand
    val ALUOp = Input(UInt(2.W))          // Control signal
    
    // Outputs
    val result = Output(UInt(32.W))       // ALU result
    val zero = Output(Bool())             // Zero flag for branch instructions
  })

  // 00: ADD
  // 01: SUB
  // 10: LESS THAN
  // 11: EQUAL
  
  val aluResult = Wire(UInt(32.W))
  
  aluResult := 0.U

  switch(io.ALUOp) {
    is(0.U) {       // ADD
      aluResult := io.inputA + io.inputB
    }
    is(1.U) {       // SUB
      aluResult := io.inputA - io.inputB
    }
    is(2.U) {       // BGE
      aluResult := Mux(io.inputA.asSInt >= io.inputB.asSInt, 0.U, 1.U)
      // note aluResult = 0 if A >= B
    }
    is(3.U) {       // BEQ
      aluResult := Mux(io.inputA === io.inputB, 0.U, 1.U)
      // note aluResult = 0 if A = B
    }
  }
  
  
  // Connect outputs
  io.result := aluResult
  io.zero := (aluResult === 0.U)        // Zero flag for BEQ and BGE
}