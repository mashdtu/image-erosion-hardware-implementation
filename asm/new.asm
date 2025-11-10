ADDI $r1, $r0, 255
ADDI $r2, $r0, 20       ; skip top border
ADDI $r3, $r0, 380      ; skip bottom border
BEQ $r2, $r3, 12        ; jump to end if inside border
LOAD $r4, 0($r2)        ; load pixel colour as r4
BEQ $r4, $r0, 10        ; jump to end if black
ADDI $r2, $r2, 400
STORE $r1, 0($r2)
SUBI $r2, $r2, 399      ; stored value at address r2 and incremented by 1.
JUMP 3
ADDI $r2, $r2, 1        ; increment by 1
JUMP 3
END