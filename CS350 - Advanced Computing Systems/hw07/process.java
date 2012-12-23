/* Process.java - Creates several threads that all run at the same time, each accessing a critical section of code.
 * 
 * Tim Duffy - written for CS350 hw07
 */

import java.util.Random;

class process extends Thread{
	private int id;
	private int j;
	private int N;
	static volatile boolean[] flag;
	static volatile int[] ticket;
	static volatile boolean[] choosing;
	static volatile int turn;
	private int avgloopexec; 

	public process(int i, int n){  
		id = i;
		N = n;
		flag = new boolean [N];
		ticket = new int [N];
		choosing = new boolean [N];

		if(i == 1) { j = 0; }
		else{ j = 1; }
	}

	public void run(){
		Random rndm = new Random();

		int iter;
		for(iter = 1; iter <= 5; iter++){


			/* Entry protocol for question 1b
			flag[id] = true;
			while (flag[j] == true){
				if (turn != id) {
					flag[id]=false;
					while (turn != id) {};
					flag[id]=true;
				}
			} 

			// Entry protocol for Q1c
			flag[id] = true;
			while (flag[j] == true){
				if (turn != id) {
					flag[id]=false;
					while (turn != id) {};
					try { sleep(1000 * id); }
					catch (Exception e) { System.out.println(e); }
					flag[id]=true;
				}
			}
			 */

			// Entry protocol for question 1d 
			turn = j;
			flag[id] = true;
			while (flag[j] && turn == j) { this.avgloopexec++; };

			/* Entry protocol for Q2

			ticket[id]=1;

			// Find max ticket
			int maximum = ticket[0]; 
			for (int i=0; i<ticket.length; i++) {
				if (ticket[i] > maximum) {
					maximum = ticket[i];   // new maximum
				}
			}
			ticket[id] = maximum + 1;

			for(int k=0; k<N; k++){
			while(ticket[k]!=0 && ((ticket[k] < ticket[id]) ||
				((ticket[k]==ticket[id]) && (k < id)) ) ){};
			}
			 */

			System.out.println("Thread " + id + " is starting iteration " + iter);
			try { sleep(rndm.nextInt(21)); }
			catch (Exception e) { System.out.println(e); }

			System.out.println("We hold these truths to be self-evident, that all men are created equal,");
			try { sleep(rndm.nextInt(21)); }
			catch (Exception e) { System.out.println(e); }

			System.out.println("that they are endowed by their Creator with certain unalienable Rights,");
			try { sleep(rndm.nextInt(21)); }
			catch (Exception e) { System.out.println(e); }

			System.out.println("that among these are Life, Liberty and the pursuit of Happiness.");
			try { sleep(rndm.nextInt(21)); }
			catch (Exception e) { System.out.println(e); }

			System.out.println("Thread " + id + " is done with iteration " + iter);

			/* Exit protocol for question 1b and 1c
			turn = j;
			flag[id] = false;
			 */

			// Exit protocol for question 1d
			flag[id] = false;
			
			/* Exit protocol for question 2
			ticket[id] = 0;	
			*/
		} 

		// Calculate average loop executions per CS request
		this.avgloopexec = this.avgloopexec/iter;
		System.out.println( "+~+~ For process " + id + " the average number busy-wait loopsper CS request is " + avgloopexec + " ~+~+\n");
	}

	public static void main(String[] args) {

		final int N = 2;	
		process p[] = new process[N];

		for (int i = 0; i < N; i++){
			p[i] = new process(i, N);
			p[i].start();
		}
	}
}