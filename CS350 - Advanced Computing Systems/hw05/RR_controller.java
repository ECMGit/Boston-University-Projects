/* Round Robin simulator - simulates an Round Robin scheduled system with user specified arrival rate (lambda),
 * service rate (Ts), and time slice given for each round of service (c). 
 * Uses RR_Event.java to schedule events and State.java record the current state of the system
 * 
 * Written by Tim Duffy for CS350 hw05
 */


import java.io.*;
import java.util.*;
import static java.lang.Math.*;

class RR_controller {  
    public static void main(String[] args) {
      
      // Create log file, or clear the one that already exists
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("RR_sim_log.txt"));
        
        //Write out the specified string to the file
        out.write("Time \t\t\tw \t# served   total in system (q)");
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
     
      Scanner console = new Scanner(System.in);
      
      System.out.println("Welcome to the Round Robin simulator!");
     // System.out.println("What is the mean arrival rate (lambda) of events (events / second)?");
    //  double LAMBDA = console.nextDouble() / 1000; // convert to events/ msec
      //console.nextLine();
      double LAMBDA = 30.0 /1000;
      
     // System.out.println("What is the mean service time (Ts) for events (in msecs)?");
     // double TS = 1.0 / console.nextDouble(); // convert to # processes served/ msec
     // console.nextLine();
        double TS = 1.0 / 30;
        
      System.out.println("How long is the fixed time slice (quantum) of the RR scheduler (in msecs)?");
      double c = console.nextDouble();
      console.nextLine();
      
      double p = ((LAMBDA * 1000) * ((1.0/TS)/1000));
      
      // If there is an infinite queue, and utilization is over 100%, do not run simulation
      if(p > 1){ 
        System.out.println("This system is over 100% utilization! Simulation is pointless.");
        System.out.println("Tq = Tw = w = q = infinity.");
        return;
      }
      
   //   System.out.println("How long would you like the simulation to run (in seconds)?");
   //   double MAXTIME = console.nextDouble() * 1000;
       double MAXTIME = 100.0 * 1000;
      
       LinkedList <State> state = initializeState();
      LinkedList <RR_Event> schedule = initializeSchedule(LAMBDA, TS, c);
      
      System.out.println("System warming up - Please wait.");
      
      int simStartIndex = warmup(schedule, state, MAXTIME);
      
      System.out.println("System ready - Beginning simulation.");
      
      double time = 0;
      int index = simStartIndex;
      double qsum = 0.0;
      
      while(time < (2 * MAXTIME)) { 
        RR_Event x = schedule.get(index);
        if(x.type == 0 || x.type == 2){ time = x.deathtime; }
        else{ time = x.arrivaltime; }
        
        x.function(schedule,state,time);
        index++;
      }
      
      System.out.println("Simulation complete! Calculating results. \n");
      report(schedule, state, simStartIndex); // Prints out report of simulation
    }
    
    public static LinkedList <State> initializeState(){
      LinkedList <State> state = new LinkedList <State>();
      state.add(new  State(-1, 0.0, 0, 0));
      return state;
    }
    
    public static LinkedList <RR_Event> initializeSchedule(double l, double mean_ts, double c){
      LinkedList <RR_Event> schedule = new LinkedList <RR_Event>();
      Random rndm = new Random();
            
      double x = (-1* log(1-rndm.nextDouble())) / (1.0/250);
      RR_Event frstMon = new RR_Event(-1, x, 0.0, 0.0, 0.0, l, mean_ts, c);
      schedule.add(frstMon);
      
      double frstArrtime = ((-1* log(1-(rndm.nextDouble()))) / l);
      double firstTs = (-1* log(1-rndm.nextDouble())) / mean_ts;
      RR_Event frstArr = new RR_Event(1, frstArrtime, 0, firstTs, 0.0, l, mean_ts, c);

      if(x < frstArrtime){ schedule.addLast(frstArr); }
      else{ schedule.addFirst(frstArr); }
      
      return schedule;
    }
    
    // Warms up the system: Runs for as long as the simulation does
    public static int warmup(LinkedList <RR_Event> schedule, LinkedList <State> state, double MAXTIME){
      
      double time = 0;
      int index = 0;
      
      while(time < MAXTIME) { 
        RR_Event x = schedule.get(index);
        if(x.type == 0){ time = x.deathtime; }
        else{ time = x.arrivaltime; }
        
        x.function(schedule,state,time);
        index++;
      }
      return index;
    }
    
    public static void report(LinkedList <RR_Event> schedule, LinkedList <State> state, int startIndex){
      
      int numevents = schedule.size()-1;
      int index = startIndex;
      int count = 0;
      double avgTq = 0;
      RR_Event curr;
      
      // Calculate average Tq and average Tw
      while(index < numevents){
        
        curr = schedule.get(index);
        // If the event is a death, extract birth and death times, and calculate Tq
        if(curr.type == 0){
          avgTq += (curr.deathtime - curr.arrivaltime);
          count ++;
        }
        
        // If curr is a monitor or arrival, do nothing
        index++;
      }

      avgTq = avgTq / count;  // Sample mean
      
      // Using Little's law, we know that Tw = Tq - Ts
      double avgTw = (avgTq - (1.0 / schedule.getFirst().mean_ts));
      
      // Calculate average w and q using state
      index = 0;
      count = 0;
      double avgQ = 0;
      double avgW = 0;
      
      while(index < state.size()){
        
        State currState = state.get(index);
        
        // Only consider monitoring events (states with type == -1)
        if(currState.type == -1){
          avgQ += (currState.waiting + currState.served);
          avgW += currState.waiting;
          count++;
        }
        
        index++;
      }

      // Sample means
      avgQ = avgQ / count;
      avgW = avgW / count;
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("RR_sim_log.txt", true));
        
        //Write the report to the file
        out.write("\nSpecifications for this system (according to this simulation): \n");
        out.write("Average arrival rate (lambda): " + (schedule.getFirst().lambda * 1000) + " events/second\n");
        out.write("Average service rate (Ts): " + (1.0 /schedule.getFirst().mean_ts) + " msecs\n");
        out.write("Average wait time (Tw): " + avgTw + " msecs\n");
        out.write("Average response time (Tq): " + avgTq + " msecs\n");
        out.write("Average number of process waiting (w): " + avgW + " processes\n");
        out.write("Average number of process in the system (q): " + avgQ + " processes\n");
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
      
      System.out.println("Specifications for this system (according to this simulation): ");
      System.out.println("Average arrival rate (lambda): " + (schedule.getFirst().lambda * 1000) + " events/second");
      System.out.println("Average service rate (Ts): " + (1.0 /schedule.getFirst().mean_ts) + " msecs");
      System.out.println("Average wait time (Tw): " + avgTw + " msecs");
      System.out.println("Average response time (Tq): " + avgTq + " msecs");
      System.out.println("Average number of process waiting (w): " + avgW + " processes");
      System.out.println("Average number of process in the system (q): " + avgQ + " processes");
    }
}