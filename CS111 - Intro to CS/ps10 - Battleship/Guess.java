/*
 * Guess.java - blueprint class for objects that represent a single
 * guess (i.e., a single row, column pair) in the game of Battleship.
 * 
 * Computer Science 111, Boston University
 *
 */

public class Guess {
    private int row;
    private int column;
    
    /*
     * constructor
     */
    public Guess(int row, int column) {
        this.row = row;
        this.column = column;
    }
    
    /*
     * getRow - returns the row coordinate of the guess
     */
    public int getRow() {
        return row;
    }
    
    /*
     * getColumn - returns the column coordinate of the guess
     */
    public int getColumn() {
        return column;
    }
    
    /*
     * toString - returns a string representation of the guess
     */
    public String toString() {
        return row + "," + column;
    }
}