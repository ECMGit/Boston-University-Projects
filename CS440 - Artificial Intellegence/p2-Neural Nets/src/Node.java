/**
 * @author Zhiqiang Ren 
 * date: Feb. 4th. 2012
 * 
 */

package aipackage;

import java.util.ArrayList;
import java.util.List;

public class Node {
	public Node(int layer, int pos, boolean isThreshold)
	{
		m_input = 0;
		m_output = 0;
		m_input_conn = new ArrayList<Connection>();
		m_output_conn = new ArrayList<Connection>();

		m_beta = 0;

		m_pos = pos;
		m_layer = layer;
	}

	public void addInputConnection(Connection con) {
		m_input_conn.add(con);
	}

	public void addOutputConnection(Connection con) {
		m_output_conn.add(con);
	}

	public void setOutput(double output) {
		m_output = output;
	}

	public double getOutput() {
		return m_output;
	}

	public double f(double sigma)
	{
		return 1 / (1 + Math.exp(-1 * sigma));        
	}

	public double getBeta() {
		return m_beta;
	}

	public Connection getOutputConnection(int j) {
		return m_output_conn.get(j);
	}

	public List<Connection> getInputConnectionList() {
		return m_input_conn;        
	}

	public List<Connection> getOutputConnectionList() {
		return m_output_conn;        
	}

	public int getPos() {
		return m_pos;
	}

	public int getLayer() {
		return m_layer;
	}

	public void calcOutput() {
		m_input = 0;
		m_output = 0;

		for (Connection con: m_input_conn) {
			m_input += (con.getWeight() * con.getFromNode().getOutput());
		}
		m_output = f(m_input);
	}

	// This calculates the error for nodes in the output layer, and updates deltaw for each input connection
	public void calcBetaOut(double target, double rate){
		m_beta = target - m_output;

		for (Connection c: m_input_conn) {												// For each input connection

			double FromNodeOutput = c.getFromNode().getOutput();						// Output of 'From' Node
			double delta = rate * FromNodeOutput * m_output * (1 - m_output) * m_beta;	// Calculate new change in weight

			c.sumDelta(delta);								// Add this weight change to this connections current deltaw value
		}
	}

	// This calculates the error for nodes in the hidden layers
	public void calcBetaHidden(double rate){

		/* 1. Calculate the error for this hidden node */

		m_beta = 0;
		for (Connection c: m_output_conn) {

			double weight = c.getWeight();						// Weight of the output connection
			double toNodeOut = c.getToNode().getOutput();		// Output of 'To' Node
			double toNodeBeta = c.getToNode().getBeta();		// Beta of 'To' Node

			m_beta += weight * toNodeOut * (1 - toNodeOut) * toNodeBeta;
		}

		/* 2. Now update the deltaw for each input connection */

		for (Connection c: m_input_conn) {												// For each input connection

			double FromNodeOutput = c.getFromNode().getOutput();						// Output of 'From' Node
			double delta = rate * FromNodeOutput * m_output * (1 - m_output) * m_beta;	// Calculate new change in weight

			c.sumDelta(delta);								// Add this weight change to this connections current deltaw value
		}
	}

	private List<Connection> m_input_conn;
	private List<Connection> m_output_conn;

	private double m_input;
	private double m_output;

	private double m_beta;

	private int m_pos;  // starting from 0
	private int m_layer;  // starting from 0
}


