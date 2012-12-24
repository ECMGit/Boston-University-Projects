/*
 * NewtonRaphson.java - approximares roots of a real number using
 * the Newton-Raphson method.
 * 
 * code by: Tim Duffy
 * <timmahd@bu.edu>
 * 
 * Problem: Approximating Roots
 * The Newton-Raphson method can be used to approximate the roots of a function
 * (the roots of f (x) are the values x where f (x) is zero). We start by taking a guess x0 , and
 * then improving it by considering the point where the tangent to f (x) at x0 intersects the
 * x-axis, let us call this point x1 . x1 will be closer to the root, and we can ﬁnd x2 by ﬁnding
 * the intersection with the x-axis of the tangent to the curve at x1 , and so on. It is easy to
 * see that given Xn,  X(n+1) = X(n) ? ( f(Xn)/ f'(xn) )
 * 
 * How is this useful for ﬁnding roots of real numbers? Consider that ﬁnding the kth root of
 * a non-negative real number n is equivalent to ﬁnding the root of f (x) = xk ? n. Your job
 * is to write a recursive function, which given k, n, x0 and iter, computes an approximation
 * of the kth root of n by performing iter steps of Newton-Raphson starting at x0 . Submit
 * a single ﬁle called NewtonRaphson.java which has a main containing a loop asking the
 * user for arguments, and calls a recursive function ﬂoat approximate (int k, ﬂoat n,
 * ﬂoat x0, int iter) that returns the approximation. The program should terminate when
 * a negative k is input. You can read more about the Newton-Raphson method here: http://en.wikipedia.org/wiki/Newton’s_method.
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
      
      