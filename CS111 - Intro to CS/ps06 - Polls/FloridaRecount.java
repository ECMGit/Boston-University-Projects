/*
 * FloridaRecount.java
 * 
 * starter code by Computer Science 111
 * 
 * completed by: Tim Duffy
 * email: timmahd
 * 
 * description: an application that uses arrays to
 * process a data file of information about uncounted
 * ballots from the 2000 presidential election in Florida.
 */

import java.util.*;
import java.io.*;

public class FloridaRecount {
    public static final String FILE_NAME = "recount.txt";
    public static final int NUM_CODERS = 3;
    public static final int CERTIFIED_BUSH = 2912790;
    public static final int CERTIFIED_GORE = 2912253;
    
    public static final String[] COUNTIES = {"Broward", "Miami-Dade", "Palm Beach", "Volusia"};
    public static final boolean[] USED_PUNCH_BALLOTS = {true, true, true, false};
    
    /*
     * printCounty - prints the name of a county with the correct
     * amount of padding so that the results will line up in columns.
     */
    public static void printCounty(String name) {
        System.out.printf("%-14.14s\t", name);
    }
    
    public static int getCountyNum(String countyName) {
      for(int i = 0; i < COUNTIES.length; i++){
        if(COUNTIES[i].equalsIgnoreCase(countyName)){
          return i;
        }
      }
      return -1;
    }
    
    public static void readFile(int standard, int mustAgree, int[] bushCounts, int[] goreCounts)throws FileNotFoundException{
      Scanner input = new Scanner(new File(FILE_NAME));
      input.useDelimiter("[\t\n]");

      int bushCode;
      int goreCode;
      String county;
      int countyNum;
      
      while(input.hasNext()){
        
        county = input.next();
        countyNum = getCountyNum(county);
        
        // These are temporary counters that keep track of
        // of how many coders think that a ballot should be counted
        // for each of the candidates.       
        int numCodersBush = 0;
        int numCodersGore = 0;

        // Process the codes assigned by each of the coders.
        for(int i = 0; i < NUM_CODERS; i++){
            bushCode = input.nextInt();
            bushCode = doesIndicate(bushCode, standard, countyNum);
            
            goreCode = input.nextInt();
            goreCode = doesIndicate(goreCode, standard, countyNum);
            
            
            if (bushCode == 1 && goreCode == 0) {
              numCodersBush++;
            } else if (bushCode == 0 && goreCode == 1) {
              numCodersGore++;
            }
        }

        if (numCodersBush >= mustAgree) {
          bushCounts[countyNum] = bushCounts[countyNum] + 1;
        } else if (numCodersGore >= mustAgree) {
          goreCounts[countyNum] = goreCounts[countyNum] + 1;
        }
      }
      
      printResults(bushCounts, goreCounts);
    }
    
    public static void printResults(int[] bushCounts, int[] goreCounts){
      System.out.println("Votes gained by county");
      printCounty(" ");
      System.out.println("Bush\tGore");
      for(int i = 0; i < COUNTIES.length; i++){
        printCounty(COUNTIES[i]);
        System.out.println(bushCounts[i] + "\t" + goreCounts[i]);
      }
      
      int totalBush = CERTIFIED_BUSH;
      int totalGore = CERTIFIED_GORE;
      
      for(int i = 0; i < COUNTIES.length; i++){
        totalBush += bushCounts[i];
        totalGore += goreCounts[i];
      }
      
      System.out.println("Final Results");
      System.out.println("\tBush: " + totalBush + " votes");
      System.out.println("\tGore: " + totalGore + " votes");
      
      int diff = totalBush - totalGore;
      
      if (diff >=0){
        System.out.println("Bush wins by " + diff + " votes.");
      }
      else{
        System.out.println("Gore wins by " + (-1 * diff) + " votes.");
      }
    }
      
    public static int doesIndicate(int code, int standard, int countyNum){
      if(USED_PUNCH_BALLOTS[countyNum]){
        if(standard == 1){
          if(code == 4){
            return 1;
          }
        }
        else{
          if(code > 0){
            return 1;
          }
        }
        return 0;
      }
      else{
        if(standard == 1){
          if(code == 44){
            return 1;
          }
        }
        else{
          if(code != 0 && code != 99){
            return 1;
          }
        }
        return 0;
      }
    }
    
    
    public static void main(String args[])throws FileNotFoundException{
        
      Scanner console = new Scanner(System.in);
      
      System.out.println("Welcome to the Florida Recount Calculator");
      System.out.println();
      System.out.println("Possible standards for which ballot markings should be counted:");
      System.out.println("1) only filled ovals and fully detached chads");
      System.out.println("2) any sign of intent (e.g., partially detached or dimpled chads");
      System.out.println("Which standard do you want to use?");
      int standard = console.nextInt();
      System.out.println("How many of the coders must agree (2-3)?");
      int mustAgree = console.nextInt();
      
      int[] bushCounts = new int[COUNTIES.length];
      int[] goreCounts = new int[COUNTIES.length];
      
      readFile(standard, mustAgree, bushCounts, goreCounts);
    }
}