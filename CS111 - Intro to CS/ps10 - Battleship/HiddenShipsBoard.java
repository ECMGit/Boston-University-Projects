/*
 * HiddenShipsBoard.java - defines a board where the ship locations are hidden from the user
 * 
 * Code by: Tim Duffy, timmahd
 */

public class HiddenShipsBoard extends Board{
  
  // constructor
  public HiddenShipsBoard(int dimension) {
    super(dimension);
  }
  
  // This method only shows hits and misses on the board
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