ADDI $r1, $r0, 255
ADDI $r2, $r0, 20       ; skip top border
ADDI $r3, $r0, 380      ; skip bottom border
BEQ $r2, $r3, 24        ; jump to end if inside border
LOAD $r4, 0($r2)        ; load pixel colour as r4
BEQ $r4, $r0, 22        ; jump to increment if black
SUBI $r5, $r2, 20       ; set r5 to pixel above
LOAD $r4, 0($r5)        ; load above pixel colour as r4
BEQ $r4, $r0, 22        ; jump to increment if black
ADDI $r5, $r2, 20       ; set r5 to pixel below
LOAD $r4, 0($r5)        ; load above pixel colour as r4
BEQ $r4, $r0, 22        ; jump to increment if black
SUBI $r5, $r2, 1        ; set r5 to pixel left
LOAD $r4, 0($r5)        ; load above pixel colour as r4
BEQ $r4, $r0, 22        ; jump to increment if black
ADDI $r5, $r2, 1        ; set r5 to pixel right
LOAD $r4, 0($r5)        ; load above pixel colour as r4
BEQ $r4, $r0, 22        ; jump to increment if black
ADDI $r2, $r2, 400
STORE $r1, 0($r2)
SUBI $r2, $r2, 399      ; stored value at address r2 and incremented by 1.
JUMP 3
ADDI $r2, $r2, 1        ; increment by 1
JUMP 3
END