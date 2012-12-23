/*
 * NewtonRaphson.java - approximares roots of a real number using
 * the Newton-Raphson method.
 * 
 * code by: Tim Duffy
 * <timmahd@bu.edu>
 */

import java.util.*;
import static java.lang.Math.pow;

public class NewtonRaphson {
  
  public static float approximate(int k, float n, float x0, int iter){
    double guess = x0;
    double root = k;
    
    //base case: iter is 1
    if(iter == 1){
      return (float)(guess - ((pow(guess, root) - n) / (root * pow(guess, root - 1))));
    }
    
    //recursive case
    guess = approximate(k, n, x0, iter -1);
    return (float) (guess - ((pow(guess, root) - n) / (root * pow(guess, root - 1))));
  }
    
    
    
  public static void main (String[] args){
    
    Scanner console = new Scanner(System.in);
    int k, iter;
    float n, x0;
    float root;
    String runAgain;
    
    do{
      
      System.out.print("Enter a non-negative number: ");
      n = console.nextInt();
      
      if(n < 0){
        System.out.print("Number must be non-negative!");
        break;
      }
      
      System.out.print("Which root would you like to approximate?(1,2,3...): ");
      k = console.nextInt();
      
      System.out.print("How many times should Newtons method be applied?: ");
      iter = console.nextInt();
      
      System.out.print("Initial guess?: ");
      x0 = console.nextInt();
      
      root = approximate(k, n, x0, iter);
      
      System.out.print("The " + k + " root of " + n + " is " + root + ".");
      System.out.println();
      System.out.println();
      System.out.print("Would you like to run again? (y/n): ");
      runAgain = console.next();
      
    }while(runAgain.equalsIgnoreCase("y"));
  }
}
      
      