ADDI $r1, $r0, 10       # test alu
SUBI $r1, $r1, 5        # test alu
BEQ $r1, $r0, 3         # test alu
BEQ $r1, $r1, 4         # test alu
BGE $r0, $r1, 5         # test alu
BGE $r1, $r1, 6         # test alu
LOAD $r2, 0($r1)        # test data memory
STORE $r1, 0($r1)       # test data memory
JUMP 9                  # test cputop
END                     # test cputop