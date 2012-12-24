/* M/M/1 controller - simulates an M/M/1 system with user specified arrival rate and service rate. 
 * Uses Event.java to schedule events and State.java record the current state of the system
 * 
 * Written by Tim Duffy for CS350 hw04
 */


import java.io.*;
import java.util.*;
import static java.lang.Math.*;

class MM1_controller {  
    public static void main(String[] args) {
      
      // Create log file, or clear the one that already exists
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("MM1_sim_log.txt"));
        
        //Write out the specified string to the file
        out.write("Time \t\t\tw \t# served   total in system (q)");
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
     
      Scanner console = new Scanner(System.in);
      
      System.out.println("Welcome to the M/M/1 simulator!");
      System.out.println("What is the mean arrival rate (lambda) of events (events / second)?");
      double LAMBDA = console.nextDouble() / 1000; // convert to events/ msec
      console.nextLine();
      
      System.out.println("What is the mean service time (Ts) for events (in seconds)?");
      double TS = 1.0 / (console.nextDouble() * 1000); // convert to # processes served/ msec
      console.nextLine();
      
      System.out.println("Is this a fixed service time? (enter 'y' for yes, fixed Ts or 'n' for no, random Ts)");
      String answer = console.next();
      boolean fixedTs;
      if(answer.equals("y") || answer.equals("Y") || answer.equals("yes") || answer.equals("Yes")){
        fixedTs = true;
      }
      else { fixedTs = false; }
      
      console.nextLine();
        
      System.out.println("What is the maximum queue size? (enter -1 for infinite queue)?");
      int k = console.nextInt();
      
      // If the user selects an infinte q
      if(k == -1){ 
        double p = ((LAMBDA * 1000) * ((1.0/TS)/1000));
      
      // If there is an infinite queue, and utilization is over 100%, do not run simulation
      if(p > 1){ 
        System.out.println("This system is over 100% utilization! Simulation is pointless.");
        System.out.println("Tq = Tw = w = q = infinity.");
        return;
      }
      
      // Otherwise, pick a very large number for k instead
      k = 2147483647; // 2,147,483,647 is the largest number an int type can represent
      } 
      
      System.out.println("How long would you like the simulation to run (in seconds)?");
      double MAXTIME = console.nextDouble() * 1000;
      
      LinkedList <State> state = initializeState();
      LinkedList <Event> schedule = initializeSchedule(LAMBDA, TS, k, fixedTs);
      
      System.out.println("System warming up - Please wait.");
      
      int simStartIndex = warmup(schedule, state, MAXTIME);
      
      System.out.println("System ready - Beginning simulation.");
      
      double time = 0;
      int index = simStartIndex;
      double qsum = 0.0;
      
      while(time < (2 * MAXTIME)) { 
        Event x = schedule.get(index);
        if(x.type == 0){ time = x.deathtime; }
        else{ time = x.arrivaltime; }
        
        x.function(schedule,state,time);
        index++;
      }
      
      System.out.println("Simulation complete! Calculating results. \n");
      report(schedule, state, simStartIndex); // Prints out report of simulation
    }
    
    public static LinkedList <State> initializeState(){
      LinkedList <State> state = new LinkedList <State>();
      state.add(new  State(0.0, 0, 0));
      return state;
    }
    
    public static LinkedList <Event> initializeSchedule(double l, double mean_ts, int k, boolean fixedTs){
      LinkedList <Event> schedule = new LinkedList <Event>();
      Random rndm = new Random();
            
      double x = (-1* log(1-rndm.nextDouble())) / (1.0/250);
      Event frstMon = new Event(-1, x, 0, l, mean_ts, k, fixedTs);
      schedule.add(frstMon);
      
      double frstArrtime = ((-1* log(1-(rndm.nextDouble()))) / l);
      Event frstArr = new Event(1, frstArrtime, 0, l, mean_ts, k, fixedTs);
      
      if(x < frstArrtime){ schedule.addLast(frstArr); }
      else{ schedule.addFirst(frstArr); }
      
      return schedule;
    }
    
    // Warms up the system: Runs for as long as the simulation does
    public static int warmup(LinkedList <Event> schedule, LinkedList <State> state, double MAXTIME){
      
      double time = 0;
      int index = 0;
      
      while(time < MAXTIME) { 
        Event x = schedule.get(index);
        if(x.type == 0){ time = x.deathtime; }
        else{ time = x.arrivaltime; }
        
        x.function(schedule,state,time);
        index++;
      }
      return index;
    }
    
    public static void report(LinkedList <Event> schedule, LinkedList <State> state, int startIndex){
      
      // The list state contains only events that occured during the simulation
      int numevents = state.size();
      int index = startIndex;
      int count = 0;
      double avgTq = 0;
      Event curr;
      
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
      
      // We can now begin to compute the 95% confidence interval for Tq
      index = startIndex;
      count = 0;
      // Find the sample variance
      double Tqvar = 0;
      while(index < numevents){
        
        curr = schedule.get(index);
        // If the event is a death, extract birth and death times, and calculate Tq
        if(curr.type == 0){
          
          double tq = (curr.deathtime - curr.arrivaltime);
          Tqvar += ((tq - avgTq) * (tq - avgTq));
          count ++;
        }
        
        // If curr is a monitor or arrival, do nothing
        index++;
      }
     
      Tqvar = Tqvar/(count-1);
      //1.645 is the Z value for 95% confidence interval
      double ETq = 1.645 * (Tqvar / sqrt((double) count));
      
      // Using Little's law, we know that Tw = Tq - Ts
      double avgTw = (avgTq - (1.0 / schedule.getFirst().mean_ts));
      
      // Calculate average w and q using state
      index = 0;
      count = 0;
      double avgQ = 0;
      double avgW = 0;
      
      while(index < numevents){
        
        State currState = state.get(index);
        
        // Only consider Monitor events
        if(schedule.get(index).type == -1){
          avgQ += (currState.waiting + currState.served);
          avgW += currState.waiting;
          count++;
        }
          
        index++;
      }
      
      // Sample means
      avgQ = avgQ / count;
      avgW = avgW / count;
      
      // Calculate the 95% confidence interval for q
      index = 0;
      count = 0;
      double q;
      double qvar = 0;
      while(index < numevents){
        
        State currState = state.get(index);
        
        // Only consider Monitor events
        if(schedule.get(index).type == -1){
          q = (currState.waiting + currState.served);
          qvar += ((q - avgQ) * (q - avgQ));
          count++;
        }
        
        index++;
      }
      
      qvar = qvar/(count-1);
      //1.645 is the Z value for 95% confidence interval
      double Eq = 1.645 * (qvar / sqrt((double) count));
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("MM1_sim_log.txt", true));
        
        //Write the report to the file
        out.write("\nSpecifications for this system (according to this simulation): \n");
        out.write("Average arrival rate (lambda): " + (schedule.getFirst().lambda * 1000) + " events/second\n");
        out.write("Average service rate (Ts): " + (1.0 /schedule.getFirst().mean_ts) + " msecs\n");
        out.write("Average wait time (Tw): " + avgTw + " msecs\n");
        out.write("Average response time (Tq): " + avgTq + " msecs\n");
        out.write("95% confidence interval for Tq: [ " + avgTq + " - " + ETq + ", " + avgTq + " + " + ETq + " ]\n");
        out.write("Average number of process waiting (w): " + avgW + " processes\n");
        out.write("Average number of process in the system (q): " + avgQ + " processes\n");
        out.write("95% confidence interval for q: [ " + avgQ + " - " + Eq + ", " + avgQ + " + " + Eq + " ]\n");
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
      System.out.println("95% confidence interval for Tq: [ " + avgTq + " - " + ETq + ", " + avgTq + " + " + ETq + " ]");
      System.out.println("Average number of process waiting (w): " + avgW + " processes");
      System.out.println("Average number of process in the system (q): " + avgQ + " processes");
      System.out.println("95% confidence interval for q: [ " + avgQ + " - " + Eq + ", " + avgQ + " + " + Eq + " ]");
    }
}