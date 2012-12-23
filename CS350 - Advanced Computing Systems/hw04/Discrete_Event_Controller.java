/* Discrete Event simulator - (Attemps, but fails to) simulate an system with specific arrival rate and service rates
 * This system has multiple components, including 3 connected queuing systems for CPU, Disk I/O, and Network.
 * Uses Discrete_Event.java to schedule events and Discrete_State.java record the current state of the system
 * 
 * Written by Tim Duffy for CS350 hw04
 */
import java.io.*;
import java.util.*;
import static java.lang.Math.*;

class Discrete_Event_Controller {  
    public static void main(String[] args) {
      
      // Create log file, or clear the one that already exists
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter("Discrete_Event_log.txt"));
        
        //Write out the specified string to the file
        out.write("Time \t\t\tw \t# served   total in system (q)\n");
        //flushes and closes the stream
        out.close();
      }
      
      catch(IOException e){
        System.out.println("There was a problem:" + e);
      }
     
      System.out.println("Welcome to the Discrete Event simulator!");
      System.out.println("This simulates a system that does the following:");
      System.out.println(" 1. The process uses the CPU and then with a probability 0.1 proceeds to step (2)");
      System.out.println("    with probability 0.4 proceeds to step (3),");
      System.out.println("    and with probability 0.5 proceeds to step (4).");
      System.out.println(" 2. The process performs a disk I/O and then with a probability 0.5 proceeds to step (1),");
      System.out.println("    and with probability 0.5 proceeds to step (3).");
      System.out.println(" 3. The process performs a network I/O and then proceeds to step (1).");
      System.out.println(" 4. The process is done!\n");
      System.out.println("This simulation uses the following assumptions:");
      System.out.println(" - Process arrivals are Poisson with a rate of 40 processes per second");
      System.out.println(" - CPU service time is uniformly distributed between 10 and 30 msec");
      System.out.println(" - Disk I/O service time is normally distributed with mean of 100 msec");
      System.out.println("   and standard deviation of 20 msec (but never negative)");
      System.out.println(" - Network service time is constant with mean of 25 msec.");
      System.out.println(" - All buffers are of infinite size\n");
      
      System.out.println("How long should this simulation run? (in seconds)");
      Scanner console = new Scanner(System.in);
      double MAXTIME = console.nextDouble() * 1000;
      
      LinkedList <Discrete_State> state = initializeState();
      LinkedList <Discrete_Event> schedule = initializeSchedule();
      System.out.println("System warming up - Please wait.");
      
      int simStartIndex = warmup(schedule, state, MAXTIME);
      
      System.out.println("System ready - Beginning simulation.");
      
      double time = 0;
      int index = simStartIndex;
      
      while(time < (2*MAXTIME)) { 
        Discrete_Event x = schedule.get(index);
        time = x.deathtime;
        x.function(schedule,state,time);
        System.out.println(index + " " + time);
        index++;
      }
      
      System.out.println("Simulation complete! Calculating results. \n");
      //report(schedule, state, simStartIndex); // Prints out report of simulation
    }
    
    public static LinkedList <Discrete_State> initializeState(){
      LinkedList <Discrete_State> state = new LinkedList <Discrete_State>();
      state.add(new Discrete_State(1, 0.0, 0, 0)); // CPU queue
      state.add(new Discrete_State(2, 0.0, 0, 0)); // Disk queue
      state.add(new Discrete_State(3, 0.0, 0, 0)); // Network queue
      return state;
    }
    
    public static LinkedList <Discrete_Event> initializeSchedule(){
      LinkedList <Discrete_Event> schedule = new LinkedList <Discrete_Event>();
      Random rndm = new Random();
            
      // Add Monitoring events for each of the 3 queues
      schedule.add(new Discrete_Event(-1, 1, 0.0, 0));
      schedule.add(new Discrete_Event(-1, 2, 0.0, 0));
      schedule.add(new Discrete_Event(-1, 3, 0.0, 0));
       
      // Add the first birth to the system
      double frstArrtime = (-1* log(1-(rndm.nextDouble()))) / (40.0/1000);
      Discrete_Event frstArr = new Discrete_Event(1, 1, frstArrtime, frstArrtime);
      schedule.addFirst(frstArr);
      
      return schedule;
    }
    
    // Warms up the system: Runs for as long as the simulation does
    public static int warmup(LinkedList <Discrete_Event> schedule, LinkedList <Discrete_State> state, double MAXTIME){
      
      double time = 0;
      int index = 0;
      
      while(time < MAXTIME) { 
        Discrete_Event x = schedule.get(index);
        time = x.deathtime;
        x.function(schedule,state,time);
        index++;
      }
      return index;
    }
}