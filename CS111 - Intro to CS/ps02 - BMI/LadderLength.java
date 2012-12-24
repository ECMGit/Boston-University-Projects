/**
 * LadderLength.java
 * Computer Science 111, Boston University
 * 
 * base code provided by the course staff
 * 
 * algorithm implemented by: Tim Duffy <timmahd@bu.edu>
 * 
 * This program computes the necessary length of a ladder, given
 * the height of the point to be reached and the angle at which the
 * ladder will be positioned.
 */

import java.util.*;

public class LadderLength {
    public static void main(String[] args) {
        int height;     // the height to be reached in feet
        int angle;      // the angle at which the ladder will be positioned
        
        // Read the values from the user.
        Scanner console = new Scanner(System.in);
        System.out.print("Enter the height to the nearest foot: ");
        height = console.nextInt();
        System.out.print("Enter the angle to the nearest degree: ");
        angle = console.nextInt();
        
        double rads, length, yards, feet;                // Declare variables with type double
        
        rads = (angle / 180.0) * 3.14159265358979323846; // Convert degrees to radians
        length = height / (Math.sin(rads));              // Compute length of ladder
        yards = length / 3.0;                            // Calculate yards
        feet = length % 3;                               // Calculate the extra number of feet
        
        // Output the results
        System.out.println("The required length is:");
        System.out.println("    " + length + " feet.");
        System.out.println("    " + yards + " yards.");
        System.out.println("    " + (int)yards + " yards and " + feet + " feet.");
        System.out.println();

    }
}
