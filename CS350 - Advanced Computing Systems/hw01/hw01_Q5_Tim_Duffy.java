import java.util.Random;
import static java.lang.Math.*;
import java.io.*;
import java.util.Scanner;

class hw01_Q5_Tim_Duffy {
    public static void main(String[] args) {
      // Random number generator
      Random rndm = new Random();
            
      Scanner console = new Scanner(System.in);
      System.out.println("What is the mean service time (Ts) for events (in seconds)?");
      double TS = 1.0 / (console.nextDouble() * 1000);
      
      double sum = 0.0;
      
      double MAXTIME = 100000;
      
     /* for(int i = 1; i <= 100; i++){
       The Random class, along with the nextFloat function returns the next
       * pseudorandom, uniformly distributed float value between 0.0 and 1.0 
       * from this random number generator's sequence 
        double U = rndm.nextDouble();
        double V = ((-1* log(1-U)) / TS );
        
        System.out.println("" + V);
                        
        sum += V;
      }
      
      System.out.print("The mean of these 100 numbers is actually "); 
      System.out.println(sum/100); */
      
     System.out.print( "" + ((-1* log(1-(rndm.nextDouble()))) / (1.0/(0.015*1000))));
       
      
    }
}