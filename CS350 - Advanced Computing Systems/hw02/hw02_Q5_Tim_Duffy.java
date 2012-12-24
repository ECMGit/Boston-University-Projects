import java.util.Random;
import static java.lang.Math.*;
import java.io.*;
import java.util.Scanner;

class hw02_Q5_Tim_Duffy {
    public static void main(String[] args) {
      
      Scanner console = new Scanner(System.in);
      
      System.out.println("Input a vaule for U (between 0 and 1) for the mean of the normal distribution:");
      double U = console.nextDouble();
      console.nextLine();
      
      System.out.println("Input a vaule for S, the standard deviation of the normal distribution:");
      double S = console.nextDouble();
      console.nextLine();
      
      System.out.println("How many samples would you like to take?");
      int samps = console.nextInt();
      
      double sum = 0;
      for (int i= 0; i<=samps; i++){
        sum += Grand(U, S);
      }
      
      System.out.println("The mean of these " samp + " samples is " + (sum/samps));
    }
    
    /* Random number generator - This function returns a vaule
     * between 0.0 to 1.0 with a uniform distribution */
    public static double Zrand(){
      Random rndm = new Random();
      return rndm.nextDouble();   
    }
    
    // Picks a random number with mean U and std. dev. S
    public static double Grand(double U, double S){
      
      double z = Zrand();
      double x = ((z*S) + U);
      
      return x; 
    }
}
