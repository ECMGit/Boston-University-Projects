/*
 * PartOne.java
 *
 * Computer Science 111, Boston University
 * 
 * A class that contains methods from Part I of PS 8.
 */

public class PartOne {
  
 
  // Prints the first n letters of the alphabet backwards, then forwards
    public static void printSomething(int n) {
        if (n <= 0) {
            return;
        }
        
        System.out.println((char)('a' + n - 1));
        printSomething(n - 1);
        System.out.println((char)('a' + n - 1));
    }
    
    
    public static int mystery(int a, int b) {
        if (a < 0) {
            return 1;
        } else {
            return 2 + mystery(a - b, b);
        }
    }

    public static boolean includesZero(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            // As soon as we find a 0, we can return 0.
            if (arr[i] == 0) {
                return true; 
            }
        }

        // If we make it here, the array must have no zeroes.
        return false;
    }
}
