/**
 * ComputeBMI.java
 * Computer Science 111, Boston University
 * 
 * base code provided by the course staff
 * 
 * algorithm implemented by: Tim Duffy <timmahd@bu.edu> 
 * 
 * This program computes a person's body mass index (BMI)
 * from the person's height and weight.
 */

import java.util.*;

public class ComputeBMI {
    public static void main(String[] args) {
        int weight;         // the person's weight in pounds
        int height;         // the person's height in inches

        // Read the values from the user.
        Scanner console = new Scanner(System.in);
        System.out.print("Enter the person's weight (to the nearest pound): ");
        weight = console.nextInt();
        System.out.print("Enter the person's height (to the nearest inch): ");
        height = console.nextInt();
        
        
        /*
         * The lines above read the weight and height from the user
         * and store them in the variables weight and height.
         * Fill in the rest of the program below, using those
         * variables to compute and print the BMI.
         */
        
        double BMI;
        BMI = (weight * 720.0) / (height * height);      // Compute body mass index and store the value in the variable BMI
        System.out.print("Your BMI is " + BMI + " lbs/in^2");   // Output BMI
        System.out.println();

    }
}
