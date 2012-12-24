/*
 * HumanPlayer.java - takes guesses from the user.
 *
 * Code by: Tim Duffy, timmahd
 */

import java.util.*;
  
public class HumanPlayer extends Player{
  private Scanner console;
  
  // Construct a human player
  public HumanPlayer(String name, Board board, Scanner console){
    super(name, board, false);
    this.console = console;
  }
  
  // Create a guess with user input
  public Guess nextGuess(Board otherBoard) {
    int row;
    int col;
    
    boolean valid = false;
    while(!valid){
      System.out.println("Enter your guess.");
      System.out.print("     row:");
      row = console.nextInt();
      console.nextLine();
      System.out.print("  column:");
      col = console.nextInt();
      console.nextLine();
      
      if( (row < 8 && row >= 0) && (col < 8 && col >= 0) ) { valid = true; }
    }
    
    Guess guess = new Guess(row, col);
    return guess;
  }
}