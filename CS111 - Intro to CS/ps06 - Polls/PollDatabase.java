/*
 * PollDatabase.java
 * 
 * starter code by Computer Science 111
 * 
 * completed by: Tim Duffy
 * email: <timmahd@bu.edu>
 * 
 * description: a simple database application that retrieves data
 * from a text file using a Scanner object.
 */

import java.io.*;
import java.util.*;

public class PollDatabase {
  
  public static final String DATA_FILE_NAME = "polls.txt"; // Class constant for name of file
  
  /*
   * getInt - repeatedly prompts the user with the message msg
   * until he/she inputs an integer from the console, and returns 
   * the integer.
   */
  public static int getInt(Scanner console, String msg) {
    System.out.print(msg);
    while (!console.hasNextInt()) {
      console.nextLine();        // discard the current line of input
      System.out.print("Not an integer. Try again: ");
    }
    
    int num = console.nextInt();
    console.nextLine();   // consume the rest of the line
    return num;
  }
  
  /*
   * printHeading - prints the heading for the results of a search.
   * 
   * If the parameter is true (i.e., if the user calls printHeading(true)), 
   * the heading will include a column for the pollster.  
   * 
   * If the parameter is false (i.e., if the user calls printHeading(false)(, 
   * the heading will not include that column.
   */
  public static void printHeading(boolean includePollster) {
    System.out.println();
    if (includePollster) {
      System.out.print("pollster\t\t");
    }
    System.out.println("date\tMcCain\tObama\tundec\tmargin");
  }
  
  /*
   * printPollster - prints the name of a pollster with the correct
   * amount of padding so that the results will line up in columns.
   */
  public static void printPollster(String pollster) {
    System.out.printf("%-20.20s\t", pollster);
  }
  
  /*
   * month - takes a date string of the form m/d and return the month
   * as an integer.
   */
  public static int month(String date) {
    int slashIndex = date.indexOf('/');
    String monthStr = date.substring(0, slashIndex);
    return Integer.parseInt(monthStr);
  }
  
  /*
   * day - takes a date string of the form m/d and return the day
   * as an integer.
   */
  public static int day(String date) {
    int slashIndex = date.indexOf('/');
    String dayStr = date.substring(slashIndex + 1);
    return Integer.parseInt(dayStr);
  }
  
  /*
   * isBetween - takes three date strings of the form m/d, and returns
   * true if the first date is on or between the second and third dates,
   * and false otherwise.
   * 
   * We have given you the start of this method.  The current version
   * calls the month and day methods to get the components of the dates
   * as integers.  It then always returns false.
   * 
   * You will need to complete the method so that it returns 
   * the correct value, given the six date components.
   * Hint: use an if - else if - else statement.
   */
  public static boolean isBetween(String date, String start, String end) {
    // Get the month and day components of the three dates.
    int month = month(date);
    int day = day(date);
    int startMonth = month(start);
    int startDay = day(start);
    int endMonth = month(end);
    int endDay = day(end);
    
    // Check to see if date is between target dates, and return the appropriate value
    if((startMonth < month) && (endMonth < month)){
      return false;
    }
    else if((startMonth < month) && (endMonth == month)){
      if(endDay >= day){
        return true;
      }
      else{
        return false;
      }
    }
    else if((startMonth < month) && (endMonth > month)){
      return true;
    }
    else if((startMonth == month) && (endMonth == month)){
      if((startDay <= day) && (day <= endDay)){
        return true;
      }
      else{
        return false;
      }
    }
    else if((startMonth == month) && (endMonth > month)){
      if(startDay <= day){
        return true;
      }
      else{
        return false;
      }
    }
    else{
      return false;
    }
  }
  
  public static void searchPollster(Scanner input, Scanner console){
    //Get input from user
    System.out.print("pollster?: ");
    String target = console.nextLine();
    
    input.useDelimiter("[\t\n]"); //Change delimiter to tabs
    boolean found = false; // flag to see if any matches were found
    boolean heading = true;
    
    while(input.hasNext()){ // while there is another token to read
      // Read tokens on the line
      String pollster = input.next();
      String date = input.next();
      int mcCain = input.nextInt();
      int obama =  input.nextInt();
      String unDec = input.nextLine();
      
      if(target.equalsIgnoreCase(pollster)){ // if a match is found
        
        if(heading){
          printHeading(false);
        }
        
        heading = false; // disable heading from printing again
        found = true;
        int margin = mcCain - obama;
        
        if(margin > 0){ // if McCain is up, add an R
          System.out.println(date + "\t" + mcCain + "\t" + obama + unDec + "\t" + margin + "R");
        }
        else if(margin < 0){ // if O8ama is up, add an D
          System.out.println(date + "\t" + mcCain + "\t" + obama + unDec + "\t" + (-1 * margin) + "D");
        }
        else{ // if tied, add nothing
          System.out.println(date + "\t" + mcCain + "\t" + obama + unDec + "\t" + margin);
        }
      }
    }
    if(!found){ // if no results match target
      System.out.println("No matches found");
    }
  }
  
  public static void searchDate(Scanner input, Scanner console){
    
    //Get input from user as Strings
    System.out.print("Enter start date (m/d): ");
    String start = console.nextLine();
    System.out.print("Enter end date (m/d): ");
    String end = console.nextLine();
   
    input.useDelimiter("[\t\n]"); //Change delimiter to tabs
    boolean found = false; //boolean flag
    boolean heading = true;
    
    while(input.hasNext()){ // while there is another token to read
      // read tokens on the line
      String pollster = input.next();
      String date = input.next();
      int mcCain = input.nextInt();
      int obama =  input.nextInt();
      String unDec = input.nextLine();
      
      if(isBetween(date, start, end)){ //check to see if date is between target dates
        
        if(heading){
          printHeading(true);
        }
        
        heading = false; // disable heading from printing again
        found = true;
        int margin = mcCain - obama;
        
        if(margin > 0){ // if McCain is up, add an R
          printPollster(pollster);
          System.out.println(date + "\t" + mcCain + "\t" + obama + unDec + "\t" + margin + "R");
        }
        else if(margin < 0){ // if O8ama is up, add an D
          printPollster(pollster);
          System.out.println(date + "\t" + mcCain + "\t" + obama + unDec + "\t" + (-1 * margin) + "D");
        }
        else{
          printPollster(pollster); // if tied, add nothing
          System.out.println(date + "\t" + mcCain + "\t" + obama + unDec + "\t" + margin);
        }
      }
    }
    if(!found){ // if no results match target
      System.out.println("No matches found");
    }
  } 
  
  public static void main(String[] args) throws FileNotFoundException{
    Scanner console = new Scanner(System.in); // Scanner to read input
    
    int choice;
    
    do{
      Scanner input = new Scanner(new File(DATA_FILE_NAME)); // Scanner to read from file
      System.out.println("MENU");
      System.out.println("1. search by pollster");
      System.out.println("2. search by date");
      System.out.println("3. quit");
      String msg = "Enter the number of your choice:";
      
      do{
        choice = getInt(console, msg);
      } while(choice != 1 && choice != 2 && choice != 3); // ask until user enters a 1,2, or 3
      
      if(choice == 1){
        searchPollster(input, console); // Search by pollster
      }
      else if(choice == 2){
        searchDate(input, console); // Search by date
      }      
    
      System.out.println();
    }while(choice !=3); // repeat until user enters a 3 to quit
  }
}