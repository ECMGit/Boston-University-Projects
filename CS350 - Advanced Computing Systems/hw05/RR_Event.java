/* M/M/1 Event - contains code to schedule new birth/death events, and update system state.
 * Used by MM1-controller.java and along with State.java 
 * 
 * Written by Tim Duffy for CS350 hw04
 */

import java.util.*;
import static java.lang.Math.*;

public class RR_Event {

  //(-1) if this is a monitor event, (0) for a death event, or (1) for a new birth event, or (2) for a timeout
  int type; 
  
  double arrivaltime;
  double deathtime;
  double Ts;
  double timeGiven;
  double lambda;
  double mean_ts;
  double c;
  
    public RR_Event(int type, double arrivaltime, double deathtime, double Ts, double timeGiven, double l, double mean_ts, double c){
      this.type = type; 
      this.arrivaltime = arrivaltime;
      this.deathtime = deathtime;
      this.Ts = Ts;
      this.timeGiven = timeGiven; 
      this.lambda = l;
      this.mean_ts = mean_ts;
      this.c = c;
    }
    
    public void function(LinkedList <RR_Event> schedule, LinkedList <State> state, double time){
      
      State m = state.getLast();   // Current state of the system (before this event takes place)
      Random rndm = new Random();
      
      switch(this.type){
          
        // If this event is a birth event
        case 1: 
          
          // If no processes are being served, this process goes right to processor
          if(m.idle == true) {
            state.addLast(new State(1, time, 1, 0));
        
            // If one slice is enough, schedule death.
             RR_Event nextdeath;
            if(c >= this.Ts){
              this.timeGiven = this.Ts; // process is done
              double newdeathtime = this.arrivaltime + this.Ts;
              nextdeath = new RR_Event(0, this.arrivaltime, newdeathtime, this.Ts, this.timeGiven, this.lambda, this.mean_ts, this.c);
              sched(schedule, nextdeath);
            }
            // Otherwise, timeout the process
            else{
              RR_Event retProc = new RR_Event(2, this.arrivaltime, this.arrivaltime, this.Ts, this.timeGiven, this.lambda, this.mean_ts, this.c);
              sched(schedule, retProc);
            }
          }
          
          // If, upon the arrival of this event, the system is NOT idle, it waits at the end of the queue
          else{
            state.addLast( new State(1, time, 1, (m.waiting + 1)));
            
            RR_Event lastevent = schedule.getLast();
            int index = schedule.size()-1;
            
            while (lastevent.type != 0 && lastevent.type != 2){
              lastevent = schedule.get(index-1);
              index--;
            }
            
            // If one quantum is enough to finish service, we can schedule its death after the 
            // last death or timeout already in the schedule
            if(c >= this.Ts){
              RR_Event nextdeath = new RR_Event(0, this.arrivaltime, (lastevent.deathtime + this.Ts), this.Ts, this.timeGiven, this.lambda, this.mean_ts, this.c);
              sched(schedule, nextdeath);
            }
            
            // Otherwise, schedule a timeout 
            else{
              RR_Event waitProc = new RR_Event(2, this.arrivaltime, lastevent.deathtime, this.Ts, 0.0, this.lambda, this.mean_ts, this.c);
              sched(schedule, waitProc);
            }
          }
          
          // Create a new birth event for the next process, then add it to the schedule
          double newIAT = (-1* log(1-rndm.nextDouble())) / this.lambda;
          double newTs = (-1* log(1-rndm.nextDouble())) / mean_ts;
          
          // The next arrival time will occurr at this.arrivaltime + newIAT
          RR_Event nextbirth = new RR_Event(1, (this.arrivaltime + newIAT), 0.0, newTs, 0.0, this.lambda, this.mean_ts, this.c);
          sched(schedule, nextbirth);
        
          break;
          
        // For events that have timed out  
        case 2:
          // If one time slice is enough, schedule its death
          if(c >= (this.Ts-this.timeGiven)){
              double newdeathtime = this.deathtime + (this.Ts-this.timeGiven);
              this.timeGiven = this.Ts; // Process is done using resource 
              RR_Event nextdeath = new RR_Event(0, this.arrivaltime, newdeathtime, this.timeGiven, this.Ts, this.lambda, this.mean_ts, this.c);
              sched(schedule, nextdeath);
            }
            // Otherwise, give one time slice, and then timeout process
            else{
              RR_Event lastevent = schedule.getLast();
              int index = schedule.size()-1;
              
              while (lastevent.type != 0 && lastevent.type != 2){
                lastevent = schedule.get(index-1);
                index--;
              }
              
              RR_Event retProc = new RR_Event(2, this.arrivaltime, (lastevent.deathtime + this.c), this.Ts, (this.timeGiven + this.c), this.lambda, this.mean_ts, this.c);
              sched(schedule, retProc);
            }
          break;
          
        // If this event is a death event, we update the state appropriately
        case 0:
          
          // If this process is the only one in the system, then after its death, the system will be empty
          if (m.served == 1 && m.waiting == 0) {
            state.addLast(new State(0, time, 0, 0)); 
          }
          
          // Otherwise, this process leaves, and the next process starts its service (q decreases by 1)
          else {
            state.addLast(new State(0, time, m.served, (m.waiting - 1)));
          }
          
          break;
          
          // If this event is a monitoring event, output the state of the system and schedule a new Monitor event
          case -1:
            
            //Create a new state: everything is the same except the time
            State currState = new State(-1, this.arrivaltime, m.served, m.waiting);
            state.addLast(currState);
            
            currState.output();
          
            // Then scheule a new monitor event (average of 1 monitor event every 0.05 seconds)
            double x = (-1* log(1-rndm.nextDouble())) / (1.0/50);
            RR_Event newMon = new RR_Event(-1, (this.arrivaltime + x), 0, 0.0, 0.0, this.lambda, this.mean_ts, this.c);
          
            sched(schedule, newMon);
            break;
      }
    }
    
    public void sched(LinkedList <RR_Event> schedule, RR_Event newevent){
      
      // schedule will grow quite long, so it is best to start looking at the end
      int index = schedule.size()-1;
      double newTime;
      
      // Store the time of the new event 
      if(newevent.type == 0 || newevent.type == 2) { newTime = newevent.deathtime; }
      else{ newTime = newevent.arrivaltime; }
      
      // Add new event at correct position (sorted by time)
      double lasteventtime;
      if(schedule.getLast().type == 0 || schedule.getLast().type == 2) { lasteventtime = schedule.get(index).deathtime; }
      else{ lasteventtime = schedule.get(index).arrivaltime; }
      
      
      while((newTime < lasteventtime) && (index > 0)) {
        
        index--;
        if(schedule.get(index).type == 0 || schedule.get(index).type == 2) { lasteventtime = schedule.get(index).deathtime; }
        else{ lasteventtime = schedule.get(index).arrivaltime; }
      }
      
      schedule.add(index+1, newevent);
    }
}