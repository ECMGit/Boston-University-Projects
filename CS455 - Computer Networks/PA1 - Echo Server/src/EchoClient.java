import java.io.*;
import java.net.*;
import java.util.*;

public class EchoClient {

	// console is used to get input from command prompt
	static String host;
	static int port;
	static Socket s;
    static Scanner console = new Scanner(System.in);
    static BufferedReader in;
    static PrintWriter out;
    static String setup;		// Setup message sent by user
    static int numprobes;
    static String type;
    static int size;
    
	public static void main(String[] args) throws Exception {
		host = args[0];
        port = Integer.parseInt(args[1]);
      
        // Create socket and bind to host on specified port
        s = new Socket(host, port);
        setupIO(s);
        CSP();	// Run the Connection Setup Phase, and log data needed for testing
        MP();	// Measurement phase - sends packets of data to the server and measures RTT or TPUT.
        CTP();	// Close the connection gracefully
    }
	
	public static void setupIO(Socket s) throws IOException{
		
		// out sends message taken from console, and send it to the server
		out = new PrintWriter(s.getOutputStream(), true); 
       
        // reads input from server (whatever is sent to client)
        
        try {
        	in = new BufferedReader(new InputStreamReader(s.getInputStream()));     
            System.err.println("Connected to " + host + " on port " + port);
        }
        catch (IOException ioe) {
            System.err.println("Could not open " + s);
        }
	}
	public static void CSP() throws IOException{
    	// Connection Setup Phase
        String resp = "";
        do{
        	System.out.println("Please enter a setup message in the format:");
        	System.out.println("<PROTOCOL PHASE> <MEASUREMENT TYPE> <NUMBER OF PROBES> <MESSAGE SIZE> <SERVER DELAY>");
        	setup = console.nextLine();
        	out.println(setup);
        	resp = in.readLine();
        	System.err.println(resp);
        
        }while(!resp.equals("200 OK: Ready"));
        
        // Setup was ok, so we can start parsing the message, and storing variables
        String delims = "[ ]+";
        String[] tokens = setup.split(delims);
        type = tokens[1];
        numprobes = Integer.parseInt(tokens[2]);
        size = Integer.parseInt(tokens[3]);
    }
	public static void MP() throws IOException{
	
		long[][] times = new long[3][numprobes];
		
		for(int i = 0; i < 3; i++){	
			System.out.println("\nRunning Test #" + (i+1) + ". Transmitting " + numprobes
					+ " " + size + "-byte probe messages to " + host);
			
			for(int j=0; j < numprobes;j++){
    		
				// Create a non-empty byte array of user-specified size
		        String s = newPayload();
				String sent = "m " + (j+1) + " " + s;
				
				out.println(sent);						// Send message to server
	    		Long timesent = new Date().getTime();	// Remember the time sent
	    		String rec = in.readLine();				// Get message from server
	            Long timerec = new Date().getTime();	// Remember the time sent
	            if(rec.equalsIgnoreCase("404 ERROR: Invalid Measurement Message")){ break; }
	            
	            times[i][j] = timerec - timesent;		// Store RTT time for each message sent
	            if(times[i][j] == 0){ times[i][j] = 1; }// prevent divide by zero errors
	            System.out.println(size + " bytes from " + host + ":" + "  RTT = " + times[i][j] + " ms");
			}
		}       	
	      
		if (type.equalsIgnoreCase("RTT")){ findRTT(times); }
		if (type.equalsIgnoreCase("TPUT")){ findTPUT(times); }
	}
	// This method calculates minimum, average, and max RTT for each of the 3 test runs.
	public static void findRTT(long[][] times){
		
		long[] min = new long[3];	// min RTT
		long[] max = new long[3];	// max RTT
		long[] total = {0,0,0};		// total RTT
		
		System.out.println();
		for(int i = 0; i < 3; i++){	
			
			// Set min and max to first logged time
			min[i] = times[i][0];
			max[i] = times[i][0];
			
			for(int j=0; j < numprobes;j++){
				
				if (times[i][j] < min[i]){ min[i] = times[i][j]; }	// Update min RTT
				if (times[i][j] > max[i]){ max[i] = times[i][j]; }	// Update max RTT
				total[i] += times [i][j];							// Update total to calculate average
			}
			
			total[i] = total[i]/numprobes;		// total now holds the average RTT
			System.out.println("round-trip for test #" + (i+1) + " min/avg/max: " + min[i] + "/" + total[i] + "/" + max[i]);
		}

		long avg = 0;
		long run_min = min[0];
		long run_max = max[0];
		for(int i = 0; i < 3; i++){
		    avg +=  total[i];
		    if(min[i] < run_min){ run_min = min[i]; }
		    if(max[i] > run_max){ run_max = max[i]; }	
		}



		System.out.println("min/avg/max of all 3 tests: " + run_min + "/" + (avg/3) + "/" + run_max);

	}
	// Find minimum, average, and maximum throughput for each of the 3 test runs.
	public static void findTPUT(long[][] times){
		
		long[] min = new long[3];		// min tput
		long[] max = new long[3];		// max tput
		long[] total = {0,0,0};			// total RTT
		
		System.out.println();
		for(int i = 0; i < 3; i++){	
			
			// Set min and max to first logged time
			min[i] = ((size * 1000) / times[i][0]);
			max[i] = min[i];
			
			for(int j=0; j < numprobes;j++){
				
				long x = ((size* 8000) / times[i][j]);		//tput for message of size 'size' and RTT times[i][j]
				
				if (x < min[i]){ min[i] = x; }
				if (x > max[i]){ max[i] = x; }
				total[i] += x;									//sum of tputs
			}
			
			total[i] = total[i]/numprobes;		// total now holds the average tput
			System.out.println("Throughput (in bps) for test #" + (i+1) + " min/avg/max: "
					+ min[i] + "/" + total[i] + "/" + max[i]);
		}

		long avg = 0;
		long run_min = min[0];
		long run_max = max[0];
		for(int i = 0; i < 3; i++){
		    avg +=  total[i];
		    if(min[i] < run_min){ run_min = min[i]; }
		    if(max[i] > run_max){ run_max = max[i]; }	
		}



		System.out.println("min/avg/max of all 3 tests: " + run_min + "/" + (avg/3) + "/" + run_max);	
	}
	public static void CTP() throws IOException{
		
		out.println("t ");		// Send termination request to server
		System.err.println(in.readLine());
		s.close();
	}
	
	// create a random size-byte payload for each probe sent 
	public static String newPayload(){
		
		Random rndm = new Random();
		byte[] message = new byte[size];
        rndm.nextBytes(message);
        
        for(int i = 0; i < size; i++){
        	message[i] = (byte) (rndm.nextInt(93) + 33);
        }
        
        String x = new String(message);
        return x;
	}
}
