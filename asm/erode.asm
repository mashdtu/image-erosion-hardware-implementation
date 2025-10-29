# General rules:
# Jump to instruction 4 (0x0040000C): make pixel black and skip to next pixel
# Jump to instruction 7 (0x00400018): do nothing and skip to next pixel

li      $s0,        0x0                         # Define first pixel address (0)
j       0x0040001C                              # Jump to instruction 8 (skip next 4 instructions)

                                                # Make pixel black

move    $t0,        $s0                         # (Instruction 4) Copy pixel register in temporary $t0 register
addi    $t0,        $t0,        0x190           # Add 400 to the current pixel (copy to output image)
sll     $t0,        $t0,        2               # Multiply by 4 bytes (32 bits) as we store in 32 bit slots
sw      $zero,      0x0($t0)                    # Make the current pixel black in the output image

                                                # Check if pixel is positioned on a border

addi    $s0,        $s0,        0x1             # (Instruction 7) Increment pixel memory address to next pixel
lw      $s1,        0x0($s0)                    # (Instruction 8) Load the new pixel colour
beq     $s1,        $zero,      0x00400018      # If pixel is black, jump to instruction 7

li      $s2,        0x13                        # Set register $s2 to 0x13 = 19 
slt     $t0,        $s2,        $s0             # Compare border and pixel values with slt
beq     $t0,        $zero,      0x0040000C      # If pixel is on top border, jump to instruction 4

li      $s2,        0x17B                       # Set register $s2 to 0x17B = 379 
slt     $t0,        $s0,        $s2             # Compare border and pixel values with slt
beq     $t0,        $zero,      0x0040000C      # If pixel is on top border, jump to instruction 4

move    $s3,        $s0                         # Modulus method (no division)
addi    $s3,        $s3,        -0x14           # Subtract 20 from the remainder
bgez    $s3,        0x0040003C                  # Jump back 1 instruction if the result is above 0
addi    $s3,        $s3,        0x14            # If result below 0, add back 20, result is now remainder
beq     $s3,        $zero,      0x0040000C      # If pixel is on left border, jump to instruction 4
li      $s2,        0x13                        # Set register $s2 to 0x14 = 19        
beq     $s3,        $s2,        0x0040000C      # If pixel is on right border, jump to instruction 4

                                                # Code to do actual erosion on non-border pixels

addu    $s4,        $s0,        $zero           # Define another pixel
addi    $s4,        $s4,        -0x1            # Select pixel to the left
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x0040000C      # If left pixel is black, make current black and skip to next

addi    $s4         $s4,        0x2             # Select pixel to the right
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x0040000C      # If right pixel is black, make current black and skip to next

addi    $s4,        $s4,        -0x1            # Select pixel above
addi    $s4,        $s4,        -0x14
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x0040000C      # If above pixel is black, make current black and skip to next

addi    $s4,        $s4,        0x28            # Select pixel below
lw      $s1,        0x0($s4)
beq     $s1,        $zero,      0x0040000C      # If above pixel is black, make current black and skip to next

                                                # If all neighbour pixels are white, make current white
move    $t0,        $s0                         # Copy pixel register in a temporary $t0 register
addi    $t0,        $t0,        0x190           # Add 400 to the current pixel (copy to output image)
sll     $t0,        $t0,        0x2             # Multiply by 4 bytes (32 bits) as we store in 32 bit slots
li      $t1,        0x1                         # Define colour of the pixel (white)
sw      $t1,        0x0($t0)                    # Make the current pixel white in the output image

li      $t0,        0x18F                       # Set temporary register $t0 to 0x18F = 399
bne     $s0,        $t0,        0x00400018      # For all pixels 0 - 399, jump back to instruction 7



