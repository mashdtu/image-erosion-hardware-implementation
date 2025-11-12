addiu   $s1,    $zero,  255             # s1 = constant 255
addiu   $s7,    $zero,  20              # s7 = constant 20
addiu   $s6,    $zero,  760             # s6 = constant 400 + 400 - 20 - 20
addiu   $s2,    $zero,  21              # skip top border
addiu   $s3,    $zero,  379             # skip bottom border

main_loop:
beq     $s2,    $s3,    end             # jump to end if inside border
lw      $s4,    0($s2)                  # load pixel colour as s4
beq     $s4,    $zero,  increment       # jump to increment if black
addiu   $s5,    $s2,    -20             # set s5 to pixel above
lw      $s4,    0($s5)                  # load above pixel colour as s4
beq     $s4,    $zero,  increment       # jump to increment if black
addiu   $s5,    $s2,    20              # set s5 to pixel below
lw      $s4,    0($s5)                  # load below pixel colour as s4
beq     $s4,    $zero,  increment       # jump to increment if black
addiu   $s5,    $s2,    -1              # set s5 to pixel left
lw      $s4,    0($s5)                  # load left pixel colour as s4
beq     $s4,    $zero,  increment       # jump to increment if black
addiu   $s5,    $s2,    1               # set s5 to pixel right
lw      $s4,    0($s5)                  # load right pixel colour as s4
beq     $s4,    $zero,  increment       # jump to increment if black
addiu   $s2,    $s2,    400
sw      $s1,    0($s2)
addiu   $s2,    $s2,    -399            # store value at address s2 and increment by 1.
j       main_loop

increment:
addiu   $s2,    $s2,    1               # increment by 1
j       main_loop

end:
addiu   $s1,    $zero,  400             # set all vertical corners to black
addiu   $s1,    $s1,    19
sw      $zero,  0($s1)                  # make left side black
addiu   $s1,    $s1,    1
sw      $zero,  0($s1)                  # make right side black
bge     $s6,    $s1,    27
nop