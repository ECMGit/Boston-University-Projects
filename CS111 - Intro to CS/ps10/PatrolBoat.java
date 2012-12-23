/*
 * PatrolBoat.java - defines a PatrolBoat.
 * 
 * Code by: Tim Duffy, timmahd
 */

public class PatrolBoat extends Ship{
  private int length = 2;
  private int numHits;
  
  //constructor
  public PatrolBoat() {
    super(2);
  }
  
  public int getLength() {
    return length;
  }
   
  public char getSymbol() {
    return 'p';
  }
      
  public String toString() {
    return "patrol boat";
  }
}