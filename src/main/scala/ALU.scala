import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    val inputA = Input(UInt(32.W))        // First operand
    val inputB = Input(UInt(32.W))        // Second operand
    val aluControl = Input(UInt(4.W))     // 4-bit control signal for different operations
    val shamt = Input(UInt(5.W))          // Shift amount for BSL instruction
    
    // Outputs
    val result = Output(UInt(32.W))       // ALU result
    val zero = Output(Bool())             // Zero flag for branch instructions
    val negative = Output(Bool())         // Negative flag for BNNE instruction
    val overflow = Output(Bool())         // Overflow flag for arithmetic operations
  })

  //Implement this module here
  
  // ALU Control Signal Definitions for Assignment 2 Instructions
  // 0000: AND
  // 0001: OR
  // 0010: ADD (for ADD, ADDI, LOAD, STORE address calculation)
  // 0011: XOR
  // 0100: NOR
  // 0101: Reserved
  // 0110: SUB (for SUBI, BEQ comparison, BNNE comparison)
  // 0111: SLT (Set Less Than)
  // 1000: SLL (Shift Left Logical for BSL)
  // 1001: SRL (Shift Right Logical)
  // 1010: SRA (Shift Right Arithmetic)
  // 1011-1111: Reserved for future use
  
  val aluResult = Wire(UInt(32.W))
  
  // Default output values
  io.result := 0.U
  io.zero := false.B
  io.negative := false.B
  io.overflow := false.B
  
  // ALU Operations for Assignment 2
  switch(io.aluControl) {
    is("b0000".U) { // AND (Bitwise AND)
      aluResult := io.inputA & io.inputB
    }
    
    is("b0001".U) { // OR (Bitwise OR)
      aluResult := io.inputA | io.inputB
    }
    
    is("b0010".U) { // ADD (Addition)
      // Used for: ADD, ADDI, LOAD/STORE address calculation
      val sum = io.inputA +& io.inputB  // +& for width expansion to detect overflow
      aluResult := sum(31, 0)
      // Set overflow flag for signed arithmetic
      val aPos = !io.inputA(31)  // A is positive
      val bPos = !io.inputB(31)  // B is positive  
      val rNeg = sum(31)         // Result is negative
      io.overflow := (aPos && bPos && rNeg) || (!aPos && !bPos && !rNeg)
    }
    
    is("b0011".U) { // XOR (Bitwise XOR)
      aluResult := io.inputA ^ io.inputB
    }
    
    is("b0100".U) { // NOR (Bitwise NOR)
      aluResult := ~(io.inputA | io.inputB)
    }
    
    is("b0110".U) { // SUB (Subtraction)
      // Used for: SUBI, BEQ comparison, BNNE comparison
      val diff = io.inputA -& io.inputB  // -& for width expansion to detect overflow
      aluResult := diff(31, 0)
      // Set overflow flag for signed arithmetic
      val aPos = !io.inputA(31)  // A is positive
      val bNeg = io.inputB(31)   // B is negative
      val rNeg = diff(31)        // Result is negative
      io.overflow := (aPos && bNeg && rNeg) || (!aPos && !bNeg && !rNeg)
    }
    
    is("b0111".U) { // SLT (Set Less Than) - signed comparison
      aluResult := Mux(io.inputA.asSInt < io.inputB.asSInt, 1.U, 0.U)
    }
    
    is("b1000".U) { // SLL (Shift Left Logical) - for BSL instruction
      // Use shamt input for shift amount (from instruction encoding)
      aluResult := io.inputA << io.shamt
    }
    
    is("b1001".U) { // SRL (Shift Right Logical)
      aluResult := io.inputA >> io.shamt
    }
    
    is("b1010".U) { // SRA (Shift Right Arithmetic) - sign-extending
      aluResult := (io.inputA.asSInt >> io.shamt).asUInt
    }
    
  }.otherwise {
    // Default case for undefined operations
    aluResult := 0.U
    io.overflow := false.B
  }
  
  // Connect outputs
  io.result := aluResult
  io.zero := (aluResult === 0.U)        // Zero flag for BEQ
  io.negative := aluResult(31)          // Negative flag for BNNE (MSB = sign bit)
}