/* This program will write the words "HEY HEY HO HO" in block letters. */

public class BlockLetterWriter{
    public static void WriteH() {
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("+-----+");
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("|     |");
    }
    
    public static void WriteE() {
       System.out.println("+------");
       System.out.println("|");
       System.out.println("|");
       System.out.println("+------");
       System.out.println("|");
       System.out.println("|");
       System.out.println("+------");
    }
    
     public static void WriteY() {
       System.out.println("\\     /");
       System.out.println(" \\   /");
       System.out.println("  \\ /");
       System.out.println("   V");
       System.out.println("   |");
       System.out.println("   |");
       System.out.println("   |");
    }
     
     public static void WritePoint() {
       System.out.println("   |");
       System.out.println("   |");
       System.out.println("   |");
       System.out.println("   |");
       System.out.println("   |");
       System.out.println("   \'");
       System.out.println("   *");
    }
     
     public static void WriteO() {
       System.out.println("+-----+");
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("|     |");
       System.out.println("+-----+");
    }
     
     public static void WriteSpace() {
       System.out.println();
       System.out.println();
       System.out.println();
       System.out.println();
       System.out.println();
    }
     
     public static void WriteHEY() {
      WriteH();
      System.out.println();
      WriteE();
      System.out.println();
      WriteY();
    }

     public static void WriteHO() {
      WriteH();
      System.out.println();
      WriteO();
    }
     
     public static void main(String[] args) {
      WriteHEY();
      WriteSpace();
      WriteHEY();
      System.out.println();
      WritePoint();
      WriteSpace();
      WriteHO();
      WriteSpace();
      WriteHO();
      System.out.println();
      WritePoint();
      System.out.println();
    }
}