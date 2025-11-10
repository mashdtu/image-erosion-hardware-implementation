ADDI $r1, $r0, 255      ; r1 = constant 255
ADDI $r7, $r0, 20       ; r7 = constant 20
ADDI $r6, $r0, 760      ; r6 = 400 - 20 - 19
ADDI $r2, $r0, 21       ; skip top border
ADDI $r3, $r0, 379      ; skip bottom border
BEQ $r2, $r3, 26        ; jump to end if inside border
LOAD $r4, 0($r2)        ; load pixel colour as r4
BEQ $r4, $r0, 24        ; jump to increment if black
SUBI $r5, $r2, 20       ; set r5 to pixel above
LOAD $r4, 0($r5)        ; load above pixel colour as r4
BEQ $r4, $r0, 24        ; jump to increment if black
ADDI $r5, $r2, 20       ; set r5 to pixel below
LOAD $r4, 0($r5)        ; load below pixel colour as r4
BEQ $r4, $r0, 24        ; jump to increment if black
SUBI $r5, $r2, 1        ; set r5 to pixel left
LOAD $r4, 0($r5)        ; load left pixel colour as r4
BEQ $r4, $r0, 24        ; jump to increment if black
ADDI $r5, $r2, 1        ; set r5 to pixel right
LOAD $r4, 0($r5)        ; load right pixel colour as r4
BEQ $r4, $r0, 24        ; jump to increment if black
ADDI $r2, $r2, 400
STORE $r1, 0($r2)
SUBI $r2, $r2, 399      ; stored value at address r2 and incremented by 1.
JUMP 5
ADDI $r2, $r2, 1        ; increment by 1
JUMP 5
ADDI $r1, $r0, 400      ; set all vertical corners to black
ADDI $r1, $r1, 19
STORE $r0, 0($r1)       ; make left side black
ADDI $r1, $r1, 1
STORE $r0, 0($r1)       ; make right side black
BGE $r6, $r1, 27
END