/*
 * Ship.java - blueprint class for objects that represent a single
 * ship in the game of Battleship.
 * 
 * Computer Science 111, Boston University
 * 
 * ********* YOU SHOULD NOT EDIT THIS FILE. *********
 */

public class Ship {
    // This ships length, and the number of times it has been hit.
    private int length;
    private int numHits;
    
    /*
     * constructor for a ship with the specified length
     */
    public Ship(int length) {
        this.length = length;
        numHits = 0;
    }
    
    /*
     * getLength - returns the length of the ship
     */
    public int getLength() {
        return length;
    }
    
    /*
     * getNumHits - returns the number of hits that the ship
     * has incurred
     */
    public int getNumHits() {
        return numHits;
    }
    
    /*
     * isSunk - is this ship sunk?  Returns true if it is, and false otherwise.
     */
    public boolean isSunk() {
        return (numHits >= length);
    }
    
    /*
     * applyHit - apply a single hit to this ship
     */
    public void applyHit() {
        numHits++;
    }
    
    /*
     * getSymbol - returns the single-character symbol that should
     * be used when displaying this ship on the board
     */
    public char getSymbol() {
        return 'S';
    }
   
    /*
     * toString - returns a string representation (a name) of the ship
     */
    public String toString() {
        return "ship of length " + length;
    }
}