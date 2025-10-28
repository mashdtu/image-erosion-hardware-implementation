import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val aluControl = Input(UInt(4.W))  // 4-bit control signal for different operations
    val result = Output(UInt(32.W))
    val zero = Output(Bool())          // Zero flag for branch instructions
  })

  //Implement this module here
  
  // ALU Control Signal Definitions (MIPS I instruction set)
  // 0000: AND
  // 0001: OR
  // 0010: ADD
  // 0011: XOR
  // 0100: NOR
  // 0101: Reserved
  // 0110: SUB (Subtract)
  // 0111: SLT (Set Less Than)
  // 1000: SLL (Shift Left Logical)
  // 1001: SRL (Shift Right Logical)
  // 1010: SRA (Shift Right Arithmetic)
  // 1011-1111: Reserved for future use
  
  val aluResult = Wire(UInt(32.W))
  
  // MIPS I ALU Operations
  switch(io.aluControl) {
    is("b0000".U) { // AND
      aluResult := io.operandA & io.operandB
    }
    is("b0001".U) { // OR
      aluResult := io.operandA | io.operandB
    }
    is("b0010".U) { // ADD
      aluResult := io.operandA + io.operandB
    }
    is("b0011".U) { // XOR
      aluResult := io.operandA ^ io.operandB
    }
    is("b0100".U) { // NOR
      aluResult := ~(io.operandA | io.operandB)
    }
    is("b0110".U) { // SUB (Subtract)
      aluResult := io.operandA - io.operandB
    }
    is("b0111".U) { // SLT (Set Less Than)
      aluResult := Mux(io.operandA.asSInt < io.operandB.asSInt, 1.U, 0.U)
    }
    is("b1000".U) { // SLL (Shift Left Logical)
      // Use lower 5 bits of operandB as shift amount (MIPS standard)
      aluResult := io.operandA << io.operandB(4, 0)
    }
    is("b1001".U) { // SRL (Shift Right Logical)
      // Use lower 5 bits of operandB as shift amount
      aluResult := io.operandA >> io.operandB(4, 0)
    }
    is("b1010".U) { // SRA (Shift Right Arithmetic)
      // Use lower 5 bits of operandB as shift amount
      aluResult := (io.operandA.asSInt >> io.operandB(4, 0)).asUInt
    }
  }.otherwise {
    // Default case for undefined operations
    aluResult := 0.U
  }
  
  // Connect outputs
  io.result := aluResult
  io.zero := (aluResult === 0.U)  // Zero flag is true when result is zero
}