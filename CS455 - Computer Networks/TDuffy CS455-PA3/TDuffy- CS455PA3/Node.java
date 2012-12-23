/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 * 
 * Compilation instructions: After compiling all files, run Project3.java
 */ 
public class Node{ 
    
    public final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between node 0 and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/
    
    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) {
    	
    	this.nodename = nodename;
    	lkcost = initial_lkcost;
    	
    	int size = initial_lkcost.length;
    	costs = new int[size][size];
    	
    	// When we initialize distance tables, we only have values for entries on the diagonal
    	for(int i = 0; i < size; i++){
    		for(int j = 0; j < size; j++){
    			if(i==j){ costs[i][j] = initial_lkcost[i]; }
    			else{ costs[i][j] = INFINITY; }					// Otherwise we show that there is no known connection
    		}
    	}
    	System.out.printf("\nt=%.3f: Initialized Node #%d\n", NetworkSimulator.clocktime, nodename);
    	sendmincosts();
    }    

	// This method updates the costs array using the information stored in rcvdpkt
	void rtupdate(Packet rcvdpkt) {  
		
		int source = rcvdpkt.sourceid;
		int[] dist = rcvdpkt.mincost;
		int directCost = costs[source][source];
		boolean send = false;
		
		// When we receive a packet from node X, we only update column #X		
		for(int i = 0; i < 4; i++){
			if(i != nodename){									// There is no need to update a path from a node to itself
				if((dist[i] + directCost) < costs[i][source]){	// And we will only update the table if this new information
					costs[i][source] = dist[i] + directCost;	// suggests a shorter path than one already known
					send = true;
				}
			}
		}
		
		// Print info to screen
		System.out.printf("t=%.3f: Node #%d has received a routing packet from node #%d\n", NetworkSimulator.clocktime, nodename, source);
		if(send){ System.out.println("Distance table changed!"); }
		else { System.out.println("No change to distance table."); }
		
		printdt();
		
		// Now we send this information out to any connected nodes
		if(send){ sendmincosts(); }
	}
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {
    	
    	System.out.println("\nLink between " + nodename + " and " + linkid + " has changed from " + costs[linkid][linkid] + " to " + newcost);
    	// When we receive info about a link change, we must update our distance table
    	int diff = newcost - costs[linkid][linkid];
    	
    	// Now we add this difference to all entries in column #linkid
    	for(int i = 0; i < 4; i++){
    		if(costs[i][linkid] != INFINITY){
    			costs[i][linkid] += diff;
    		}
    	}
    	
    	// Finally, we must send this updated information out to any connected nodes
    	sendmincosts();   	
    }    


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d   %3d\n",costs[1][1], costs[1][2],costs[1][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][1], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][1], costs[3][2],costs[3][3]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d \n",costs[0][0], costs[0][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][0], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][0], costs[3][2]);
	    break;
	    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    0     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][0],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][0],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][0],costs[2][2]);
	    break;
        }
    }

    /* Constructs an array holding the minimum costs to other nodes, then sends it out to neighboring routers */
    private void sendmincosts() {
		
    	// First, we construct an array of minimum costs
    	int[] min = {costs[0][0], costs[1][0], costs[2][0], costs[3][0]};
    	int[] dir = {0,0,0,0};	//Holds the direction the shortest path goes through
    	
    	//Fill it with the minimum distance to each node
    	for (int to = 0; to < 4; to++){
    		for(int via = 0; via < 4; via++){
    			if(costs[to][via] < min[to]){
    				min[to] = costs[to][via];
    				dir[to] = via;    				
    			}
    		}
    	}
    	
    	// make a copy of min[], in case we have to poison a link
    	int[] copy = {min[0],min[1],min[2],min[3]};
    	
    	// poisoned[i] is true if that value has been poisoned in order to prevent loops (used for output only) 
    	boolean[] poisoned = {false,false,false,false};		
    	
    	/* Now send this array to any nodes directly connected to this one.
    	 * In order to prevent loops, if the shortest path goes through the node we are sending to,
    	 * we will poison the link.
    	 */
    	for (int rec = 0; rec < 4; rec++){
    		
    		for(int x = 0; x<4; x++){ copy[x] = min[x]; poisoned[x] = false; }	// restore original values each time we send to a new node
			
    		// If the receiving node is not this node, and a link to that node exists (i.e. is not INFINITY), send to that node
    		if((rec != nodename) && (costs[rec][rec] < INFINITY)){	
    			
    			System.out.printf("\nt=%.3f: Node #%d is sending packet to node #%d\n", NetworkSimulator.clocktime, nodename, rec);
    			
    			for (int i = 0; i < 4; i++){
    				
    				// If the direction of the shortest path goes through the node we are sending to, poison that link
    				if(dir[i] == rec){
	    				copy[i] = INFINITY;	
	    				poisoned[i] = true;
	    				System.out.println("Shortest path to node " + i + " goes through node #" + rec  + ", so we will poison this link to prevent loops");
    				}
    			}
    				
    			printarr(copy,poisoned);
    			NetworkSimulator.tolayer2(new Packet(nodename, rec, copy));
    		}
    	}
	}

	private void printarr(int[] arr, boolean[] poisoned) {
		System.out.print("Minimum distance to nodes: ");
		for(int i = 0; i < 4; i++){
			
			if(poisoned[i] == true){ System.out.print(arr[i] + "(poisoned)"); }
			else{ System.out.print(arr[i]); }
			
			if(i < 3){ System.out.print(", "); }
		}

		System.out.println();
	}
}
