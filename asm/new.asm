ADDI $r1, $r0, 255
ADDI $r2, $r0, 20       ; skip top border
ADDI $r3, $r0, 380      ; skip bottom border
BEQ $r2, $r3, 8         ; change later (jump to end)
ADDI $r2, $r2, 400
STORE $r1, 0($r2)
SUBI $r2, $r2, 399      ; store value at address r2 and incremented by 1.
JUMP 3
END