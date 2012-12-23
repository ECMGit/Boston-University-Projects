/* 
 * The following code draws the vertical tower
 * section of the Zakim bridge.
 */

public class DrawTower {                        // These lines are not 
  public static void main (String[] args) {     // in my DrawZakim.java program.  
                                                // I just thought I would make it
    int SCALE_FACTOR = 2;                       // eaiser for you to see the output.
    
    for(int line = 1; line <= 3; line++) {
      
      //print blocks
      for(int a = 1; a <= SCALE_FACTOR; a++) {
        
        //print spaces
        for(int b = 1; b <= 7 * SCALE_FACTOR; b++) {
          System.out.print(" ");
        }
        
        //print vertical line
        System.out.print("|");
        
        //print colons
        for(int c = 1; c < SCALE_FACTOR; c++) {
          System.out.print(":");
        }
        
        //print center line
        System.out.print("|");
        
        //print more colons
        for(int d = 1; d < SCALE_FACTOR; d++) {
          System.out.print(":");
        }
        
        //print vertical line
        System.out.print("|");
        System.out.println();
      }
      
      //print horizontal line for only 2 iterations
      for(int z=line + 2; z<=4; z=z+2) {
        //print spaces
        for(int x = 1; x <= (7* SCALE_FACTOR) ; x++) {
          System.out.print(" ");
        }
        // print horizontal line
        for(int y = 1; y <=(2 * SCALE_FACTOR) + 1; y++) {
          System.out.print("-");
        }
        System.out.println();
      }
    }
  }
}