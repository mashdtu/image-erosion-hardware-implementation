ADDI $r1, $r0, 255      ; r1 = constant 255
ADDI $r7, $r0, 20       ; r5 = constant 20
ADDI $r2, $r0, 11       ; skip top border
ADDI $r3, $r0, 89      ; skip bottom border
BEQ $r2, $r3, 31        ; jump to end if inside border
LOAD $r4, 0($r2)        ; load pixel colour as r4
BEQ $r4, $r0, 29        ; jump to increment if black
SUBI $r5, $r2, 10       ; set r5 to pixel above
LOAD $r4, 0($r5)        ; load above pixel colour as r4
BEQ $r4, $r0, 29        ; jump to increment if black
ADDI $r5, $r2, 10       ; set r5 to pixel below
LOAD $r4, 0($r5)        ; load below pixel colour as r4
BEQ $r4, $r0, 29        ; jump to increment if black
SUBI $r5, $r2, 1        ; set r5 to pixel left
LOAD $r4, 0($r5)        ; load left pixel colour as r4
BEQ $r4, $r0, 29        ; jump to increment if black
ADDI $r5, $r2, 1        ; set r5 to pixel right
LOAD $r4, 0($r5)        ; load right pixel colour as r4
BEQ $r4, $r0, 29        ; jump to increment if black
SUBI $r4, $r2, 0
SUBI $r4, $r4, 20
BEQ $r4, $r0, 29        ; skip if inside left border
ADDI $r6, $r0, 19
BEQ $r4, $r6, 29        ; skip if inside right border
BGE $r4, $r5, 20        ; jump back if still above 20
ADDI $r2, $r2, 400
STORE $r1, 0($r2)
SUBI $r2, $r2, 399      ; stored value at address r2 and incremented by 1.
JUMP 4
ADDI $r2, $r2, 1        ; increment by 1
JUMP 4
END