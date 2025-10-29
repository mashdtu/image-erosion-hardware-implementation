setup:
    li      $s0,        0x0                         # Define first pixel address (0)
    j       main_loop                               # Jump to main_loop (skip make_black and increment)

make_black:                                         # Make pixel black
    move    $t0,        $s0                         # Copy pixel register in temporary $t0 register
    addi    $t0,        $t0,        0x190           # Add 400 to the current pixel (copy to output image)
    sll     $t0,        $t0,        2               # Multiply by 4 bytes (32 bits) as we store in 32 bit slots
    sw      $zero,      0x0($t0)                    # Make the current pixel black in the output image
    
increment:
    addi    $s0,        $s0,        0x1             # Increment pixel memory address to next pixel

main_loop:
    li      $t0,        0x190                       # Set temporary register $t0 to 0x18F = 399
    beq     $s0,        $t0,        end             # If pixel is not in 0 - 399, jump to end

    lw      $s1,        0x0($s0)                    # Load the new pixel colour
    beq     $s1,        $zero,      increment       # If pixel is black, jump to increment

    li      $s2,        0x13                        # Set register $s2 to 0x13 = 19 
    slt     $t0,        $s2,        $s0             # Compare border and pixel values with slt
    beq     $t0,        $zero,      make_black      # If pixel is on top border, jump to make_black
    li      $s2,        0x17B                       # Set register $s2 to 0x17B = 379 
    slt     $t0,        $s0,        $s2             # Compare border and pixel values with slt
    beq     $t0,        $zero,      make_black      # If pixel is on top border, jump to make_black

    move    $s3,        $s0                         # Copy pixel index
mod_loop:
    addi    $s3,        $s3,        -0x14           # Subtract 20 (0x14) from remainder
    bgez    $s3,        mod_loop                    # If still >= 0, keep looping
    addi    $s3,        $s3,         0x14           # If below 0, add 20 back -> remainder

    beq     $s3,        $zero,      make_black      # If pixel is on left border, jump to make_black
    li      $s2,        0x13                        # Set register $s2 to 0x14 = 19        
    beq     $s3,        $s2,        make_black      # If pixel is on right border, jump to make_black

    addu    $s4,        $s0,        $zero           # Define another pixel
    addi    $s4,        $s4,        -0x1            # Select pixel to the left
    lw      $s1,        0x0($s4)
    beq     $s1,        $zero,      make_black      # If left pixel is black, make current black and skip to next

    addi    $s4,        $s4,        0x2             # Select pixel to the right
    lw      $s1,        0x0($s4)
    beq     $s1,        $zero,      make_black      # If right pixel is black, make current black and skip to next

    addi    $s4,        $s4,        -0x1            # Select pixel above
    addi    $s4,        $s4,        -0x14
    lw      $s1,        0x0($s4)
    beq     $s1,        $zero,      make_black      # If above pixel is black, make current black and skip to next

    addi    $s4,        $s4,        0x28            # Select pixel below
    lw      $s1,        0x0($s4)
    beq     $s1,        $zero,      make_black      # If above pixel is black, make current black and skip to next
                                                    # If all neighbour pixels are white, make current white
    move    $t0,        $s0                         # Copy pixel register in a temporary $t0 register
    addi    $t0,        $t0,        0x190           # Add 400 to the current pixel (copy to output image)
    sll     $t0,        $t0,        0x2             # Multiply by 4 bytes (32 bits) as we store in 32 bit slots
    li      $t1,        0x1                         # Define colour of the pixel (white)
    sw      $t1,        0x0($t0)                    # Make the current pixel white in the output image

    j       increment                               # Jump to increment

end:
    nop


