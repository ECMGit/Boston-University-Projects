/**
 * Stringmanipulator.java
 * Computer Science 111, Boston University
 * 
 * coded by: Tim Duffy <timmahd@bu.edu> 
 * 
 * This program manipulates a string given by the user
 * in various ways. It then removes a user specified
 * substring from a user specified string.
 */

import java.util.*;

public class StringManipulator{
  
  public static void printWithSpaces(String str1) {
    System.out.println();
    System.out.println("Here is the string with spaces inserted:");
    
    // Add a space between each character
    for(int i = 0; i < str1.length(); i++){
      System.out.print(str1.charAt(i) + " ");
    }
    System.out.println();
  }
  
  public static void printStreched(String str1) {
    System.out.println();
    System.out.println("Here is a streched version of the string:");
    
    // Run outer loop once for each character
    for(int i = 1; i <= str1.length(); i++){
      // Run inner loop to print the characters
      for(int a = 0; a < i; a ++){
        System.out.print(str1.charAt(i-1)); // Print character at index(i-1), i times.
      }
    }
    System.out.println();
  }
  
  public static char middleChar (String str1) { 
    int len = str1.length();
    char mid;
    
    // if length is an even number of characters
    if (len % 2 == 0) {
      mid = str1.charAt(((str1.length() +1)/2) -1);
    }
    else { // if length is an odd number of characters
      mid = str1.charAt((len -1)/2);
    }
    return mid;
  }   
    
  public static String removeSubstring(String word, String subStr){
    String subGone = "";      // initilize to an empty string
    int firstOccur = word.indexOf(subStr);  // Find the first occurence of the substring
    
    // If the substing starts at the first character, remove the substring and keep the rest
    if(firstOccur ==0) {
      subGone = word.substring(subStr.length());
    }
    // If the substing starts elsewhere keep everything before and after the substring occurence
    else {
      subGone = word.substring(0, firstOccur) + word.substring(firstOccur + subStr.length());
    }    
    return subGone;
  }
  
  public static void main(String[] args) {
    Scanner console = new Scanner(System.in);
    
    //Get string from user:
    System.out.print("Enter a string: " );
    String str1 = console.nextLine();
    
    printWithSpaces(str1);
    printStreched(str1);
    System.out.println();
    System.out.println("Its middle character is: " + middleChar(str1));
    System.out.println();
    
    // Ask user for a string followed by a substring
    System.out.println("Enter a word, and a substring of that word, seperated by a space: " );
    String word = console.next();
    String subStr = console.next();
    console.nextLine(); // Consume rest of line
    
    System.out.println();
    System.out.println("Removing " + subStr + " from " + word + " gives: " +removeSubstring(word, subStr));
  }
}