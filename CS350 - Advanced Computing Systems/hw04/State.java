/* M/M/1 State - used to record the system state.
 * Used by MM1-controller.java and along with Event.java 
 * 
 * Written by Tim Duffy for CS350 hw04
 */
import java.util.*;
import java.io.*;

public class State {
  
  double time;
  int served;
  int waiting;
  int q;
  boolean idle;
  
    public State(double time, int s, int w){
      this.time = time;
      this.served = s;
      this.waiting = w;
      this.q = s + w;
      
      if(q == 0) { idle = true; }
      else{ idle = false; }
      
    }
    
    public void output(){
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("MM1_sim_log.txt", true));
        
        //Write out the specified string to the file
        out.write(this.time + "   \t" + this.waiting + "\t   " + this.served
                    + "\t\t" + (this.waiting + this.served) + "\n");
        
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
    }
}