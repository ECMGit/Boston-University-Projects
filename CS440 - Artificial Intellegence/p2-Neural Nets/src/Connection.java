/**
 * @author Zhiqiang Ren 
 * date: Feb. 4th. 2012
 * 
 */

package aipackage;


public class Connection {
    
    public Connection(Node from, Node to, double weight) {
        m_from = from;
        m_to = to;
        m_weight = weight;
        m_deltaw = 0;
    }
    
    public Node getFromNode() {
        return m_from;
    }
    
    public Node getToNode() {
        return m_to;
    }
    
    public double getWeight() {
        return m_weight;
    }
    
    public void sumDelta(double x){
    	m_deltaw += x;
    }
    
    public void updateWeight(){
    	
    	m_weight += m_deltaw;			// Update the weights with our cumulative change
    	m_deltaw = 0;					// Reset m_deltaw after we update the weights (which is only done at the end of a training round)
    }
    
    private double m_weight;
    private double m_deltaw;

    private Node m_from;
    private Node m_to;

}
