/**
 * DrawZakim.java
 * Computer Science 111, Boston University
 * 
 * Code provided by: Tim Duffy <timmahd@bu.edu> 
 * 
 * This program draws an ASCII picture of the Zikam bridge
 * using for loops.
 */
public class DrawZakim {
  public static final int SCALE_FACTOR = 2;
  
  // This method draws the top of the tower
  public static void drawTop() {
    for(int line = 1; line <= SCALE_FACTOR; line++) {
      //print spaces
      for(int i = 1; i <= (8 * SCALE_FACTOR) - line; i++) {
        System.out.print(" ");
      }
          
      //print forward slashes
      for(int i = 0; i <= line - 1; i++) {
        System.out.print("/");
      }
       
      //print vertical bar
      System.out.print("|");
          
       //print backslashes
       for(int i = 0; i <= line - 1; i++) {
         System.out.print("\\");
       }
       
       System.out.println();
    }
  }
  
  // This method draws the body of the tower
  public static void drawTower() {
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
  
  // This method draws the section where the tower and legs meet
  public static void drawTowerBase() {
    //print spaces
    for(int x = 1; x <= 7 * SCALE_FACTOR - 1 ; x++) {
      System.out.print(" ");
    } 
    
    System.out.print("/");
    
    // print horizontal line
    for(int y = 1; y <=(2 * SCALE_FACTOR) + 1; y++) {
      System.out.print("-");
    }
    System.out.print("\\");
    System.out.println();
  }
  
  // This method draws the center of the figure, before the body splits apart
  public static void drawCenter() {
    for(int line = 1; line <=(2 * SCALE_FACTOR) - 1; line ++) {
     
      //print spaces
      for(int q = 1; q <= (7 * SCALE_FACTOR) - line -1; q++) {
        System.out.print(" ");
      }
      
      System.out.print("/");
      
      //Print left braces
      for(int i = 1; i <=SCALE_FACTOR + line; i ++) {
        System.out.print("{");
      }
      
      System.out.print("|");
      
      //Print right braces
      for(int j = 1; j <=SCALE_FACTOR + line; j ++) {
        System.out.print("}");
      }
      System.out.print("\\");
      System.out.println();
    }
  }   
  
  // This method draws the legs before the cable appears
  public static void drawTopLegs() {
    for(int line = 1; line <=SCALE_FACTOR; line ++) {
      //print spaces
      for(int q = 1; q <= (5 * SCALE_FACTOR) - line; q++) {
        System.out.print(" ");
      }
      
      System.out.print("/");
      
      //Print left braces
      for(int i = 1; i <= (2 * SCALE_FACTOR) - 1; i ++) {
        System.out.print("{");
      }
      
      System.out.print("/");
      
      //print center spaces
      for(int j = 1; j <= (2 * SCALE_FACTOR) + (2 * line) - 1; j++) {
        System.out.print(" ");
      }
      
      System.out.print("\\");
      
      //Print right braces
      for(int k = 1; k <=(2 * SCALE_FACTOR) - 1; k ++) {
        System.out.print("}");
      }                      
      System.out.print("\\");
      System.out.println();
    }
  }
  
  // This method draws the legs and the support cable in the center
  public static void drawMiddleLegs() {
    for(int line = 1; line <= 2 * SCALE_FACTOR; line ++) {
      
      //print spaces
      for(int a = 1; a <= (4 * SCALE_FACTOR) - line; a++) {
        System.out.print(" ");
      }
      
      System.out.print("/");
    
      //Print left braces
      for(int i = 1; i <= (2 * SCALE_FACTOR) - 1; i ++) {
        System.out.print("{");
      }
      
      System.out.print("/");
      
      //Print spaces between 'beam' and 'cable'
      for(int c = 1; c <= 2 * (line - 1); c++){
        System.out.print(" ");
      }
      
      System.out.print("\\");
    
      //print center spaces
      for(int d=1; d <= (4 * SCALE_FACTOR) - (2 * line) + 1; d++) {
        System.out.print(" ");
      }
      
      System.out.print("/");
                         
      //Print spaces between 'cable' and 'beam'
      for(int e = 1; e <= 2 * (line - 1); e++){
        System.out.print(" ");
      }
      System.out.print("\\");
                       
      //Print right braces
      for(int i = 1; i <= (2 * SCALE_FACTOR) - 1; i ++) {
        System.out.print("}");
      }
      
      System.out.print("\\");
      System.out.println();
    }
  }
 
  // This method draws the legs and the support cable after it has reached the center
  public static void drawBottomLegs() {
    for(int line = 1; line <= 2 * SCALE_FACTOR; line++) {
 
      //print spaces
      for(int a = 1; a <= (2 * SCALE_FACTOR) - line; a++) {
        System.out.print(" ");
      }
      
      System.out.print("/");
      
      //Print left braces
      for(int b = 1; b <= (2 * SCALE_FACTOR) - 1; b ++) {
        System.out.print("{");
      }
      
      System.out.print("/");
      
      //Print spaces between 'beam' and 'cable'
      for(int c = 1; c <= line + (4 * SCALE_FACTOR) - 1; c++){
        System.out.print(" ");
      }
      
      System.out.print("|");
      
      //Print spaces between 'cable' and 'beam'
      for(int d = 1; d <= line + (4 * SCALE_FACTOR) - 1; d++){
        System.out.print(" ");
      }
      
      System.out.print("\\");
                       
      //Print right braces
      for(int e = 1; e <= (2 * SCALE_FACTOR) - 1; e ++) {
        System.out.print("}");
      }
      
      System.out.print("\\");
      System.out.println();
    }
  }
  
  // This method draws the bottom edge of the figure
  public static void drawBase() {
    for(int a = 1; a <= (2 * SCALE_FACTOR); a++) {
      System.out.print("=");
    }
    
    for(int b = 1; b <= (6 * SCALE_FACTOR); b++) {
      System.out.print(" ");
    }
    
    System.out.print("=");
      
    for(int c = 1; c <= (6 * SCALE_FACTOR); c++) {
      System.out.print(" ");
    }
    
    for(int d = 1; d <= (2 * SCALE_FACTOR); d++) {
      System.out.print("=");
    }
  }
  
  public static void main(String[] args) {
    drawTop();
    drawTower();
    drawTowerBase();
    drawCenter();
    drawTopLegs();
    drawMiddleLegs();
    drawBottomLegs();
    drawBase();
  }
}