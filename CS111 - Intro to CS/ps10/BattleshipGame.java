/*
 * BattleshipGame.java - the main class for a program for playing the
 * game of Battleship.
 * 
 * Computer Science 111, Boston University
 * 
 * code added by: Tim Duffy, timmahd
 */

import java.util.*;

public class BattleshipGame {
    public static final int DIMENSION = 8;
    public static final int NUM_PLAYERS = 2;
    public static final Random RAND = new Random();
    
    /*** Fields for a BattleshipGame object ***/
    
// Scanner object used for getting input from the keyboard
    private Scanner console;
    // An array containing an object for each player.
    private Player[] players;
    
    // An array containing an object for each board. 
    private Board[] boards;
    
    // Indicates whether the program should pause after each
    // pair of moves.
    private boolean shouldPause;
    
    /*
     * constructor 
     */
    public BattleshipGame() {
        console = new Scanner(System.in);
        players = new Player[NUM_PLAYERS];
        boards = new Board[NUM_PLAYERS];
        shouldPause = false;
    }
        
    public static void main(String[] args) {
        System.out.println("Welcome to the game of Battleship!");
        BattleshipGame game = new BattleshipGame();
        game.initialize();
        game.play();
    }
        /*
     * initialize - configures the state of the game, 
     * based on the type of game selected by the user.
     */
    public void initialize() {
        System.out.println("Types of games:");
        System.out.println("  1. random vs. random");
        System.out.println("  2. human vs. random");
        System.out.println("  3. human vs. intelligent");
        System.out.println("  4. random vs. intelligent");
        System.out.println("  5. intelligent vs. intelligent");
        
        boolean validChoice = false;
        do {
            System.out.print("Enter your choice: ");
            int choice = console.nextInt();
            console.nextLine();
                    
            if (choice == 1 || choice >= 4) {
                shouldPause = true;
            }
            /* 
             * IMPORTANT: When you add support for a new choice,
             * make sure that you include the line "validChoice = true;"
             * so that the program will stop asking for another choice.
             */
            if (choice == 1) {
              boards[0] = new Board(DIMENSION);
              boards[1] = new Board(DIMENSION);
              
              players[0] = new Player("player 0", boards[0], true);
              players[1] = new Player("player 1", boards[1], true);
              validChoice = true;
            } 
            else if (choice == 2) {
              boards[0] = new Board(DIMENSION);
              boards[1] = new HiddenShipsBoard(DIMENSION);
              
              players[0] = new HumanPlayer("you", boards[0], console);
              players[1] = new Player("the computer", boards[1], true);
              validChoice = true;
            }
            else if (choice == 3) {
              boards[0] = new Board(DIMENSION);
              boards[1] = new HiddenShipsBoard(DIMENSION);
              
              players[0] = new HumanPlayer("you", boards[0], console);
              players[1] = new IntelligentPlayer("the computer", boards[1], true);
              validChoice = true;
            }
            else if (choice == 4) {
              boards[0] = new Board(DIMENSION);
              boards[1] = new Board(DIMENSION);
              
              players[0] = new Player("random", boards[0], true);
              players[1] = new IntelligentPlayer("intelligent", boards[1], true);
              validChoice = true;
            }
            else if (choice == 5) {
              boards[0] = new Board(DIMENSION);
              boards[1] = new Board(DIMENSION);
              
              players[0] = new Player("intelligent 0", boards[0], true);
              players[1] = new IntelligentPlayer("intelligent 1", boards[1], true);
              validChoice = true;
            }
            else {
              System.out.println("That type of game is not supported.");
            }
        } while (!validChoice);
        
        addShipsTo(boards[0]);
        addShipsTo(boards[1]);
    }
    
    /*
     * addShipsTo - add a fleet of ships to the specified board. 
     * Note that this method can be static, because it doesn't need to
     * access any of the fi
     * 
     * elds of the BattleshipGame object.
     */
    public static void addShipsTo(Board board) {
        board.addShip(new Battleship());
        board.addShip(new Cruiser());
        board.addShip(new Tanker());
        board.addShip(new PatrolBoat());
        board.addShip(new PatrolBoat());
    }
    
    /*
     * play - guides the playing of the game.
     */
    public void play() {
        while (true) {
            displayBoards();                            
             
            boolean gameOver = processOneGuess(0, 1);
            if (!gameOver) {
                gameOver = processOneGuess(1, 0);
            }
            
            if (gameOver) {
                return;
            }
        }
    }
    
    /*
     * processOneGuess - processes a single guess from the player whose index
     * is specified by the first parameter.  The index of the other
     * player is given by the second parameter.
     * 
     * Returns true if the player's guess ends the game, and false otherwise.
     */
    public boolean processOneGuess(int current, int other) {
        // Get the current player's next guess. 
        Guess guess = players[current].nextGuess(boards[other]);
        
        if (players[current].shouldPrintGuesses()) {
            System.out.println(players[current] + "'s guess: " + guess);
        }
        
        // Apply the guess, and print a message if a ship is hit.
        Ship ship = boards[other].applyGuess(guess);
        if (ship != null) {
            System.out.print("*** " + players[current]);
            if (ship.isSunk()) {
                System.out.println(" sunk a " + ship + "!!");
            } else {
                System.out.println(" got a hit!");
            }
        }
                
        if (players[other].hasLost()) {
            System.out.println("*** Game over! ***");
            System.out.println("The winner is " + players[current] + ".");
            displayBoards();
            return true;
        } else {
            return false;
        }
    }        
                                
    /*
     * displayBoards - display each player's board, and pause if appropriate.
     */
    public void displayBoards() {
        System.out.println();
        
        // Print the actual boards.
        System.out.println(players[0] + ":");
        boards[0].display();
        System.out.println(players[1] + ":");
        boards[1].display();
        
        // Pause if appropriate.
        if (shouldPause) {
            System.out.print("Press <ENTER> to continue (enter S to stop pausing): ");
            String entry = console.nextLine();
            if (entry.equalsIgnoreCase("S")) {
                shouldPause = false;
            }
        }
        
        // Print a horizontal line with the same width as the boards.
        for (int i = 0; i <= DIMENSION; i++) {
                System.out.print("---");
        }    
        System.out.println();
    }
}