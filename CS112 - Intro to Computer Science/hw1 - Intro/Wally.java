/*
 * Wally.java - calculates the number of Wallys after n generations.
 * 
 * code by: Tim Duffy
 * <timmahd@bu.edu
 * 
 * Problem: Consider an organism that reproduces asexually, let us call him Wally (like the
 * Red Sox mascot). It takes a Wally three generations to reach maturity, and a Wally never
 * dies. Upon reaching maturity, a Wally gives birth to another Wally, and keeps giving birth
 * to another Wally every generation thereafter. If in generation 1, we have one newborn Wally
 * that matures and gives birth to another Wally in generation 4, how many Wallies will there
 * be in generation n? Please formulate a recursive and iterative solution to the problem. What
 * is the complexity of each one (in terms of n)? Submit a single Þle called Wally.java with
 * two methods: countWallyRecursive and countWallyIterative. Your main should be in
 * the same Þle, it should repeatedly read an integer n from the console and print out the total
 * number of Wallies in generation n twice (once using each function). The main loop should
 * stop when a negative n is entered. State the complexity of each function in the comments.
 *
 */

import java.util.*;

public class Wally{
  public static void main(String[] args){
    Scanner console = new Scanner(System.in);
    int n;
    int wallyRecursive, wallyIterative;
    
    do{
      System.out.print("Enter a number (negative number to quit): ");
      n = console.nextInt();
      
      if(n >= 0){
        wallyRecursive = countWallyRecursive(n);
        wallyIterative = countWallyIterative(n);
        
        if (wallyRecursive == 1){
          System.out.println("Recursive: There is " + wallyRecursive + " Wally in generation " + n + ".");
        }
        else{
          System.out.println("Recursive: There are " + wallyRecursive + " Wallys in generation " + n + ".");
        }
        
        if (wallyIterative == 1){
          System.out.println("Iterative: There is " + wallyIterative + " Wally in generation " + n + ".");
        }
        else{
          System.out.println("Iterative: There are " + wallyIterative + " Wallys in generation " + n + ".");
        }
      }
      
    }while(n >= 0);
  }
  
  
  /* 
   * This function calls itself recursively to calculate the number of Wallys after n generations,
   * where n is the user input.
   * 
   * This method has a complexity of Big-Theta(2^n) because each level of the call stack doubles
   */
  
  public static int countWallyRecursive(int n){
    
    //base case 1
    if(n == 0){
      return 0;
    }
    
    //base case 2
    if(n < 4){
      return 1;
    }
    
    //recursive case
    return countWallyRecursive(n-1) + countWallyRecursive(n - 3);
  }
  
   /* 
   * This function uses loops to calculate the number of Wallys after n generations,
   * where n is the user input.
   * 
   * This method has a complexity of O(n).
   */
  public static int countWallyIterative(int n){
    if(n == 0){
      return 0;
    }
    
    int[] arr = new int[n];
    
    for(int i = 0; i <= n - 1; i++){
      if(i < 3){
        arr[i] = 1;
      }
      else{
        arr[i] = arr[i-1] + arr[i-3];
      }
    }
    return arr[n-1];
  }
}