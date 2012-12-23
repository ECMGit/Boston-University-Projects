/*
 * IntelligentPlayer.java - computer that makes intelligent guesses.
 *
 * Code by: Tim Duffy, timmahd
 */

public class IntelligentPlayer extends Player {
  private Board otherBoard;
  private int row;
  private int col;
  Guess guess = new Guess(row,col);
  private Guess lastGuess = new Guess(row,col);
  private Guess lastHit = new Guess(row,col);
  private int lastRowHit;
  private int lastColHit;
  private int lastRow;
  private int lastCol;
  private boolean rndmGuess;
  
  //constructor for an intellegent player
  public IntelligentPlayer(String name, Board board, boolean shouldPrintGuesses){
    super(name, board, shouldPrintGuesses);
  }
  
  //this method creates a guess by an intelligent computer player
  public Guess nextGuess(Board otherBoard){
    this.otherBoard = otherBoard;
    rndmGuess = true;
    
    // if the last guess was a hit, update last hit
    if(otherBoard.previousHit(lastRow, lastCol)){ 
      rndmGuess = false;
      lastHit = new Guess(lastRow,lastCol);
      lastRowHit = lastRow;
      lastColHit = lastCol;
    }
    
    //if the last hit sank the ship, guess at random
    if(otherBoard.sunkShipAt(lastRowHit, lastColHit)){
      rndmGuess = true;
    }
    else{
      rndmGuess = false;
    }
    
    if(rndmGuess){
      guess = super.nextGuess(otherBoard);
    }
    else{
      guess = smartGuess();
    }
    
    lastGuess = new Guess(guess.getRow(), guess.getColumn());
    lastRow = guess.getRow();
    lastCol = guess.getColumn();
    return guess;
  }
  
  //guesses in the same area
  public Guess smartGuess(){
    do {
      if(canNotGuess()){
        rndmGuess = true;
        return super.nextGuess(otherBoard);
      }
      
      // generate a random number
      int tempRND = BattleshipGame.RAND.nextInt(4);
      
      if(tempRND == 0){
        row = lastRowHit + 1;
        col = lastColHit;
      }
      else if (tempRND == 1){
        row = lastRowHit;
        col = lastColHit + 1;
      }
      else if (tempRND == 2){
        row = lastRowHit - 1 ;
        col = lastColHit;
      }
      else if (tempRND == 3){
        row = lastRowHit;
        col = lastColHit - 1;
      }
    } while (row > 7 || row < 0 || col > 7 || col < 0 || otherBoard.hasBeenTried(row, col));
    
    Guess guess = new Guess(row, col);
    return guess;
  }
  
  public boolean canNotGuess(){
    if(lastRowHit == 0){ //top row
      if(lastColHit == 0){ //first column
        if(otherBoard.hasBeenTried(lastRowHit + 1, lastColHit) && otherBoard.hasBeenTried(lastRowHit, lastColHit + 1)){
          return true;
        }
      }
      else if(lastColHit == 7){ //last column
        if(otherBoard.hasBeenTried(lastRowHit + 1, lastColHit) && otherBoard.hasBeenTried(lastRowHit, lastColHit - 1)){
          return true;
        }
      }
      else{ //in between
        if(otherBoard.hasBeenTried(lastRowHit + 1, lastColHit) && 
           otherBoard.hasBeenTried(lastRowHit, lastColHit + 1) && 
           otherBoard.hasBeenTried(lastRowHit, lastColHit - 1)){
          return true;
        }
      }
    }
    else if(lastRowHit == 7){ //last row
      if(lastColHit == 0){ //first column
        if(otherBoard.hasBeenTried(lastRowHit - 1, lastColHit) && otherBoard.hasBeenTried(lastRowHit, lastColHit + 1)){
          return true;
        }
      }
      if(lastColHit == 7){ //last column
        if(otherBoard.hasBeenTried(lastRowHit - 1, lastColHit) && otherBoard.hasBeenTried(lastRowHit, lastColHit - 1)){
          return true;
        }
      }
      else{ //in between
        if(otherBoard.hasBeenTried(lastRowHit - 1, lastColHit) && 
           otherBoard.hasBeenTried(lastRowHit, lastColHit + 1) && 
           otherBoard.hasBeenTried(lastRowHit, lastColHit - 1)){
          return true;
        }
      }
    }
    else if(lastColHit == 0){ //first column
      if(lastRowHit != 0 && lastRowHit != 7 ){ //inbetween
        if(otherBoard.hasBeenTried(lastRowHit + 1, lastColHit) && 
           otherBoard.hasBeenTried(lastRowHit - 1, lastColHit) &&
           otherBoard.hasBeenTried(lastRowHit, lastColHit + 1)){
          return true;
        }
      }
    }
    else if(lastColHit == 7){ //last column
      if(lastRowHit != 0 && lastRowHit != 7 ){ //in between
        if(otherBoard.hasBeenTried(lastRowHit + 1, lastColHit) && 
           otherBoard.hasBeenTried(lastRowHit - 1, lastColHit) &&
           otherBoard.hasBeenTried(lastRowHit, lastColHit - 1)){
          return true;
        }
      }
    }
    else if(lastColHit != 0 && lastColHit != 7 && lastRowHit != 0 && lastRowHit != 7){
      if(otherBoard.hasBeenTried(lastRowHit + 1, lastColHit) && 
         otherBoard.hasBeenTried(lastRowHit - 1, lastColHit) &&
         otherBoard.hasBeenTried(lastRowHit, lastColHit - 1) &&
         otherBoard.hasBeenTried(lastRowHit, lastColHit - 1)){
        return true;
      }
    }
    return false;
  }
}
