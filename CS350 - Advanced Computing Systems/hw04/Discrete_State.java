/* Discrete_State - contains code to record system state.
 * Used by Discrete_Event_controller.java along with Discrete_Event.java 
 * 
 * Written by Tim Duffy for CS350 hw04
 */
import java.util.*;
import java.io.*;

public class Discrete_State {
  int type; // 1-CPU state, 2-Disk State, 3-Network State
  double time;
  int served;
  int waiting;
  int q;
  boolean idle;
  
    public Discrete_State(int type, double time, int s, int w){
      this.type = type;
      this.time = time;
      this.served = s;
      this.waiting = w;
      this.q = s + w;
      
      if(q == 0) { idle = true; }
      else{ idle = false; }
      
    }
    
    public void output(){
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("Discrete_Event_log.txt", true));
        
        //Write out the specified string to the file
        if(this.type == 1){
        out.write("    CPU: " + this.time + "   \t" + this.waiting + "\t   " + this.served
                    + "\t\t" + (this.waiting + this.served) + "\n");
        }
        
        else if(this.type == 2){
        out.write("   Disk: " + this.time + "   \t" + this.waiting + "\t   " + this.served
                    + "\t\t" + (this.waiting + this.served) + "\n");
        }
        else{
        out.write("Network: " + this.time + "   \t" + this.waiting + "\t   " + this.served
                    + "\t\t" + (this.waiting + this.served) + "\n\n");
        }
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
    }
}