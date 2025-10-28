import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    
    // Read ports
    val readAddress1 = Input(UInt(5.W))      // First read register address (rs)
    val readAddress2 = Input(UInt(5.W))      // Second read register address (rt)
    val readData1 = Output(UInt(32.W))       // First read data output
    val readData2 = Output(UInt(32.W))       // Second read data output
    
    // Write port
    val writeAddress = Input(UInt(5.W))      // Write register address (rd)
    val writeData = Input(UInt(32.W))        // Write data input
    val writeEnable = Input(Bool())          // Write enable signal
  })

  //Implement this module here
  
  // Create 32 registers, each 32 bits wide (MIPS standard)
  // Register 0 ($zero) is always 0 and cannot be written to
  val registers = Reg(Vec(32, UInt(32.W)))
  
  // Initialize all registers to 0
  for (i <- 0 until 32) {
    registers(i) := 0.U
  }
  
  // Read operations (combinational)
  // Register 0 always reads as 0
  io.readData1 := Mux(io.readAddress1 === 0.U, 0.U, registers(io.readAddress1))
  io.readData2 := Mux(io.readAddress2 === 0.U, 0.U, registers(io.readAddress2))
  
  // Write operation (sequential)
  // Cannot write to register 0 ($zero)
  when(io.writeEnable && (io.writeAddress =/= 0.U)) {
    registers(io.writeAddress) := io.writeData
  }

}