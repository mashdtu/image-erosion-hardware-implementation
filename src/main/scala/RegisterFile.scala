import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    
    // Read ports
    val readRegister1 = Input(UInt(5.W))      // First read register address (rs)
    val readRegister2 = Input(UInt(5.W))      // Second read register address (rt)
    val readData1 = Output(UInt(32.W))        // First read data output
    val readData2 = Output(UInt(32.W))        // Second read data output
    
    // Write ports
    val writeRegister = Input(UInt(5.W))      // Write register address (rd)
    val writeData = Input(UInt(32.W))         // Write data input

    // Control signals
    val ctrl_RegWrite = Input(Bool())         // Write enable signal
  })

  //Implement this module here
  
  // Create 8 registers, each 32 bits wide
  // Register 0 ($r0) is always 0 and cannot be written to
  val registers = RegInit(VecInit(Seq.fill(8)(0.U(32.W))))
  
  // Read operations (combinational)
  io.readData1 := Mux(io.readRegister1 === 0.U, 0.U, registers(io.readRegister1))
  io.readData2 := Mux(io.readRegister2 === 0.U, 0.U, registers(io.readRegister2))
  
  // Write operation (sequential)
  // Cannot write to register 0 ($r0)
  when(io.ctrl_RegWrite && (io.writeRegister =/= 0.U)) {
    registers(io.writeRegister) := io.writeData
    printf("Register %d written with value: %d\n", io.writeRegister, io.writeData)
  }
}