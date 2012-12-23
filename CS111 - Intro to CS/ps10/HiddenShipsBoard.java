/*
 * HiddenShipsBoard.java - defines a board where the ship locations are hidden from the user
 * 
 * Code by: Tim Duffy, timmahd
 */

public class HiddenShipsBoard extends Board{
  
  
  public HiddenShipsBoard(int dimension) {
    super(dimension);
  }
  
  public char getStatusChar(int row, int col) {
    if (previousHit(row, col)) {
      return 'X';
    } else if (previousMiss(row, col)) {
      return '-';
    } else {
      return ' ';
    }
  }   
}