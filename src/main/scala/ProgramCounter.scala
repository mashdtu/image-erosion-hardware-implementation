import chisel3._
import chisel3.util._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val stop = Input(Bool())
    val jump = Input(Bool())
    val run = Input(Bool())
    val programCounterJump = Input(UInt(16.W))
    val programCounter = Output(UInt(16.W))
    
    // Add branch support
    val branch = Input(Bool())
    val branchAddress = Input(UInt(16.W))
  })
  
  // Program counter register initialized to 0
  val programCounterReg = RegInit(0.U(16.W))
  
  // Next program counter value
  val programCounterNext = Wire(UInt(16.W))
  
  when(!io.run) {
    // When not running, hold current value
    programCounterNext := programCounterReg
  }.elsewhen(io.stop) {
    // When stop is asserted, hold current value
    programCounterNext := programCounterReg
  }.elsewhen(io.jump) {
    // When jump is asserted, jump to specified address
    programCounterNext := io.programCounterJump
  }.elsewhen(io.branch) {
    // When branch is asserted, branch to specified address
    programCounterNext := io.branchAddress
  }.otherwise {
    // Otherwise increment by 1
    programCounterNext := programCounterReg + 1.U
  }
  
  // Update the register on each clock cycle
  programCounterReg := programCounterNext
  
  // Connect the output
  io.programCounter := programCounterReg
}