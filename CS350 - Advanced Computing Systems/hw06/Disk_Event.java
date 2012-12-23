/* Disk_Event - contains code to schedule new birth/death events, and update system state.
 * Used by Disk-controller.java and along with Disk_State.java 
 * 
 * Written by Tim Duffy for CS350 hw06
 */

import java.util.*;
import static java.lang.Math.*;

public class Disk_Event {
  
  int type; //(-1) if this is a monitor event, (0) for a death event, or (1) for a birth event
  
  double arrivaltime;
  double deathtime;
  int track;
  int lasttrack;
  int direction; // 1 = forward (increasing), -1 = backwards (decreasing)
  double lambda;
  int N;
  double U;
  double V;
  double TS; // for reporting at end of simulation
  double queuesize;
  
  public Disk_Event(int type, double arrivaltime, double deathtime, int track, int lasttrack, int direction, double l, int N, double U, double V){
    this.type = type; 
    this.arrivaltime = arrivaltime;
    this.deathtime = deathtime;
    this.track = track;
    this.lasttrack = lasttrack;
    this.direction = direction;
    this.lambda = l;
    this.N = N;
    this.U = U;
    this.V = V;
  }
  
  public void function(LinkedList <Disk_Event> schedule, LinkedList <Disk_State> state, double time, LinkedList <Disk_Event> queue, int process){
    
    Disk_State m = state.getLast();   // Current state of the system (before this event takes place)
    Random rndm = new Random();
    
    switch(this.type){
      
      // If this event is a birth event
      case 1:
        
        // If no processes are being served, this process goes right to processor
        if(m.idle == true) { state.addLast(new Disk_State(time, 1, 0)); }
        //Otherwise the process waits in the queue behind all others
        else{ state.addLast( new Disk_State(time, 1, (m.waiting + 1))); }
        
        // Next, create Ts for this process
        int diff = (this.lasttrack - this.track);
        if(diff < 0){ diff = diff * -1; }
        
        double newTs = U + (V * diff);
        this.TS = newTs;
        
        if(m.idle == true){ queue.clear(); }
        
        // Add request to queue
        queue.addLast(this);
        
        // If this is the only request in the system begin service deathtime = (arrivaltime + service time)
        if(queue.size() == 1) {
          this.deathtime = this.arrivaltime + newTs;
          // Create this death event and add to the schedule
          Disk_Event nextdeath = new Disk_Event(0, this.arrivaltime, this.deathtime, this.track, this.lasttrack, this.direction, this.lambda, this.N, this.U, this.V);
          sched(schedule, nextdeath);
        }
        // Otherwise, the death occurs at a later time (depending on scheduling process)
        
        // Create a new birth event for the next process, then add it to the schedule
        double newIAT = (-1* log(1-rndm.nextDouble())) / this.lambda;
        // The next arrival time will occurr at this.arrivaltime + newIAT
        Disk_Event nextbirth = new Disk_Event(1, (this.arrivaltime + newIAT), 0, (rndm.nextInt(this.N)), this.track, this.direction, this.lambda, this.N, this.U, this.V);
        sched(schedule, nextbirth);
        
        break;
        
      // If this event is a death event, we update the state, and select next request
      case 0:
        
        // If this process is the only one in the system, then after its death, the system will be empty
        if (m.served == 1 && m.waiting == 0){
        state.addLast(new Disk_State(time, 0, 0)); 
        }
        
        // Otherwise, this process leaves, and the next process starts its service (q decreases by 1)
        else { state.addLast(new Disk_State(time, m.served, (m.waiting - 1))); }
        
        // remove the event from the queue
        double eventtime = this.arrivaltime;
        int index = 0;
        
        while(index < queue.size()){
          if(queue.get(index).arrivaltime == eventtime){ queue.remove(index);}
          index++;
        }
        
        this.queuesize = queue.size();
        
        // Now check the queue for any more requests
        if(queue.size() > 0){
          
          Disk_Event nextrequest = queue.getFirst();
          
          // Random - pick a request at random
          if(process == 1){
            int nextindex = rndm.nextInt(queue.size());
            nextrequest = queue.remove(nextindex);
          }
          
          // SCAN
          else if(process == 3){
            
            index = 0;
            int currpos = this.lasttrack;
            int direction = this.direction;
            boolean found = false;
            
            // If the head is moving forward, look for requests closest to current location, while moving forward
            if(direction == 1) {
              while(index < queue.size()){
                if((queue.get(index).track > currpos) && (queue.get(index).track < nextrequest.track)){
                  nextrequest = queue.get(index);
                  found = true;
                }
                index++;
              }
            }
            
            // if there are no other requests in the forward direction, search for requests in the opposite direction
            if((found = false) && (queue.getFirst().track < currpos)){ direction = -1; }

            index = 0;
            if(direction == -1){
              while(index < queue.size()){
                if((queue.get(index).track < currpos) && (queue.get(index).track < nextrequest.track)){
                  nextrequest = queue.get(index);
                  queue.remove(index);
                  found = true;
                }
                index++;
              }
            }
            
            // if there are no other requests in the backwards direction, search for requests in the other direction
            if((found = false) && (queue.getFirst().track > currpos)){ direction = -1; }
          }
          
          // Schedule death (last death [this one] + Ts)
          Disk_Event nextdeath = new Disk_Event(0, nextrequest.arrivaltime, (this.deathtime + nextrequest.TS), nextrequest.track, nextrequest.lasttrack, nextrequest.direction, this.lambda, this.N, this.U, this.V);
          sched(schedule, nextdeath);
        }
        
        break;
        
        // If this event is a monitoring event, output the state of the system and schedule a new Monitor event
      case -1:
        
        //Create a new state: everything is the same except the time
        Disk_State currState = new Disk_State(this.arrivaltime, m.served, m.waiting);
        state.addLast(currState);
        
        currState.output();
        
        // Then scheule a new monitor event (average of 1 monitor event every 0.25 seconds)
        double x = (-1* log(1-rndm.nextDouble())) / (1.0/250);
        Disk_Event newMon = new Disk_Event(-1, (this.arrivaltime + x), 0, this.track, this.track, this. direction, this.lambda, this.N, this.U, this.V);
        
        sched(schedule, newMon);
        break;
    }
  }
  
  public void sched(LinkedList <Disk_Event> schedule, Disk_Event newevent){
    
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