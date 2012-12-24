/* 
 * CS:APP Data Lab 
 * 
 * bits.c - Source file with your solutions to the Lab.
 *          This is the file you will hand in to your instructor.
 *
 */

#include "btest.h"
#include <limits.h>

team_struct team =
{
   /* Team name: */
    "timmahd + davarisg", 
   /* Student name 1: */
   "Tim Duffy       ",
   /* Login ID 1: */
   "timmahd",

   /* Student name 2: Full name of the second team member */
   "Georgios Davaris",
   /* Login ID 2: Login ID of the second team member */
   "davarisg"
};

/// bitNor - ~(x|y) using only ~ and & 
int bitNor(int x, int y) {
  return ~x & ~y;  // Equivalent to ~(x|y) after distributing the ~
}

// bitXor - x^y using only ~ and & 
int bitXor(int x, int y) {
  return ~(~x & ~y) & ~(x & y);  // x^y = (x & ~y) | (~x & y)

}
// isNotEqual - return 0 if x == y, and 1 otherwise 
int isNotEqual(int x, int y) {
  return !!(x ^ y); // Uses !! to check for on bits in x^y
}
// getByte - Extract byte n from word x
//           Bytes numbered from 0 (LSB) to 3 (MSB)
int getByte(int x, int n) {
    x >>= (n << 3); 	// shift word over so bit n is in front
    x = x & 0xFF;   	// apply mask
    return x;
}

// copyLSB - set all bits of result to least significant bit of x
int copyLSB(int x) {
  x <<= 31;           // Puts LSB in front
  return (x >>=31);   // Arithmetic shift keeps the same first bit when shifting
}

// logicalShift - shift x to the right by n, using a logical shift
int logicalShift(int x, int n) {
  int mask = ~(1 << 31); 	// create a number with n zeros, then all 1's
  mask >>= (n+(~0));          // shift over (n-1)

  return ((x >> n) & mask);     // apply mask
}

// bitCount - returns count of number of 1's in word
int bitCount(int x) {
  
  int temp = 0x55 | 0x55 << 8;
  int mask = temp | temp << 16;
  
  /* Apply mask (0101...0101) to pull out bits in odd positions
     Shift x by 1 to count bits in even position */
  x = (x & mask) + ((x >> 1) & mask); // 16 sums of 2 bit portions
  
  temp = 0x33 | 0x33 << 8;
  mask = temp | temp << 16; // mask = (0011...0011)

  // Use the same principle to count bits in first 2 positions
  // Shift to count bits in last 2 positions
  x = (x & mask) + ((x >> 2) & mask); // x is now 8 sums of 4 bit portions
  
  temp = 0x0F | 0x0F << 8;
  mask = temp | temp << 16; // mask = (0000 1111 ... 0000 1111)
  
  // Condense to 4 sums of 8 bit portions
  x = (x & mask) + ((x >> 4) & mask);
  
  mask = 0xFF << 16 | 0xFF; // mask = (0x00FF00FF)
  
  // Condense to 2 sums of 16 bit portions
  x = (x & mask) + ((x >> 8) & mask);
  
  mask = 0xFF | (0xFF << 8); // mask = (0x0000FFFF)
  
  // Finally, condense to 1 sum of a 32 bit portion
  x = (x & mask) + ((x >> 16) & mask);

  return x;
}


// bang - Compute !x without using !
int bang(int x) {
  int signX = x >> 31;
  int signNegX = (~x+1) >> 31;
  
  // x = 0 is the only number that has a positve sign for x and (~x +1)
  return( ~(signX | signNegX)) & 1;
}

// leastBitPos - return a mask that marks the position of the
//               least significant 1 bit. If x == 0, return 0
int leastBitPos(int x) {
  
  // Adding 1 to ~x allows you to keep least significant
  // one bit when AND'd with x
  return  x & (~x+1);

}

// TMax - return maximum two's complement integer 
int tmax(void) {
  return ~(1<<32);  // return the number (011111....1111)
}

// isNonNegative - return 1 if x >= 0, return 0 otherwise 
int isNonNegative(int x) {
  // Put sign bit in LSB position. 
  return (~(x >> 31)) & 0x1; // Applying the mask 0x1 will make all but LSB == 0
}

// isGreater - if x > y  then return 1, else return 0 
int isGreater(int x, int y) {
  
  int signX = x >> 31;
  int signY = y >> 31;
  
  // When signs are equal, and (x - y) is positive, x is larger (return 1)
  int eql = !(signX ^ signY) & ((x + ~y) >> 31);
  
  // When signs are unequal, return 0 when x is positive and y is negative
  // otherwise it will return 1
  int notEql = (signX & !signY);
  
  return !(eql | notEql);

}

// divpwr2 - Compute x/(2^n), for 0 <= n <= 30
//           Round toward zero
int divpwr2(int x, int n) {
  
  // if x is positive, add zero. Otherwise add 
  int frac = ((1<<n) + ~0) & (x >> 31);
  
  return(x + frac) >> n;
}

// abs - absolute value of x (except returns TMin for TMin)
int abs(int x) {
  
  int signX = x>>31;
  return (x ^ signX) + ((~signX) + 1);
}

// addOK - Determine if can compute x+y without overflow
int addOK(int x, int y) {
  
  int sign = (x + y) >> 31;
  int x1 = x >> 31;
  int y1 = y >> 31;

  // overflow if x and y are the same, and sign changes after addition
  return !(~(x1 ^ y1) & (x1 ^ sign));
}
