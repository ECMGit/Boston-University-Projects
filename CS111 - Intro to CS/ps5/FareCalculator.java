/**
 * FareCalculator.java
 * Computer Science 111, Boston University
 * 
 * Code created by: Tim Duffy <timmahd@bu.edu> 
 * 
 * This program calculates the fare for a single ride using 
 * the MBTA based on user specifications.
 */

import java.util.*;

public class FareCalculator{
  
  // Calculate the cost of a bus ride
  public static String busFare(int age, Scanner console){
    String charlie;
    
    if(18 < age && age < 65){ // if the rider is aged 19-64, ask for a charlie card
          System.out.print("Is the rider using a Charlie Card?(y/n): ");
          charlie = console.next();
          console.nextLine(); // Consume rest of line
        }
    else {
      charlie = "n";
    }
      
    String fare;
        
    if (age <= 11){
      fare = "free";
    }
    else if (age >= 12 && age <= 18){
      fare = "$0.60";
    }
    else if (age >= 65){
      fare = "$0.40";
    }
    else{
      if(charlie.equalsIgnoreCase("y")){ // If they are using a charlie card
        fare = "$1.25";
      }
      else{
        fare = "$1.50";
      }         
    }
    return fare;
  }
  
  // Calculate the cost of a subway ride
  public static String subwayFare(int age, Scanner console){
    String charlie;
    
    if(18 < age && age < 65){ // if the rider is aged 19-64, ask for a charlie card
          System.out.print("Is the rider using a Charlie Card?(y/n): ");
          charlie = console.next();
          console.nextLine(); // Consume rest of line
        }
    else {
      charlie = "n";
    }
    
    String fare;
    
    if (age <= 11){
      fare = "free";
    }
    else if (age >= 12 && age <= 18){
      fare = "$0.85";
    }
    else if (age >= 65){
      fare = "$0.60";
    }
    else{
      if(charlie.equalsIgnoreCase("y")){ //if a charlie card is used
        fare = "$1.70";
      }
      else{
        fare = "$2.00";
      }         
    }
    return fare;
  }
  
  // Calculate the cost of a train ride
  public static int railFare(int age, Scanner console){
    int zoneNum;
    int cents;
    
    System.out.print("What zone (use 0 for zone 1A)?: ");
    zoneNum = console.nextInt();
    console.nextLine(); // Consume rest of line
           
    if(zoneNum == 0){
      cents = 170;         //cost is $1.70 for zone 1
    }
    else{
      cents = 425 + (50 * (zoneNum - 1));   // All other zones cost $4.25 + $0.50 for each zone after 1
    }   
    
    if ((age >= 12 && age <= 18) ||(age >= 65)){  // Students and seniors get 50% off
      cents = (cents / 2);
    }
    
    return cents;
  }
  
  // Calculate cost of a boat ride
  public static int boatFare(int age, Scanner console){
    int cents=0;
    String boatType;
    
    System.out.print("Enter the type of boat (I for Inner Harbor, B for Boston, L for Logan): ");
    boatType = console.next();
    console.nextLine(); // Consume the rest of the line
       
    if (boatType.equalsIgnoreCase("i")){
      cents = 170;
      if((age >= 12 && age <= 18) ||(age >= 65)){ // Students and seniors get 50% off
        cents = cents / 2;
      }
    }
    else if (boatType.equalsIgnoreCase("b")){
      cents = 600;
      if((age >= 12 && age <= 18) ||(age >= 65)){ // Students and seniors get 50% off
        cents = cents / 2;
      }
    }
    else if (boatType.equalsIgnoreCase("l")){
      cents = 1200;
      if(age >= 12 && age <= 18){ // Students get 50% off
        cents = cents / 2;
      }
      else if(age >= 65){
        cents = cents - 1000;   // Seniors get $10 off
      }
    }
    return cents;
  }
  
  public static void main(String[] args) {
    String runAgain = "";  // Initalize runAgain to an empty string (make the compiler happy)
    do {
          
      Scanner console = new Scanner(System.in);
            
      System.out.println("Welcome to the MBTA Fare Calculator" );
      System.out.println("Ride types:" );
      System.out.println("  1) Bus");
      System.out.println("  2) Subway");
      System.out.println("  3) Commuter Rail");
      System.out.println("  4) Boat");
      
      //Get user input
      System.out.print("Enter the type of ride (1-4): ");
      int rideNum = console.nextInt();
      console.nextLine(); // Consume rest of line
      System.out.print("Enter the age of the rider: ");
      int age = console.nextInt();
      console.nextLine(); // Consume rest of line
         
      if(rideNum == 1){ // Bus fare
        System.out.println("Fare: " + busFare(age, console));
      }
      else if(rideNum == 2){ // Subway fare
        System.out.println("Fare: " + subwayFare(age, console));
      }
      else if(rideNum == 3){ // Rail fare
        if(age >= 12){ // If rider is older than 12, calculate fare
          int cents = railFare(age, console);
          int dollars = cents/100;
          cents = cents % 100;
          System.out.println("Fare: $" + dollars + "." + cents);
        }
        else{ // Otherwise fare is free
          System.out.println("Fare: free");
        }
      }          
      else if(rideNum == 4){ // Boat fare
        if(age >= 12){ // If rider is older than 12, calculate fare
          int cents = boatFare(age, console);
          int dollars = cents/100;
          cents = cents % 100;
          
          if(cents == 0){
            System.out.println("Fare: $" + dollars + ".0" + cents);
          }
          else{
            System.out.println("Fare: $" + dollars + "." + cents);
          }
        }
        else{
          System.out.println("Fare: free");
        }
      }
      else{
        System.out.println("Invalid ride number. Please enter a number 1-4.");
      }
      
      System.out.print("Do you want another ride?(y/n): "); // Ask user if they want to run again
      runAgain = console.next();
      System.out.println("+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+");
      System.out.println();
      
    } while(runAgain.equalsIgnoreCase("y")); // repeat the loop if runAgain is equal to "y" or "Y"
                    
      System.out.println("Goodbye!");
      System.out.println();
    }
  }