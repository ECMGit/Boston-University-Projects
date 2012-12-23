/* StringRecursion.java
 *
 * starter code by: Computer Science 111, Boston University
 * 
 * modified by: Tim Duffy
 * username: timmahd
 *
 * A class that contains recursive methods that operate on strings.
 */

import java.util.*;

public class StringRecursion {
  /*
   * numOccur - a recursive method that returns the number of times 
   * that the character ch occurs in the String str.
   * 
   * The main method includes two examples of using this method.
   *
   * You can also test this method by entering
   * NumOccur.numOccur(ch, str) -- where ch is replaced by a char
   * and str is replaced by a string -- in the Interactions Pane.
   */
  public static int numOccur(char ch, String str) {
    // base case
    if (str == null || str.equals("")) {
      return 0;
    }
    
    // recursive case
    int numOccurInRest = numOccur(ch, str.substring(1));
    if (ch == str.charAt(0)) {
      return 1 + numOccurInRest;
    } else {
      return numOccurInRest;
    }
  }
  
  // Print a string with spaces in between
  public static String printWithSpaces(String str) {
    String withSpace;
    
    //base case 1
    if(str == null || str.equals("")){
      return "";
    }
    
    //base case 2
    if(str.length() == 1){ // if str is 1 letter, return that letter
      return str + " ";
    }
    
    //recursive case
    return str.charAt(0) + " " + printWithSpaces(str.substring(1)); //recursively call printWithSpaces
  }
  
  // print a string forward and backwards
  public static String printMirrored(String str) {
    //base case 1
    if(str == null || str.equals("")){
      return "";
    }
    
    //base case 2
    if(str.length() <= 1){ // if string length is 1, return it
      return str + str;
    }
    
    //recursive case
    return str.charAt(0) + printMirrored(str.substring(1, str.length())) + str.charAt(0); // recursively call printMirrored()
  }
  
  // returns the middle of a string
  public static String middle(String str) {
    
    //base case 1
    if(str == null || str.equals("")){
      return "";
    }
    
    // base case 2
    if(str.length() == 1){
      return str;
    }
    
    if(str.length() % 2 == 0){ // if string contains an even number of characters
      str = str.substring(0, str.length()-1);
    }
    
    //recursive case
    return middle(str.substring(1, str.length()-1));  // remove the first and last letters
  }
  
  //Turns a string to all cpaital letters
  public static String toUpperCase(String str) {
    char ch = str.charAt(0);
        
   //base case 1
    if(str == null || str.equals("")){
      return "";
    }
    
    //base case 2
    if(str.length() == 1){ // if str is 1 letter, return that letter
      if(ch >= 'a' && ch <= 'z'){
        ch = (char)(ch - 32);
        return (ch) + "";
      }
      else{
        return str;
      }
    }
    
    //recursive case
    if(ch >= 'a' && ch <= 'z'){ // if char is lower case
      return (char)(ch - 32) + toUpperCase(str.substring(1)); // return the capitalized first letter and recursively call toUpperCase()
    }
    else{
      return (char)(ch) + toUpperCase(str.substring(1));
    }
  }
  
  public static int nthIndexOf(int n, char ch, String str) {
         
    //base case 1 - if user give an empty or null string, or if n is 0
    if(str == null || str.equals("") || (n <= 0)){
      return -1;
    }
    
    //base case 2 - if nth occurance is found
    if((n == 1) && (str.charAt(0) == ch)){
      return 0;
    }
    
    if(str.charAt(0) == ch){ //if the characters match, decrease n
      n -= 1;
    }
    
    //recursive case
    return nthIndexOf(n, ch, str.substring(1)) + 1;
  }
  
  public static void main(String[] args) {
    System.out.println("numOccur('s', \"Mississippi\") = " +
                       numOccur('s', "Mississippi"));
    System.out.println(Runtime.getRuntime());
    
    System.out.println("numOccur('e', \"Mississippi\") = " +
                       numOccur('e', "Mississippi"));                   
  }
}
