/* M/M/1 Event - contains code to schedule new birth/death events, and update system state.
 * Used by MM1-controller.java and along with State.java 
 * 
 * Written by Tim Duffy for CS350 hw04
 */

import java.util.*;
import static java.lang.Math.*;

public class Event {

  int type; //(-1) if this is a monitor event, (0) for a death event, or (1) for a birth event
  
  double arrivaltime;
  double deathtime;
  double lambda;
  double mean_ts;
  int k;
  boolean fixedTs;
  
    public Event(int type, double arrivaltime, double deathtime, double l, double mean_ts, int k, boolean fixedTs){
      this.type = type; 
      this.arrivaltime = arrivaltime;
      this.deathtime = deathtime;
      this.lambda = l;
      this.mean_ts = mean_ts;
      this.k = k;
      this.fixedTs = fixedTs;
    }
    
    public void function(LinkedList <Event> schedule, LinkedList <State> state, double time){
      
      State m = state.getLast();   // Current state of the system (before this event takes place)
      Random rndm = new Random();
      
      switch(this.type){
          
        // If this event is a birth event
        case 1: 
          
          // First, check to see if the queue has rom for another process. If so, update the state appropriately.
          // Otherwise, do nothing (reject arrival)
          if(m.waiting < this.k){   
          
          // If no processes are being served, this process goes right to processor
          if(m.idle == true) { state.addLast(new State(time, 1, 0)); }
          //Otherwise the process waits in the queue behind all others
          else{ state.addLast( new State(time, 1, (m.waiting + 1))); }
  
          // Next, schedule the death of the process
          double newTs;
          if(fixedTs == true){ newTs = (1.0 / mean_ts); } // If the service time is fixed, set it
          else { newTs = (-1* log(1-rndm.nextDouble())) / mean_ts; } // Otherwise generate a random Ts
          
          // If the system is idle, the death will occur at time = (arrivaltime + service time)
          if(m.idle == true) { this.deathtime = this.arrivaltime + newTs; }
          // Otherwise, the death occurs at time = (time of last death + service time)
          else{
            
            int index = schedule.size()-1;
            Event lastdeath = schedule.getLast();
            
            while (lastdeath.type != 0){
              lastdeath = schedule.get(index-1);
              index--;
            }
            this.deathtime = lastdeath.deathtime + newTs;
          }
          
          // Create this death event and add to the schedule
          Event nextdeath = new Event(0, this.arrivaltime, this.deathtime, this.lambda, this.mean_ts, this.k, this.fixedTs);
          sched(schedule, nextdeath);
          
        }
          
          // Create a new birth event for the next process, then add it to the schedule
          // This occurs regardless of the acceptance/rejection of the last process
          double newIAT = (-1* log(1-rndm.nextDouble())) / this.lambda;
          // The next arrival time will occurr at this.arrivaltime + newIAT
          Event nextbirth = new Event(1, (this.arrivaltime + newIAT), 0, this.lambda, this.mean_ts, this.k, this.fixedTs);
          sched(schedule, nextbirth);
        
          break;
          
          // If this event is a death event, we update the state appropriately
        case 0:
          
          // If this process is the only one in the system, then after its death, the system will be empty
          if (m.served == 1 && m.waiting == 0) {
            state.addLast(new State(time, 0, 0)); 
          }
          
          // Otherwise, this process leaves, and the next process starts its service (q decreases by 1)
          else {
            state.addLast(new State(time, m.served, (m.waiting - 1)));
          }
          
          break;
          
          // If this event is a monitoring event, output the state of the system and schedule a new Monitor event
          case -1:
            
            //Create a new state: everything is the same except the time
            State currState = new State(this.arrivaltime, m.served, m.waiting);
            state.addLast(currState);
            
            currState.output();
          
            // Then scheule a new monitor event (average of 1 monitor event every 0.25 seconds)
            double x = (-1* log(1-rndm.nextDouble())) / (1.0/250);
            Event newMon = new Event(-1, (this.arrivaltime + x), 0, this.lambda, this.mean_ts, this.k, this.fixedTs);
          
            sched(schedule, newMon);
            break;
      }
    }
    
    public void sched(LinkedList <Event> schedule, Event newevent){
      
      // schedule will grow quite long, so it is best to start looking at the end
      int index = schedule.size()-1;
      double newTime;
      
      // Store the time of the new event 
      if(newevent.type == 0) { newTime = newevent.deathtime; }
      else{ newTime = newevent.arrivaltime; }
      
      // Add new event at correct position (sorted by time)
      double lasteventtime;
      if(schedule.getLast().type == 0) { lasteventtime = schedule.get(index).deathtime; }
      else{ lasteventtime = schedule.get(index).arrivaltime; }
      
      
      while((newTime < lasteventtime) && (index > 0)) {
        
        index--;
        if(schedule.get(index).type == 0) { lasteventtime = schedule.get(index).deathtime; }
        else{ lasteventtime = schedule.get(index).arrivaltime; }
      }
      
      schedule.add(index+1, newevent);
    }
}