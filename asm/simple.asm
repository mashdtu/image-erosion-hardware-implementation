ADDI    $r1,        $r0,        0           # 1. Define first pixel address (0)
JUMP    7                                   # 2. Jump to instruction 7 (main_loop)

ADD     $r6,        $r1,        $r0         # 3. Copy pixel register in temporary $r6 register
ADDI    $r6,        $r6,        400         # 4. Add 400 to the current pixel (copy to output image)
STORE   $r0,        $r6                      # 5. Make the current pixel black in the output image

ADDI    $r1,        $r1,        1           # 6. Increment pixel memory address to next pixel

ADDI    $r6,        $r0,        399         # 7. Set temporary register $r6 to 0x18F = 399
BEQ     $r1,        $r6,        41          # 8. If pixel is not in 0 - 399, jump to instruction 41 (end)

LOAD    $r2,        $r1                     # 9. Load the new pixel colour
BEQ     $r2,        $r0,        3           # 10. If pixel is black, jump to instruction 3 (make_black)

ADDI    $r6,        $r0,        19          # 11. Set register $r3 to 19 
BGE     $r6,        $r3,        3           # 12. If pixel is on top border, jump to instruction 3 (make_black)
ADDI    $r3,        $r0,        379         # 13. Set register $r3 to 379
BGE     $r3,        $r6,        3           # 14. If pixel is on top border, jump to instruction 3 (make_black)

ADD     $r4,        $r1,        $r0         # 15. Copy pixel index
SUBI    $r4,        $r4,        20          # 16. Subtract 20 (0x14) from remainder
BGE     $r4,        $r0,        16          # 17. If still >= 0, keep looping
ADDI    $r4,        $r4,        20          # 18. If below 0, add 20 back -> remainder
BEQ     $r4,        $r0,        3           # 19. If pixel is on left border, jump to instruction 3 (make_black)
ADDI    $r3,        $r0,        19          # 20. Set register $r3 to 0x14 = 19        
BEQ     $r4,        $r3,        3           # 21. If pixel is on right border, jump to instruction 3 (make_black)

ADD     $r5,        $r1,        $r0         # 22. Define another pixel
SUBI    $r5,        $r5,        1           # 23. Select pixel to the left
LOAD    $r2,        $r5                     # 24. If left pixel is black, make current black and skip to next
BEQ     $r2,        $r0,        3           # 25. 
ADDI    $r5,        $r5,        2           # 26. Select pixel to the right
LOAD    $r2,        $r5                     # 27. If right pixel is black, make current black and skip to next
BEQ     $r2,        $r0,        3           # 28.
SUBI    $r5,        $r5,        1           # 29. Select pixel above
SUBI    $r5,        $r5,        20          # 30. If above pixel is black, make current black and skip to next
LOAD    $r2,        $r5                     # 31.
BEQ     $r2,        $r0,        3           # 32.
ADDI    $r5,        $r5,        40          # 33. Select pixel below
LOAD    $r2,        $r5                     # 34. If below pixel is black, make current black and skip to next
BEQ     $r2,        $r0,        3           # 35. 
                                            # If all neighbour pixels are white, make current white
ADD     $r6,        $r1,        $r0         # 36. Copy pixel register in a temporary $r6 register
ADDI    $r6,        $r6,        400         # 37. Add 400 to the current pixel (copy to output image)
ADDI    $r7,        $r0,        1           # 38. Define colour of the pixel (white)
STORE   $r7,        $r6                     # 39. Make the current pixel white in the output image

JUMP    6                                   # 40. Jump back to instruction 6 (increment)

END                                         # 41. End/halt program
