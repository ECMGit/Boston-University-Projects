/*
 * Battleship.java - defines a Battleship.
 * 
 * Code by: Tim Duffy, timmahd
 */

public class Battleship extends Ship{
  private int length = 4;
  
  //constructor
  public Battleship() {
    super(4);
  }
  
  public int getLength() {
    return length;
  }
   
  public char getSymbol() {
    return 'B';
  }
  
  public String toString() {
    return "battleship";
  }
}