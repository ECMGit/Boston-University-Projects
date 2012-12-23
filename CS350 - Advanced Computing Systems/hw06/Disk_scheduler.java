/* Disk simulator - simulates a disk with lambda = 50, rotational delay = 2ms, and seek delay = 0.1 msec/track
 * Uses Disk_Event.java to schedule events and Disk_State.java record the current state of the system
 * 
 * Written by Tim Duffy for CS350 hw06
 */


import java.io.*;
import java.util.*;
import static java.lang.Math.*;

class Disk_Scheduler {  
    public static void main(String[] args) {
      
      // Create log file, or clear the one that already exists
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("Disk_sim_log.txt"));
        
        //Write out the specified string to the file
        out.write("Time \t\t\tw \t# served   total in system (q)");
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
     
      Scanner console = new Scanner(System.in);
      
      System.out.println("Welcome to the Disk Scheduler simulator!");
      System.out.println("What is the mean arrival rate (lambda) of requests (events / second)?");
      double LAMBDA = 50.0 / 1000; // convert to events/ msec
      int N = 500;
      double U = 2;
      double V = 0.1;
      
      System.out.println("What scheduling process would you like to use? (1) Random, (2) FCFS, (3) SCAN");
      int process = console.nextInt();
      
      System.out.println("How many requests do you wish to simulate?");
      int MAXREQUESTS = console.nextInt();
      
      LinkedList <Disk_State> state = initializeState();
      LinkedList <Disk_Event> queue = new LinkedList <Disk_Event>();
      LinkedList <Disk_Event> schedule = initializeSchedule(LAMBDA, N, U, V);
      
      System.out.println("System warming up - Please wait.");
      
      int simStartIndex = warmup(schedule, state, MAXREQUESTS, queue, process);
      
      System.out.println("System ready - Beginning simulation.");
      
      double time = 0;
      int index = simStartIndex;
      int numrequests = 0;
      
      while(numrequests <= MAXREQUESTS) { 
        Disk_Event x = schedule.get(index);
        if(x.type == 0){ time = x.deathtime; }
        else{ time = x.arrivaltime; }
        
        if(x.type == 1){ numrequests++; }
        
        x.function(schedule,state,time, queue, process);
        index++;
      }
      
      System.out.println("Simulation complete! Calculating results. \n");
      report(schedule, state, simStartIndex); // Prints out report of simulation
    }
    
    public static LinkedList <Disk_State> initializeState(){
      LinkedList <Disk_State> state = new LinkedList <Disk_State>();
      state.add(new  Disk_State(0.0, 0, 0));
      return state;
    }
    
    public static LinkedList <Disk_Event> initializeSchedule(double l, int N, double U, double V){
      LinkedList <Disk_Event> schedule = new LinkedList <Disk_Event>();
      Random rndm = new Random();
            
      double x = (-1* log(1-rndm.nextDouble())) / (1.0/250);
      Disk_Event frstMon = new Disk_Event(-1, x, 0, 0, 0, 0, l, N, U, V);
      schedule.add(frstMon);
      
      double frstArrtime = ((-1* log(1-(rndm.nextDouble()))) / l);
      int firsttrack = rndm.nextInt(N);
      Disk_Event frstArr = new Disk_Event(1, frstArrtime, 0, firsttrack, 1, 0, l, N, U, V);
      
      if(x < frstArrtime){ schedule.addLast(frstArr); }
      else{ schedule.addFirst(frstArr); }
      
      return schedule;
    }
    
    // Warms up the system: Runs for as long as the simulation does
    public static int warmup(LinkedList <Disk_Event> schedule, LinkedList <Disk_State> state, int MAXREQUESTS, LinkedList <Disk_Event> queue, int process){
      
      double time = 0;
      int index = 0;
      int numrequests = 0;
      
      while(numrequests <= MAXREQUESTS) { 
        Disk_Event x = schedule.get(index);
        if(x.type == 0){ time = x.deathtime; }
        else{ time = x.arrivaltime; }
        
        if(x.type == 1){ numrequests++; }
        
        x.function(schedule,state,time, queue, process);
        index++;
      }
      return index;
    }
    
    public static void report(LinkedList <Disk_Event> schedule, LinkedList <Disk_State> state, int startIndex){
      
      // The list state contains only events that occured during the simulation
      int numevents = state.size();
      int index = startIndex;
      Disk_Event curr;
      int countTq = 0;
      int countTs = 0;
      
      double avgTq = 0;
      double avgTs = 0;
      double totHeadMov = 0;
   
      // Calculate average Tq and average Ts
      while(index < numevents){
        
        curr = schedule.get(index);
        // If the event is a death, extract birth and death times, and calculate Tq
        if(curr.type == 0){
          avgTq += (curr.deathtime - curr.arrivaltime);
          countTq ++;
          
          // Add head movement needed for this request
          int diff = (curr.lasttrack - curr.track);
          if(diff < 0){ diff = diff * -1; }
          totHeadMov += diff;
        }
        
        if(curr.type == 1){
          avgTs += curr.TS;  // extract Ts
          countTs ++;
        }

        // If curr is a monitor or arrival, do nothing
        index++;
      }
      
      avgTq = avgTq / countTq;  // Sample mean
      avgTs = avgTs / countTs;
      
      // We can now begin to compute the 95% confidence interval for Tq
      index = startIndex;
      int count = 0;
      
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
      
      Tqvar = Tqvar/count;
      //1.645 is the Z value for 95% confidence interval
      double ETq = 1.645 * (Tqvar / sqrt((double) count));
      
      // Using Little's law, we know that Tw = Tq - Ts
      double avgTw = (avgTq - avgTs);
     
      
      // round all numbers off to 3 decimal places
      avgTs = Math.round(avgTs * 1000);
      avgTs = avgTs / 1000;
      avgTw = Math.round(avgTw * 1000);
      avgTw = avgTw / 1000;
      avgTq = Math.round(avgTq * 1000);
      avgTq = avgTq / 1000;
      ETq = Math.round(ETq * 1000);
      ETq = ETq / 1000;
      
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("Disk_sim_log.txt", true));
        
        //Write the report to the file
        out.write("\nSpecifications for this system (according to this simulation): \n");
        out.write("Average arrival rate (lambda): " + (schedule.getFirst().lambda * 1000) + " events/second\n");
        out.write("Total head movement was over " + (int)totHeadMov + " tracks\n");
        out.write("Average service rate (Ts): " + avgTs + " msecs\n");
        out.write("Average wait time (Tw): " + avgTw + " msecs\n");
        out.write("Average response time (Tq): " + avgTq + " msecs\n");
        out.write("95% confidence interval for Tq: [ " + avgTq + " - " + ETq + ", " + avgTq + " + " + ETq + " ]\n");
        out.close(); //flushes and closes the stream
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
      
      System.out.println("Specifications for this system (according to this simulation): ");
      System.out.println("Average arrival rate (lambda): " + (schedule.getFirst().lambda * 1000) + " events/second");
      System.out.println("Total head movement was over " + (int)totHeadMov + " tracks");
      System.out.println("Average service rate (Ts): " + avgTs + " msecs");
      System.out.println("Average wait time (Tw): " + avgTw + " msecs");
      System.out.println("Average response time (Tq): " + avgTq + " msecs");
      System.out.println("95% confidence interval for Tq: [ " + avgTq + " - " + ETq + ", " + avgTq + " + " + ETq + " ]");
    }
}