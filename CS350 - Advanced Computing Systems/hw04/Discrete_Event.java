/* Discrete Event - contains code to schedule new birth/death events, and update system state.
 * Used by Discrete_Event_controller.java along with Discrete_State.java 
 * 
 * Written by Tim Duffy for CS350 hw04
 */

import java.util.*;
import static java.lang.Math.*;

public class Discrete_Event {

  int type; //(-1) if this is a monitor event, (0) for a death event, or (1) for a birth event
  int list; // 1-CPU, 2-Disk, 3-Network
  double arrivaltime;
  double deathtime;
  double mean_ts;
  int k;
  boolean fixedTs;
  
    public Discrete_Event(int type, int list, double arrivaltime, double deathtime){
      this.type = type;
      this.list = list;
      this.arrivaltime = arrivaltime;
      this.deathtime = deathtime;
      this.mean_ts = mean_ts;
      this.k = k;
      this.fixedTs = fixedTs;
    }
    
    public void function(LinkedList <Discrete_Event> schedule, LinkedList <Discrete_State> state, double time){
      // Get last state for CPU
      int index = state.size()-1;
      Discrete_State m1 = state.get(index);
      while(state.get(index).type != 1){
        index--;
        m1 = state.get(index);
      }
      // Last state for Disk
      index = state.size()-1;
      Discrete_State m2 = state.get(index);
      while(state.get(index).type != 2){
        index--;
        m2 = state.get(index);
      }
      // Last state for network queue
      index = state.size()-1;
      Discrete_State m3 = state.get(index);
      while(state.get(index).type != 3){
        index--;
        m3 = state.get(index);
      }
      
      Random rndm = new Random();
      
      switch(this.type){
          
        // If this event is a birth event
        case 1:
          
          // For events arriving to the CPU
          if(this.list == 1){
          
            // If no processes are being served, this process goes right to processor
            if(m1.idle == true) { state.addLast(new Discrete_State(1, time, 1, 0)); }
            //Otherwise the process waits in the queue behind all others
            else{ state.addLast( new Discrete_State(1, time, 1, (m1.waiting + 1))); }
  
            // Next, schedule the death of the process
            double newTs = 10.0 + (rndm.nextInt(20)); // Generate a random Ts between 10 and 30 msecs
            double newdeathtime = 0;
            // If the system is idle, the death will occur at time = (arrivaltime + service time)
            if(m1.idle == true) { newdeathtime = this.deathtime + newTs; }
            // Otherwise, the death occurs at time = (time of last death from CPU + service time)
            else{
              
              index = schedule.size()-1;
              Discrete_Event lastdeath = schedule.getLast();
              
              while (lastdeath.type != 0 && lastdeath.list != 1){
                lastdeath = schedule.get(index-1);
                index--;
              }
              newdeathtime = lastdeath.deathtime + newTs;
            }
            
            // Create this death event and add to the schedule
            Discrete_Event nextdeath = new Discrete_Event(0, 1, this.arrivaltime, newdeathtime);
            sched(schedule, nextdeath);
            
            // Create a new birth event for the next process, then add it to the schedule
            if(this.deathtime == this.arrivaltime){
              double newIAT = (-1* log(1-rndm.nextDouble())) / (40.0/1000);
              // The next arrival time will occurr at this.arrivaltime + newIAT
              Discrete_Event nextbirth = new Discrete_Event(1, 1, (this.arrivaltime + newIAT), (this.arrivaltime + newIAT));
              sched(schedule, nextbirth);
            }
          }
          
          // For events arriving to the Disk
          else if(this.list == 2){
            
            // If no processes are being served by the disk, this process goes right to processor
            if(m2.idle == true) { state.addLast(new Discrete_State(2, time, 1, 0)); }
            //Otherwise the process waits in the queue behind all others
            else{ state.addLast( new Discrete_State(2, time, 1, (m2.waiting + 1))); }
            
            // Next, schedule the death of the process
            double newTs = (-1* log(1-(rndm.nextDouble()))) / (1.0/100); // Generate a random Ts with mean 100 msec
            double newdeathtime = 0;
            // Note: this.deathtime is the time the process arrives to the disk queue
            // If the system is idle, the death will occur at time = (deathtime + service time)
            if(m2.idle == true) { newdeathtime = this.deathtime + newTs; }
            // Otherwise, the death occurs at time = (time of last death from disk + service time)
            else{
              index = schedule.size()-1;
              Discrete_Event lastdeath = schedule.getLast();
              
              while (lastdeath.type != 0 && lastdeath.list != 2){
                lastdeath = schedule.get(index-1);
                index--;
              }
              newdeathtime = lastdeath.deathtime + newTs;
            }
            
            // Create this death event and add to the schedule
            Discrete_Event nextdeath = new Discrete_Event(0, 2, this.arrivaltime, newdeathtime);
            sched(schedule, nextdeath);
          }
          
          // For events arriving to the Network
          else if(this.list == 3){
            
            // If no processes are being served by the disk, this process goes right to processor
            if(m3.idle == true) { state.addLast(new Discrete_State(3, time, 1, 0)); }
            //Otherwise the process waits in the queue behind all others
            else{ state.addLast( new Discrete_State(3, time, 1, (m3.waiting + 1))); }
            
            // Next, schedule the death of the process
            double newTs = 25; // Constant Ts with mean of 25 msec
            double newdeathtime = 0.0;
            // Note: this.deathtime is the time the process arrives to the network queue
            // If the system is idle, the death will occur at time = (deathtime + service time)
            if(m3.idle == true) { newdeathtime = this.deathtime + newTs; }
            // Otherwise, the death occurs at time = (time of last death from network + service time)
            else{ 
              index = schedule.size()-1;
              Discrete_Event lastdeath = schedule.getLast();
              
              while (lastdeath.type != 0 && lastdeath.list != 3){
                index--;
                lastdeath = schedule.get(index);
              }
              this.deathtime = lastdeath.deathtime + newTs;
            }
            
            // Create this death event and add to the schedule
            Discrete_Event nextdeath = new Discrete_Event(0, 3, this.arrivaltime, newdeathtime);
            sched(schedule, nextdeath);
          }
          break;
          
          // If this event is a death event, we update the state appropriately
        case 0:
          
          // For processes leaving the CPU
          if(this.list == 1){
            // If this process is the only one in the system, then after its death, the system will be empty
            if (m1.served == 1 && m1.waiting == 0) { state.addLast(new Discrete_State(1, time, 0, 0)); }
            
            // Otherwise, this process leaves, and the next process starts its service (q decreases by 1)
            else { state.addLast(new Discrete_State(1, time, m1.served, (m1.waiting - 1))); }
            
            // Now we decide where to send this process: P(sent to Disk) = 10%, P(Sent to network) = 40% P(Done) = 50%
            int where = 1 + rndm.nextInt(10); // evenly distributed numbers 1-10
            
            // If we roll a 1, send process to disk. If we roll 2,3,4,or 5 send to network. If we roll 6 or higher, the process finishes.
            if(where == 1){ sched(schedule, (new Discrete_Event(1, 2, this.arrivaltime, this.deathtime))); }
            // If process is done, add to schedule as a completed process
            else if(where > 5){ sched(schedule, (new Discrete_Event(1, 4, this.arrivaltime, this.deathtime))); }
            else{ sched(schedule, (new Discrete_Event(1, 3, this.arrivaltime, this.deathtime))); }
          }
          
          // For processes leaving the disk
          if(this.list == 2){
            // If this process is the only one in the system, then after its death, the system will be empty
            if (m2.served == 1 && m2.waiting == 0) { state.addLast(new Discrete_State(2, time, 0, 0)); }
            
            // Otherwise, this process leaves, and the next process starts its service (q decreases by 1)
            else { state.addLast(new Discrete_State(2, time, m2.served, (m2.waiting - 1))); }
            
            // Now we decide where to send this process: P(sent to CPU) = 50%, P(Sent to network) = 50%
            int where = 1 + rndm.nextInt(9); // evenly distributed integers 1-10
            
            // If we roll a 1,2,3,4 or 5 send process to CPU. If we roll 5 or higher, the process is sent to the Network.
            if(where < 6){ sched(schedule, (new Discrete_Event(1, 1, this.arrivaltime, this.deathtime))); }
            else{ sched(schedule, (new Discrete_Event(1, 3, this.arrivaltime, this.deathtime))); }
          }
          
          // For processes leaving the network
          if(this.list == 3){
            // If this process is the only one in the system, then after its death, the system will be empty
            if (m3.served == 1 && m3.waiting == 0) { state.addLast(new Discrete_State(3, time, 0, 0)); }
            
            // Otherwise, this process leaves, and the next process starts its service (q decreases by 1)
            else { state.addLast(new Discrete_State(3, time, m3.served, (m3.waiting - 1))); }
            
            // Now we decide where to send this process: P(sent to CPU) = 100%
            sched(schedule, (new Discrete_Event(1, 1, this.arrivaltime, this.deathtime)));
          }
          break;
          
          // If this event is a monitoring event, output the state of the system and schedule a new Monitor event
          case -1:
            if(this.list == 1){
            // Monitor the CPU
            Discrete_State cpuState = new Discrete_State(1, this.arrivaltime, m1.served, m1.waiting);
            state.addLast(cpuState);
            
            cpuState.output();

            // Then scheule a new monitor event 1 second later
            Discrete_Event newMon = new Discrete_Event(-1, 1, (this.arrivaltime + 1000), (this.arrivaltime + 1000));
            sched(schedule, newMon);
            }
            // Monitor the Disk
            else if(this.list == 2){
              
              Discrete_State diskState = new Discrete_State(2, this.arrivaltime, m2.served, m2.waiting);
              state.addLast(diskState);
           
              diskState.output();
              
              // Then scheule a new monitor event 1 second later
              Discrete_Event newMon = new Discrete_Event(-1, 2, (this.arrivaltime + 1000), (this.arrivaltime + 1000));
              sched(schedule, newMon); 
            }
            else{
              Discrete_State networkState = new Discrete_State(3, this.arrivaltime, m3.served, m3.waiting);
              state.addLast(networkState);
            
              networkState.output();
              
              // Then scheule a new monitor event 1 second later
              Discrete_Event newMon = new Discrete_Event(-1, 3, (this.arrivaltime + 1000), (this.arrivaltime + 1000));
              sched(schedule, newMon);
            }
              break;
          
          // If this event is finished, do nothing!
          case 4:
            break;
        }
    }
    
    public void sched(LinkedList <Discrete_Event> schedule, Discrete_Event newevent){
      
      // schedule will grow quite long, so it is best to start looking at the end
      int index = schedule.size()-1;
      double newTime;
      
      // Always schedule using death time - new processes will initially have arrivaltime = deathtime
      newTime = newevent.deathtime;
      
      // Add new event at correct position (sorted by time)
      double lasteventtime = schedule.get(index).deathtime;
      
      while((newTime < lasteventtime) && (index > 0)) {
        index--;
        lasteventtime = schedule.get(index).deathtime;
      }
      
      schedule.add(index+1, newevent);
    }
}