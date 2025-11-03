addiu   $s0,        $zero,      0x0
j       0x0040001C
addu    $t0,        $s0,        $zero
addi    $t0,        $t0,        0x190
sll     $t0,        $t0,        0x2
sw      $zero,      0x0($t0)
addi    $s0,        $s0,        0x1
addiu   $t0,        $zero,      0x190
beq     $s0,        $t0,        0x004000D0
lw      $s1,        0x0($s0)
beq     $s1,        $zero,      0x00400018
addiu   $s2,        $zero,      0x13
slt     $t0,        $s2,        $s0
beq     $t0,        $zero,      0x00400008
addiu   $s2,        $zero,      0x17B
slt     $t0,        $s0,        $s2
beq     $t0,        $zero,      0x00400008
addu    $s3,        $s0,        $zero
addi    $s3,        $s3,        -0x14
bgez    $s3,        0x00400060
addi    $s3,        $s3,         0x14
beq     $s3,        $zero,      0x00400008
addiu   $s2,        $zero,      0x13
beq     $s3,        $s2,        0x00400008
addu    $s4,        $s0,        $zero
addi    $s4,        $s4,        -0x1
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x00400008
addi    $s4,        $s4,        0x2
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x00400008
addi    $s4,        $s4,        -0x1
addi    $s4,        $s4,        -0x14
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x00400008
addi    $s4,        $s4,        0x28
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x00400008
addu    $t0,        $s0,        $zero
addi    $t0,        $t0,        0x190
sll     $t0,        $t0,        0x2
addiu   $t1,        $zero,      0x1
sw      $t1,        0x0($t0)
j       0x00400018
nop
j		0x004000D0



