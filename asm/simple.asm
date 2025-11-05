ADDI    $r0,        $zero,      0           # 1. Define first pixel address (0)
JUMP    7                                   # 2. Jump to instruction 7 (main_loop)

ADD     $r5,        $r0,        $zero       # 3. Copy pixel register in temporary $r5 register
ADDI    $r5,        $r5,        400         # 4. Add 400 to the current pixel (copy to output image)
STORE   $zero,      0($r5)                  # 5. Make the current pixel black in the output image

ADDI    $r0,        $r0,        1           # 6. Increment pixel memory address to next pixel

ADDI    $r5,        $zero,      399         # 7. Set temporary register $r5 to 0x18F = 399
BEQ     $r0,        $r5,        41          # 8. If pixel is not in 0 - 399, jump to instruction 41 (end)

LOAD    $r1,        0($r0)                  # 9. Load the new pixel colour
BEQ     $r1,        $zero,      3           # 10. If pixel is black, jump to instruction 3 (make_black)

ADDI    $r5,        $zero,      0x13        # 11. Set register $r2 to 0x13 = 19 
BGE     $r5,        $r2,        3           # 12. If pixel is on top border, jump to instruction 3 (make_black)
ADDI    $r2,        $zero,      0x17B       # 13. Set register $r2 to 0x17B = 379
BGE     $r2,        $r5,        3           # 14. If pixel is on top border, jump to instruction 3 (make_black)

ADD     $r3,        $r0,        $zero       # 15. Copy pixel index
SUBI    $r3,        $r3,        20          # 16. Subtract 20 (0x14) from remainder
BGE     $r3,        $zero,      16          # 17. If still >= 0, keep looping
ADDI    $r3,        $r3,        20          # 18. If below 0, add 20 back -> remainder
BEQ     $r3,        $zero,      3           # 19. If pixel is on left border, jump to instruction 3 (make_black)
ADDI    $r2,        $zero,      19          # 20. Set register $r2 to 0x14 = 19        
BEQ     $r3,        $r2,        3           # 21. If pixel is on right border, jump to instruction 3 (make_black)

ADD     $r4,        $r0,        $zero       # 22. Define another pixel
SUBI    $r4,        $r4,        1           # 23. Select pixel to the left
LOAD    $r1,        0($r4)                  # 24. If left pixel is black, make current black and skip to next
BEQ     $r1,        $zero,      3           # 25. 
ADDI    $r4,        $r4,        0x2         # 26. Select pixel to the right
LOAD    $r1,        0($r4)                  # 27. If right pixel is black, make current black and skip to next
BEQ     $r1,        $zero,      3           # 28.
SUBI    $r4,        $r4,        1           # 29. Select pixel above
SUBI    $r4,        $r4,        20          # 30. If above pixel is black, make current black and skip to next
LOAD    $r1,        0($r4)                  # 31.
BEQ     $r1,        $zero,      3           # 32.
ADDI    $r4,        $r4,        0x28        # 33. Select pixel below
LOAD    $r1,        0($r4)                  # 34. If below pixel is black, make current black and skip to next
BEQ     $r1,        $zero,      3           # 35. 
                                            # If all neighbour pixels are white, make current white
ADD     $r5,        $r0,        $zero       # 36. Copy pixel register in a temporary $r5 register
ADDI    $r5,        $r5,        0x190       # 37. Add 400 to the current pixel (copy to output image)
ADDI    $r6,        $zero,      0x1         # 38. Define colour of the pixel (white)
STORE   $r6,        0($r5)                  # 39. Make the current pixel white in the output image

JUMP    6                                   # 40. Jump back to instruction 6 (increment)

END                                         # 41. End/halt program


