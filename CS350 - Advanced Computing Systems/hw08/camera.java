/* camera.java - Creates several client threads that request the use of a camera at specific angles. A special thread called 
 * cam is used to schedule the order requests are served based on FSCAN protocol.  
 * 
 * The use of the camera is protected by semaphores.
 * 
 * Code by Tim Duffy for CS350 - hw 08
 */
import java.util.Random;
import java.util.concurrent.Semaphore;

public class camera extends Thread{

	private int id;
	// An array to hold angle requests for each user. Camera angles will range from 1 to 8, so 
	// a request of 0 will simply mean that no request is being made.
	static volatile int[] req;
	static volatile int last = 0;     // last camera angle
	volatile boolean direction = true; // Direction camera is sweeping (true -> ascending, false -> descending)	
	static volatile Semaphore[] B; // Holds semaphores that block for clients
	static volatile Semaphore cam = new Semaphore(1, false); // Blocks for camera

	public camera(int i, int n){
		id = i;
		req = new int[n];
		B = new Semaphore[n];
		
		// Reduce number of available permits to 0
		if(id == -1){ cam.drainPermits(); }
		
		for(int j = 0; j < n; j++){
			// Initialize and decrease number of permits to 0 - now cam will decide which request to serve
			B[j] = new Semaphore(1, false);
			B[j].drainPermits();
		}
	}

	public void run(){
		
		Random rndm = new Random();
		
		if(id == -1){

			while(true){
				
				try { cam.acquire(); }
				catch (InterruptedException e) {}
			
				// Now we select the next request	
				int best = 100; // Top pick for next request to be served - initialized to some arbitrary large number

				while(best == 100 || best == -1){
					if(direction){ // if we are going in ascending order
						for(int j = 0; j < req.length; j++){
							// If req[j] has a request for an angle greater then our last angle, and smaller than 
							// our current best fit, update the best 
							if((req[j] > last) && (req[j] < best)){ best = req[j];} 
						}
					}

					// At this point, we have scanned all requests. If no best was found, it means that there are
					// no requests in the forward direction. Now we should look in the other direction
					if(best == 100){ best = -1; direction = false; }

					// Search for closes angle in backwards direction
					if(direction == false){
						for(int j = 0; j < req.length; j++){

							// If req[j] has a request for an angle less then our last angle, and greater than 
							// our current best fit, update the best. Remember to ignore 0 (which means no request)
							if((req[j] < last) && (req[j] > best) && (req[j] != 0)){ best = req[j];} 
						}
						
						// If best is still -1, then there are no more requests in the backwards direction.
						if(best == -1){ direction = true; best = 100; }
					}
				}
				
				// We now have a new angle, so the camera delays while moving to the new position
				System.out.println("Now moving to angle " + best);
				double diff = Math.sqrt((double)((best - last)*(best - last)));
				diff = diff * rndm.nextInt(10);
				try { sleep((long) diff); }
				catch (Exception e) { System.out.println(e); }
			
				last = best; // Update last angle
				
				// Take snapshot
				System.out.println("CLICK! snapshot taken at angle #" + best);
				
				//Signal all client threads with requests for this angle
				for(int j = 0; j < req.length; j++){	
					if(req[j] == best){ req[j] = 0; B[j].release(); }
				}
			}
		}

		else{
			int iter;
			// Each client makes 50 requests
			for(iter = 1; iter <= 50; iter++){

				// Sleep for a random amount of time
				try { sleep(rndm.nextInt(50)); }
				catch (Exception e) {}

				// Request a snapshot at some angle between 1 and 8 (inclusive)
				req[id] = rndm.nextInt(8) + 1;
				System.out.println("P[" + id + "] requested angle " + req[id]);
				
				cam.release(); // Enable cam to schedule next angle
				try { B[id].acquire(); }
				catch (InterruptedException e) {}
			}
		}
	}

	public static void main(String[] args){

		final int N = 10;	
		camera c[] = new camera[N];

		// Create the camera thread
		camera cam = new camera(-1, N);
		cam.start();
		
		for (int i = 0; i < N; i++){
			c[i] = new camera(i, N);
			c[i].start();
		}
	}
}
