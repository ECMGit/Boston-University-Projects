/*
 * Player.java - blueprint class for objects that represent a single
 * player in the game of Battleship.  This type of player makes
 * random guesses.
 * 
 * Computer Science 111, Boston University
 * 
 */

public class Player {
    // This player's name and board.
    private String name;
    private Board board;
    
    // Should this player's guesses be printed?
    private boolean shouldPrintGuesses;
    
    /*
     * constructor for a Player with the specified name and board
     */
    public Player(String name, Board board, boolean shouldPrintGuesses) {
        if (name == null || board == null) {
            throw new IllegalArgumentException("parameters must be non-null");
        }
        
        this.name = name;
        this.board = board;
        this.shouldPrintGuesses = shouldPrintGuesses;
    }
    
    /*
     * getName - returns the name of the player
     */
    public String getName() {
        return name;
    }
    
    /*
     * getBoard - returns the player's board
     */
    public Board getBoard() {
        return board;
    }

    /*
     * shouldPrintGuesses - should this player's guesses be printed?
     * Returns true if they should be, and false otherwise.
     */
    public boolean shouldPrintGuesses() {
        return shouldPrintGuesses;
    }
    
    /*
     * nextGuess - returns a Guess object representing the player's
     * next guess for the location of a ship on the board specified
     * by the parameter otherBoard.
     */
    public Guess nextGuess(Board otherBoard) {
        int row;
        int col;
        
        // Keep randomly selecting coordinates until we get 
        // a position that has not already been tried.
        do {
            row = BattleshipGame.RAND.nextInt(otherBoard.getDimension());
            col = BattleshipGame.RAND.nextInt(otherBoard.getDimension());
        } while (otherBoard.hasBeenTried(row, col));
        
        Guess guess = new Guess(row, col);
        return guess;
    }
    
    /*
     * hasLost - has this player lost the game?
     * Returns true if this is the case, and false otherwise.
     */
    public boolean hasLost() {
        return (board.getShipsRemaining() <= 0);
    }
    
    /*
     * toString - returns a string representation of the player
     */
    public String toString() {
        return name;
    }
}