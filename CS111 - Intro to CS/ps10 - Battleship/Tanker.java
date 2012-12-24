/*
 * Tanker.java - defines a Tanker.
 * 
 * Code by: Tim Duffy, timmahd
 */

public class Tanker extends Ship{
  private int length = 3;
  private int numHits;
  
  //constructor
  public Tanker() {
    super(3);
  }
  
  public int getLength() {
    return length;
  }
   
  public char getSymbol() {
    return 'T';
  }
    
  public void applyHit() {
    numHits = 3;
  }
  
  public boolean isSunk() {
    return (numHits >= 1);
  }
  
  public String toString() {
    return "tanker";
  }
}