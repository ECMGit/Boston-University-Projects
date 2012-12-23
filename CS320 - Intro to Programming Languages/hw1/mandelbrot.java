import java.io.*;
import java.util.*;

class Mandelbrot{
    
    public static void main(String[] args) {
	
	Scanner input = new Scanner(System.in);

	/*	System.out.print("Please enter the resolution: ");
	int res = input.nextInt();
	
	System.out.print("Enter the orbit index: ");
	int orbit = input.nextInt();

	System.out.print("res = " + res + " orbit = " + orbit);
	*/
	
	System.out.print("");
       
    }
    
    public static String prefix(int n, String xs){
	if(n <= 0 || xs.length() == 0){ return "";}
	else if( xs.length() == 1){ return xs;}
	else{
	    return ( xs.charAt(0) + (prefix((n-1), xs.substring(1))));
	}
    }
    
    public static String suffix(int n, String xs){
	if(n <= 0 || xs == ""){ return xs;}
	else{ return xs.substring(n);}
    }

    public static String split(int n, char y, String xs){
	if(n <= 0){return xs;}
	else if( n >= xs.length()){return xs;}
	else{
	    return (prefix(n, xs) + y + (split(n, y, xs.substring(n))));
	}
    }

    public static double[][] plane(int  r){
	
}