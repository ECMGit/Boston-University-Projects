public class testProject
{
    public final static void main(String[] argv)
    {
        StudentNetworkSimulator simulator;
        
        int nsim = -1;
        double loss = -1;
        double corrupt = -1;
        double delay = -1;
        int trace = -1;
        int seed = -1;
        int windowsize = -1;
        double timeout = -1;
                             
        System.out.println("-- * Network Simulator v1.0 * --");
        
        nsim = Integer.parseInt(argv[0]);
        System.out.print("Number of messages to simulate (> 0): " + nsim);
            
        delay = Integer.parseInt(argv[1]);
        System.out.print("Average time between messages from sender's layer 5 (> 0.0): " + delay);
          
        windowsize = Integer.parseInt(argv[2]);
        System.out.print("Enter window size (> 0): " + windowsize);
        
        timeout = Double.parseDouble(argv[3]);
        System.out.print("Retransmission timeout (>0.0): " + timeout);
            
        trace = Integer.parseInt(argv[4]);
        System.out.print("Trace level (>= 0): " + trace );
            
        loss = Double.parseDouble(argv[5]);
        System.out.print("Packet loss probability (0.0 for no loss): " + loss);
          
        corrupt = Double.parseDouble(argv[6]);
        System.out.print("Packet corruption probability (0.0 for no corruption): " + corrupt);
         
        seed = Integer.parseInt(argv[7]);
        System.out.print("Enter random seed: " + seed);
        
        simulator = new StudentNetworkSimulator(nsim, loss, corrupt, delay,
                                                trace, seed, windowsize, timeout);
                                                
        simulator.runSimulator();
    }
}
