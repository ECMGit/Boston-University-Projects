
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

public class EchoServer {

	static Socket clientSocket;
	static ServerSocket serverSocket;
	static BufferedReader in;
	static PrintWriter out;
	static String type;
	static int numprobes;
	static String prot;
	static int size;
	static int delay;
	
    public static void main(String[] args) throws Exception {

    	setupIO(args[0]); // create socket, and setup IO streams
    	
        CSP();	// Set up the server and store data needed for the measurement phase
        MP();	// Echo back messages from client
        CTP();	// listen for a termination request
        
        // close IO streams, then socket
        System.err.println("Closing connection with client");
        out.close();
        in.close();
        serverSocket.close();
    } 
    public static void setupIO(String x) throws IOException{
    	int port = Integer.parseInt(x);
        	
        serverSocket = new ServerSocket(port);
        System.err.println("Started server on port " + port);

        // a "blocking" call which waits until a connection is requested
        Socket clientSocket = serverSocket.accept();
        System.err.println("Accepted connection from client: " + clientSocket.getInetAddress());
        
        // in reads input given to server (whatever is sent from the client)
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
        
        // out sends message taken in, and sends it back to the client
        out = new PrintWriter(clientSocket.getOutputStream(), true); 
    }
    public static void CSP() throws IOException{
    	// Connection setup phase
        boolean valid = false;
        while(!valid){
        	try{
        		String input = in.readLine();
        		String delims = "[ ]+";
	            String[] tokens = input.split(delims);
        		prot = tokens[0];
        		type = tokens[1];
        		numprobes = Integer.parseInt(tokens[2]);
        		size = Integer.parseInt(tokens[3]);
        		delay = Integer.parseInt(tokens[4]);
        		
        		if(prot.equalsIgnoreCase("s") && (type.equalsIgnoreCase("rtt") || type.equalsIgnoreCase("tput"))){
        			out.println("200 OK: Ready");
        			valid = true;
        		}
        		else{
        			out.println("404 ERROR: Invalid Connection Setup Message"); 
        		}
        	}
        	catch (Exception e) { 
        		out.println("404 ERROR: Invalid Connection Setup Message"); 
        	}
        }
    }
    public static void MP() throws InterruptedException, IOException{
    	Random rndm = new Random();
        String s = "";
        
        for(int j = 0; j < 3; j++){
        	for(int i = 1; i <= numprobes; i++){
        		
        		s = in.readLine().toString();
        		String delims = "[ ]+";
	            String[] tokens = s.split(delims);
	            
	            Thread.sleep(rndm.nextInt(100) * delay * 2);
	            if(!tokens[1].equalsIgnoreCase("" + i)){
	            	out.println("404 ERROR: Invalid Measurement Message");
	            	return;
	            }
	            else{
	            	out.println(s);
	            }
        	}
        }
    }
    public static void CTP() throws IOException{
    	
    	String req = in.readLine();
    	if(req.equalsIgnoreCase("t ")){ out.println("200 OK: Closing Connection"); }
    	else{ out.println("404 ERROR: Invalid Connection Termination Message"); }
    	
    	serverSocket.close();
    }
}
