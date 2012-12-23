/*
 * Cruiser.java - defines a Cruiser.
 * 
 * Code by: Tim Duffy, timmahd
 */

public class Cruiser extends Ship{
  private int length = 3;
  
  //constructor
  public Cruiser() {
    super(3);
  }
  
  public int getLength() {
    return length;
  }
   
  public char getSymbol() {
    return 'C';
  }
  
  public String toString() {
    return "cruiser";
  }
}