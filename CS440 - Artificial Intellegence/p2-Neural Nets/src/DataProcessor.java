package aipackage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Locale;

public class DataProcessor {
	static String[] A1_cand = { "b", "a", "?" };
	static String[] A4_cand = { "u", "y", "l", "t", "?" };
	static String[] A5_cand = { "g", "p", "gg", "?" };
	static String[] A6_cand = { "c", "d", "cc", "i", "j", "k", "m", "r", "q",
		"w", "x", "e", "aa", "ff", "?" };
	static String[] A7_cand = { "v", "h", "bb", "j", "n", "z", "dd", "ff", "o", "?" };
	static String[] A9_cand = { "t", "f", "?" };
	static String[] A10_cand = { "t", "f", "?" };
	static String[] A12_cand = { "t", "f", "?" };
	static String[] A13_cand = { "g", "p", "s", "?" };

	public double[][] m_inputvs;
	public double[][] m_outputvs;

	private List<CreditData> m_datas;

	class CreditData {
		public CreditData(double[] inputs, double[] outputs) {
			m_inputs = inputs;
			m_outputs = outputs;
		}

		public double[] m_inputs;
		public double[] m_outputs;
	}

	double cvtDouble(String [] candidates, String name) {
		for (int i = 0; i < candidates.length; ++i) {
			if (candidates[i].equals(name)) {
				return i;
			}
		}
		return candidates.length;
	}

	public DataProcessor(String aFileName) throws FileNotFoundException {
		m_datas = new ArrayList<CreditData>();
		Scanner s = null;

		FileReader f = new FileReader(aFileName);

		try {
			s = new Scanner(new BufferedReader(f));
			s.useLocale(Locale.US);

			while (s.hasNextLine()) {
				CreditData data = processLine(s.nextLine());
				m_datas.add(data);
			}
		} finally {
			s.close();
		}

		int i = 0;
		m_inputvs = new double[m_datas.size()][];
		m_outputvs = new double[m_datas.size()][];
		for (CreditData data: m_datas) {
			m_inputvs[i] = data.m_inputs;
			m_outputvs[i] = data.m_outputs;
			++i;
		}

		preprocess();
		normalize();			// Normalize all values

	}

	private double nextDouble(Scanner s) {
		if (s.hasNextDouble()) {
			return s.nextDouble();
		} else {
			s.next();
			return 0.0;
		}
	}

	public CreditData processLine(String line) {
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(",");

		double[] inputs = new double[15];
		double[] outputs = new double[1];

		inputs[0] = cvtDouble(A1_cand, scanner.next());
		inputs[1] = nextDouble(scanner);
		inputs[2] = nextDouble(scanner);
		inputs[3] = cvtDouble(A4_cand, scanner.next());
		inputs[4] = cvtDouble(A5_cand, scanner.next());
		inputs[5] = cvtDouble(A6_cand, scanner.next());
		inputs[6] = cvtDouble(A7_cand, scanner.next());
		inputs[7] = nextDouble(scanner);
		inputs[8] = cvtDouble(A9_cand, scanner.next());
		inputs[9] = cvtDouble(A10_cand, scanner.next());
		inputs[10] = nextDouble(scanner);
		inputs[11] = cvtDouble(A12_cand, scanner.next());
		inputs[12] = cvtDouble(A13_cand, scanner.next());
		inputs[13] = nextDouble(scanner);
		inputs[14] = nextDouble(scanner);

		String output = scanner.next();
		if (output.equals("+")) {
			outputs[0] = 1.0;
		} else {
			outputs[0] = 0.0;
		}
		return new CreditData(inputs, outputs);
	}

	public void normalize(){

		// For each feature in in m_inputvs, find the average
		for(int feature = 0; feature < m_inputvs[0].length; feature++){

			double avg = 0;
			for(int row = 0; row  < m_inputvs.length; row++){
				avg += m_inputvs[row][feature];
			}
			avg = (avg / m_inputvs.length);				// Divide by number of attributes

			// Find standard deviation of this feature. 	stdDev = sqrt(	(Sum of all (xi - avg)^2) / N)    		
			double stdSum = 0;
			for(int row = 0; row  < m_inputvs.length; row++){
				double x = (m_inputvs[row][feature] - avg);
				stdSum += (x*x);
			}

			double stdDev = (stdSum / m_inputvs.length);
			stdDev = Math.sqrt(stdDev);

			// z-scale attribute #<feature> for each input
			for(int row = 0; row  < m_inputvs.length; row++){

				double z = (m_inputvs[row][feature] - avg) / stdDev; 
				m_inputvs[row][feature] = z;
			}
		}

		// Normalize each feature in m_outputs
		for(int feature = 0; feature < m_outputvs[0].length; feature++){

			double avg = 0;
			for(int row = 0; row  < m_outputvs.length; row++){
				avg += m_outputvs[row][feature];
			}
			avg = (avg / m_outputvs.length);				// Divide by number of attributes

			// Find standard deviation of this feature
			double stdSum = 0;						// stdDev = sqrt(	(Sum of all (xi - avg)^2) / N)

			for(int row = 0; row  < m_outputvs.length; row++){
				double x = (m_outputvs[row][feature] - avg);
				stdSum += (x*x);
			}

			double stdDev = (stdSum / m_outputvs.length);			// Divide by # of samples
			stdDev = Math.sqrt(stdDev);								// Take square root

			// z-scale attribute #<feature> for each input
			for(int row = 0; row  < m_outputvs.length; row++){

				double z = (m_outputvs[row][feature] - avg) / stdDev; 
				m_outputvs[row][feature] = z;
			}
		}
	}

	public void preprocess(){

		double[] cndtnMean = new double[15];
		int count = 0;

		for(CreditData data : m_datas){
			if(data.m_outputs[0] == 1.0){		// For every positive match
				for(int i = 0; i < 15; i++){
					cndtnMean[i] += data.m_inputs[i];
				}
				count++;
			}
		}
		
		// Now set the average
		for(int i = 0; i < 15; i++){
			cndtnMean[i] = cndtnMean[i]/count;
		}
		
		
		// Finally, scan through all the inputs, and replace anything that was a "?" with the appropriate mean
		for(CreditData data : m_datas){
			
			if(data.m_inputs[0] == 2){ data.m_inputs[0] = cndtnMean[0]; }
			if(data.m_inputs[3] == 4){ data.m_inputs[3] = cndtnMean[3]; }
			if(data.m_inputs[4] == 4){ data.m_inputs[4] = cndtnMean[4]; }
			if(data.m_inputs[5] == 14){ data.m_inputs[5] = cndtnMean[5]; }
			if(data.m_inputs[6] == 9){ data.m_inputs[6] = cndtnMean[6]; }
			if(data.m_inputs[8] == 2){ data.m_inputs[8] = cndtnMean[8]; }
			if(data.m_inputs[9] == 2){ data.m_inputs[9] = cndtnMean[9]; }
			if(data.m_inputs[11] == 2){ data.m_inputs[11] = cndtnMean[11]; }
			if(data.m_inputs[12] == 3){ data.m_inputs[12] = cndtnMean[12]; }
		}
	}
}



