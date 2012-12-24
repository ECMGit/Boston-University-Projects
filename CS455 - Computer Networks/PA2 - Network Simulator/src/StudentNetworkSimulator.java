import java.util.*;

public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B 
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          create a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)
    
    // Variables to remember the state of A:
    private int SeqNum;						// Used to keep track of the current sequence number
    private LinkedList<Packet> sentPackets;	// List of sent packets
    private int seqBase;					// Beginning of the window
    private int seqMax;						// Last packet in window
    private int NexttoSend;					// Last packet sent in window
    
    // Variables for analysis
    private int totalsent;					// Number of packets sent
    private int numCorrupted;				// Number of corrupted packets received
    private int numACKs;					// Number of ACKs sent
    private LinkedList<Double> RTT;			// List of RTTs for sent packets
    private int PacketsDelivered;			// Number of packets successfully delivered to B
    private int numtoL5;					// Number of packets delivered to layer 5
    private int  seednum;
    
    // Variables to remember the state of B:
    private int expected;			// sequence number that B is expecting next (B will use this when sending ACKs) 
    
    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
	WindowSize = winsize;
	LimitSeqNo = winsize+1;
	RxmtInterval = delay;
	seednum = seed;
    }

    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	// Create new packet
    	String payload = message.getData();
    	Packet p = new Packet(SeqNum, 0, -1, payload);
    	int check = findCheckSum(p);
    	p.setChecksum(check);
		SeqNum = nextNum(p.getSeqnum());		// Update sequence number
		
		// add to list of packets (queue)
		sentPackets.add(p);
		
    	// If next message is within sending window
    	if(sentPackets.indexOf(p) <= seqMax){

    		// Send packet to B
    		toLayer3(A, p);
    		System.out.println("Sending packet to B:" + p.toString());
    		
    		// When a packet is sent for the first time, store time sent in RTT list
    		RTT.add(sentPackets.indexOf(p), getTime());
    		totalsent++;
    		if(seqBase == NexttoSend){ startTimer(A, RxmtInterval); }
    		NexttoSend++;							// Update counter for next packet to send
    	}
        	// Otherwise, we must wait for an ACK to arrive and the window to shift (it is currently full)
	}
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {

    	double time = getTime();
    	
    	// If ACK is corrupted, wait for time out
    	if (isCorrupted(packet)){
    		System.out.println("A recieves corrupted ACK! Ignoring data." + packet.toString());
    		numCorrupted++;
    		return;
    	}
    	
    	int acknum = packet.getAcknum();
    	System.out.println("A received ACK for packet #" + packet.getSeqnum());
    	
    	// slide window over so that seqBase == acknum
    	while(seqBase < sentPackets.size()){
    		// This loop shifts the sending window appropriately when an ACK is lost
   	 		// and a successive ACK comes in. Therefore we can treat this ACK as an
   	 		// ACK for all previously unACKed packets where seqNum <= acknum
   	 		
    		// if the seqNum at seqBase == acknum, we only need to shift the window
    		// once which will occur outside of this loop
    		if(sentPackets.get(seqBase).getSeqnum() == acknum){
    			break;
    		}
    		
   	 		// RTT[seqBase] has the time that the packet at sentPackets[seqBase] was sent
   	 		// Here, we update this value to hold RTT
   	 		Double x = RTT.get(seqBase);
   	 		x = time - x;
   	 		RTT.add(seqBase, x);
   	 		
   	 		// Shift after updating RTT
   	 		seqBase++;
	 		seqMax++;
    	}
    	
    	// Update RTT
    	Double x = RTT.get(seqBase);
    	x = time - x;
    	RTT.add(seqBase, x);
	 		
    	// Now slide it over one more position (so that seqBase is in front of the last ACKed packet
    	seqBase++;
    	seqMax++;
		
		// If there are no outstanding packets, stop timer 
    	if(seqBase == NexttoSend){
    		stopTimer(A);
    		checkQueue();
    	}
    	// Otherwise, restart it
    	else{
    		stopTimer(A);
    		startTimer(A, RxmtInterval);
    	}

	}
    
	// This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	System.out.println("\nTimer interrupt! Resending window");
    	startTimer(A, RxmtInterval);
    	
    	// Retransmit window
    	for(int i = seqBase; ((i <= seqMax) &&  (i < sentPackets.size())); i++){
    		
    		Packet p = sentPackets.get(i);
    		toLayer3(A, p);	// retransmit window of unsent packets
    		
    		totalsent++;
    		
    		// If we are sending a packet for the first time
    		if(i == NexttoSend){
    			// When a packet is sent for the first time, store time sent in RTT list
    			RTT.add(i, getTime());
    			System.out.println("Sending packet to B:" + p.toString());
    			NexttoSend++;							// Update counter for next packet to send
    		}
    		else{
    			System.out.println("Resending packet to B:" + p.toString());
    		}
    	}
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
    	SeqNum = FirstSeqNo;
    	sentPackets = new LinkedList<Packet>();
    	seqBase = FirstSeqNo;
    	seqMax = seqBase + WindowSize - 1;
    	NexttoSend = FirstSeqNo;
    	totalsent = 0;
    	numCorrupted = 0;
    	numACKs = 0;
        RTT = new LinkedList<Double>();	
    }
    
    // This routine will be called whenever a packet sent from the A-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	PacketsDelivered++;
    	
    	if(isCorrupted(packet)){
    		System.out.println("B received corrupted packet - ignoring data");
    		numCorrupted++;
    		return;		// if the packet is corrupted, do nothing and wait for A to time out
    	}

    	// If not corrupted, only accept next in order packet
    	if(packet.getSeqnum() == expected){
    		
    		// If so, send ACK to A and send data up to layer 5
    		Packet ACK = new Packet(packet.getSeqnum(), expected, -1, "");
    		int check = findCheckSum(ACK);
    		ACK.setChecksum(check);
    		System.out.println("Sending ACK for packet #"+ ACK.getAcknum());
    		toLayer3(B, ACK);				// Send ACK to A
    		numACKs++;
    		
    		expected = nextNum(expected);	// update next expected sequence number
    		
    		toLayer5(packet.getPayload());
    		numtoL5++;
    	}
    	else{
    		
    		// Otherwise, ignore it and wait for A to retransmit
    		System.out.println("B received a packet out of order - ignoring data. Expecting #" + expected + " received #" + packet.getSeqnum());
    	}
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	expected = FirstSeqNo;
    	PacketsDelivered = 0;
    }

    // Use to print final statistics
    protected void Simulation_done(int numlost, int numcorrupted)
    {
    	System.out.println("\n=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
    	System.out.println("Seed #" + seednum );
    	System.out.println("Window Size: " + WindowSize + "\n");
    	System.out.println("Total packets sent = " + totalsent);
    	System.out.println("Number of original data packets sent: " + NexttoSend);
    	System.out.println("Number of ACK packets sent: " + numACKs);
    	System.out.println("Total number of packets delivered to Layer 5: " + numtoL5);
    	System.out.println("Number of retransmissions: " + (totalsent - NexttoSend));
    	double pLoss = (double) (numlost)/totalsent;						// Probability of packet loss
        double pCorruption = (double) numcorrupted/(totalsent-numlost);				// Probability of packet corruption
        
        System.out.println("Number of packets lost: " + numlost);
    	System.out.println("Percent of packets lost: " + pLoss);
    	System.out.println("Number of packets corrupted: " + numcorrupted);
    	System.out.println("Percent of packets corruption: " + (pCorruption));
    	System.out.print("Average RTT for packets: ");
    	
    	double avgRTT = 0;
    	// Compute average RTT for all ACKed packets
    	int i;
    	for(i = 0; i < seqBase; i++){
    		avgRTT += RTT.get(i);
    	}
    	
    	avgRTT = avgRTT / i;
    	
    	System.out.println("" + avgRTT + " ms");
    }	
    
    protected int nextNum(int x)
    {
    	int next = x + 1;		// increment sequence number
    	
    	// If this number reaches the limit, start over from the beginning
    	if (next == LimitSeqNo){ next = FirstSeqNo; }
    	
    	return next;
    }	

    // Compute the checksum of the header and payload: sum of seqNum + each char of the payload
    protected int findCheckSum(Packet p)
    {
    	int s = p.getSeqnum();
    	int a = p.getAcknum();
    	String m = p.getPayload();
    
    	int check = s + a;
    	for(int i = 0; i < m.length(); i++){
    			check += (int) m.charAt(i);
    	}
    	
    	return check;
    }
    
    // This method checks for corruption by checking if the checksum
    // matches what it is supposed to. Returns true if corrupted, false otherwise
    protected boolean isCorrupted(Packet packet){
    	
    	int a = findCheckSum(packet);
    	int b = packet.getChecksum();
    	return (a != b);
    }
    
    // This method is called when there are no outstanding packets (unACKed packets)
    // within the window. If the window has room, it will send the next packet waiting to be sent
    private void checkQueue() {
		
    	// Ensure that the window has room, then check if there is a packet waiting to be sent
    	if((NexttoSend <= seqMax)){
    		
    		if (NexttoSend < sentPackets.size()){
    		
	    		Double t = getTime();
	    		toLayer3(A, sentPackets.get(NexttoSend));
	    		if(seqBase == NexttoSend){ startTimer(A, RxmtInterval); }
	    		
	    		NexttoSend++;
	    		totalsent++;
	    		
	    		// Add time sent to RTT
	    		RTT.add(NexttoSend, t);
    		}
    	}
    }
}
