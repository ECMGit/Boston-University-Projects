/* process.java - Creates several threads that all enter the same Critical Section. A special thread, SCHED, is used 
 * to schedule which process enters the CS first, based on the idea that lower number threads have higher 
 * priority (Ex: Priority(P0) > Priority(P5)).
 * 
 * The Critical Section of code is protected by the use of semaphores.
 * 
 * Code by Tim Duffy for CS350 - hw 08
 */

import java.util.concurrent.Semaphore;

public class process extends Thread{

	private int id;
	static volatile boolean[] V;		// Keeps track of who wants access to the CS
	static volatile Semaphore[] B;		// Semaphores to block processes
	static volatile Semaphore S = new Semaphore(1, false); // blocks for SCHED

	public process(int i, int n){
		id = i;
		V = new boolean[n];
		B = new Semaphore[n];

		for(int j = 0; j < n; j++){
			// Initialize and decrease number of permits to 0 - now SCHED will decide which request to serve
			B[j] = new Semaphore(1, false);
			B[j].drainPermits();
		}
	} 

	public void run(){
		// If this is the SCHED process, we do things differently
		if(id == -1){
			
			while(true){

				try { S.acquire(); }  // S has no permits until a process wakes SCHED up again
				catch (InterruptedException e) {}
				
				for(int i=0; i< V.length; i++){
					System.out.println("Process P" + i + " = " + V[i]);
				}
				
				// Determine the next process to enter the CS and release 1 permit for that semaphore
				// releases a permit for the process with smallest id and need for CS access
				boolean found = false;		// This loop will run until a request is served
				while(!found){
					for(int i=0; i< V.length; i++){
						if (V[i] == true){ B[i].release(); found = true; break; }
					}
				}
			}
		}

		// For our actual processes
		else{
			int iter;
			for(iter = 1; iter <= 5; iter++){

				// Request access to the CS
				System.out.println("Process P" + id + " is requesting the CS");
				V[id] = true;
				
				// This will block all processes except the one that SCHED allows to enter CS
				try { B[id].acquire(); } catch (InterruptedException e1) {}

				// Enter the CS 
				System.out.println("Process P" + id + " is in the CS");

				// Exit the CS
				System.out.println("Process P" + id + " is exiting the CS");
				V[id] = false;

				// Signal that SCHED is available to pick next process
				S.release(); // Release after CS to ensure only 1 process is in CS at a time
			}
		}
	}

	public static void main(String[] args){

		final int N = 3;	
		process p[] = new process[N];
		
		// Create SCHED process
		process SCHED = new process(-1, N);
		SCHED.start();
		
		for (int i = 0; i < N; i++){
			p[i] = new process(i, N);
			p[i].start();
		}
	}
}
