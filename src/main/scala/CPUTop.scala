import chisel3._
import chisel3.util._

class CPUTop extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool ())
    val run = Input(Bool ())
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerDataMemEnable = Input(Bool ())
    val testerDataMemAddress = Input(UInt (16.W))
    val testerDataMemDataRead = Output(UInt (32.W))
    val testerDataMemWriteEnable = Input(Bool ())
    val testerDataMemDataWrite = Input(UInt (32.W))
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerProgMemEnable = Input(Bool ())
    val testerProgMemAddress = Input(UInt (16.W))
    val testerProgMemDataRead = Output(UInt (32.W))
    val testerProgMemWriteEnable = Input(Bool ())
    val testerProgMemDataWrite = Input(UInt (32.W))
  })

  //Creating components
  val programCounter = Module(new ProgramCounter())
  val dataMemory = Module(new DataMemory())
  val programMemory = Module(new ProgramMemory())
  val registerFile = Module(new RegisterFile())
  val controlUnit = Module(new ControlUnit())
  val alu = Module(new ALU())

  //Connecting the modules according to the diagram
  
  // Instruction fetch PC to Program Memory
  programCounter.io.run := io.run
  programMemory.io.address := programCounter.io.programCounter
  val instruction = programMemory.io.instructionRead
  
  // Control Unit decode instruction and generate control signals
  controlUnit.io.instruction := instruction
  controlUnit.io.aluZero := alu.io.zero
  
  // Extract instruction fields
  val rs = instruction(25, 21)      // Read register 1
  val rt = instruction(20, 16)      // Read register 2 
  val rd = instruction(15, 11)      // Write register
  val immediate = instruction(15, 0) // Immediate
  
  // Register File connections
  registerFile.io.readRegister1 := rt(4, 0)
  registerFile.io.readRegister2 := rs(4, 0)
  registerFile.io.ctrl_RegWrite := controlUnit.io.regWrite
  
  // RegDst MUX Select write register (0=rt, 1=rd)
  val writeRegister = Mux(controlUnit.io.regDst, rd(4, 0), rt(4, 0))
  registerFile.io.writeRegister := writeRegister
  
  // ALU connections
  alu.io.inputA := registerFile.io.readData2
  
  // ALUSrc MUX Select ALU input B (0=register, 1=immediate)
  val signExtendedImmediate = Cat(Fill(16, immediate(15)), immediate) // Sign extend
  alu.io.inputB := Mux(controlUnit.io.aluSrc, signExtendedImmediate, registerFile.io.readData1)
  alu.io.ALUOp := controlUnit.io.aluOp
  
  // Data Memory connections
  dataMemory.io.address := alu.io.result(15, 0)  // ALU result as address
  dataMemory.io.dataWrite := registerFile.io.readData1
  dataMemory.io.writeEnable := controlUnit.io.memWrite
  
  // MemToReg MUX Select register write data (0=ALU result, 1=memory data)  
  val writeData = Mux(controlUnit.io.memToReg, dataMemory.io.dataRead, alu.io.result)
  registerFile.io.writeData := writeData
  
  // Program Counter control
  programCounter.io.stop := controlUnit.io.halt
  programCounter.io.jump := controlUnit.io.jump
  programCounter.io.programCounterJump := immediate  // Jump address from instruction
  
  // Branch control
  val pcPlusOne = programCounter.io.programCounter + 1.U
  val branchTarget = immediate
  // Branch is true if both branch and Zero are true.
  programCounter.io.branch := (controlUnit.io.branch & alu.io.zero)
  programCounter.io.branchAddress := branchTarget
  
  // CPU done signal END instruction sets this
  io.done := controlUnit.io.halt

  //This signals are used by the tester for loading the program to the program memory, do not touch
  programMemory.io.testerAddress := io.testerProgMemAddress
  io.testerProgMemDataRead := programMemory.io.testerDataRead
  programMemory.io.testerDataWrite := io.testerProgMemDataWrite
  programMemory.io.testerEnable := io.testerProgMemEnable
  programMemory.io.testerWriteEnable := io.testerProgMemWriteEnable
  //This signals are used by the tester for loading and dumping the data memory content, do not touch
  dataMemory.io.testerAddress := io.testerDataMemAddress
  io.testerDataMemDataRead := dataMemory.io.testerDataRead
  dataMemory.io.testerDataWrite := io.testerDataMemDataWrite
  dataMemory.io.testerEnable := io.testerDataMemEnable
  dataMemory.io.testerWriteEnable := io.testerDataMemWriteEnable
}