/**
 * @author Zhiqiang Ren 
 * date: Feb. 4th. 2012
 * 
 */
package aipackage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import aipackage.DataProcessor.CreditData;

/**
 * @author Zhiqiang Ren
 * 
 */
public class NeuralNetLearner {
	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		System.out.println("========== ~Net 1~ ==========");

		int[] layers = { 6, 2, 1 }; // three layers
		NeuralNet net = new NeuralNet(layers);
		net.connectTest();

		double[][] inputvs = { { 1, 1, 0, 0, 0, 0 }, { 1, 0, 1, 0, 0, 0 },
				{ 1, 0, 0, 1, 0, 0 }, { 1, 0, 0, 0, 1, 0 },
				{ 1, 0, 0, 0, 0, 1 }, { 0, 1, 1, 0, 0, 0 },
				{ 0, 1, 0, 1, 0, 0 }, { 0, 1, 0, 0, 1, 0 },
				{ 0, 1, 0, 0, 0, 1 }, { 0, 0, 1, 1, 0, 0 },
				{ 0, 0, 1, 0, 1, 0 }, { 0, 0, 1, 0, 0, 1 },
				{ 0, 0, 0, 1, 1, 0 }, { 0, 0, 0, 1, 0, 1 },
				{ 0, 0, 0, 0, 1, 1 } };

		double[][] outputvs = { { 0 }, { 0 }, { 1 }, { 1 }, { 1 }, { 0 },
				{ 1 }, { 1 }, { 1 }, { 1 }, { 1 }, { 1 }, { 0 }, { 0 }, { 0 } };

		for (int n = 0; n < 300; ++n) {
			net.train(inputvs, outputvs, 14);
		}

		net.errorrate(inputvs, outputvs);
		System.out.println("\n========== ~Net 2~ ==========");

		int[] layers2 = { 2, 2 }; // two layers
		NeuralNet net2 = new NeuralNet(layers2);
		net2.connectAll();

		double[][] inputvs2 = { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 0 } };
		double[][] outputvs2 = { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 0, 1 } };

		for (int n = 0; n < 100; ++n) {
			net2.train(inputvs2, outputvs2, 1);
		}

		net2.errorrate(inputvs2, outputvs2);
		
		System.out.println("\n======== ~Lens Data~ ========");

		int[] layers4 = { 4, 2, 1 }; // three layers
		NeuralNet net4 = new NeuralNet(layers4);
		net4.connectAll();

		// lenseProcess converts our file into input and output arrays
		lensData lensTraining = lensProcess("lenses.training");
		lensTraining.normalize();		// scale our data
		lensData lensTesting = lensProcess("lenses.testing");
		lensTesting.normalize();
		
		for (int n = 0; n < 400; ++n) {
			net4.train(lensTraining.inputvs, lensTraining.outputvs, 1);

			double error = net4.error(lensTraining.inputvs, lensTraining.outputvs);
			//System.out.println("error is " + error);
		}
		
		System.out.print("Training ");
		net4.errorrate(lensTraining.inputvs, lensTraining.outputvs);
		System.out.print("Testing ");
		net4.errorrate(lensTesting.inputvs, lensTesting.outputvs);
		
		System.out.println("\n======= ~Credit Data~ =======");

		DataProcessor data = new DataProcessor("crx.data.training");
		DataProcessor testing = new DataProcessor("crx.data.testing");
		int[] layers3 = { 15, 30, 1 }; // three layers
		NeuralNet net3 = new NeuralNet(layers3);
		net3.connectAll();

		double[][] inputvs3 = data.m_inputvs;
		double[][] outputvs3 = data.m_outputvs;
		double[][] inputvstest = testing.m_inputvs;
		double[][] outputvstest = testing.m_outputvs;

		for (int n = 0; n < 300; ++n) {
			net3.train(inputvs3, outputvs3, 1);

			double error = net3.error(inputvs3, outputvs3);
			//System.out.println("error is " + error);
		}

		System.out.print("Training ");
		net3.errorrate(inputvs3, outputvs3);

		System.out.print("Testing ");
		net3.errorrate(inputvstest, outputvstest);

		return;
	}

	static class lensData{
		double[][] inputvs;
		double[][] outputvs;
		public lensData(){};

		public void normalize(){

			// For each feature in in m_inputvs, find the average
			for(int feature = 0; feature < inputvs[0].length; feature++){

				double avg = 0;
				for(int row = 0; row  < inputvs.length; row++){
					avg += inputvs[row][feature];
				}
				avg = (avg / inputvs.length);				// Divide by number of attributes

				// Find standard deviation of this feature. 	stdDev = sqrt(	(Sum of all (xi - avg)^2) / N)    		
				double stdSum = 0;
				for(int row = 0; row  < inputvs.length; row++){
					double x = (inputvs[row][feature] - avg);
					stdSum += (x*x);
				}

				double stdDev = (stdSum / inputvs.length);
				stdDev = Math.sqrt(stdDev);

				// z-scale attribute #<feature> for each input
				for(int row = 0; row  < inputvs.length; row++){

					double z = (inputvs[row][feature] - avg) / stdDev; 
					inputvs[row][feature] = z;
				}
			}

			// Normalize each feature in m_outputs
			for(int feature = 0; feature < outputvs[0].length; feature++){

				double avg = 0;
				for(int row = 0; row  < outputvs.length; row++){
					avg += outputvs[row][feature];
				}
				avg = (avg / outputvs.length);				// Divide by number of attributes

				// Find standard deviation of this feature
				double stdSum = 0;						// stdDev = sqrt(	(Sum of all (xi - avg)^2) / N)

				for(int row = 0; row  < outputvs.length; row++){
					double x = (outputvs[row][feature] - avg);
					stdSum += (x*x);
				}

				double stdDev = (stdSum / outputvs.length);			// Divide by # of samples
				stdDev = Math.sqrt(stdDev);								// Take square root

				// z-scale attribute #<feature> for each input
				for(int row = 0; row  < outputvs.length; row++){

					double z = (outputvs[row][feature] - avg) / stdDev; 
					outputvs[row][feature] = z;
				}
			}
		}
	}

	public static lensData lensProcess(String name) throws FileNotFoundException{
		List<double[]> inputList = new ArrayList<double[]>();
		List<double[]> outputList = new ArrayList<double[]>();
		lensData dataReturn = new lensData();
		FileReader f = new FileReader(name);
		Scanner s = null;

		try {
			s = new Scanner(new BufferedReader(f));
			s.useLocale(Locale.US);

			while (s.hasNextLine()) {
				String line = s.nextLine();

				Scanner scanner = new Scanner(line);
				scanner.useDelimiter(",");

				double[] inputs = new double[4];
				double[] outputs = new double[1];

				inputs[0] = Double.parseDouble(scanner.next());
				inputs[1] = Double.parseDouble(scanner.next());
				inputs[2] = Double.parseDouble(scanner.next());
				inputs[3] = Double.parseDouble(scanner.next());
				double x = Double.parseDouble(scanner.next());

				if(x == 1){ outputs[0] = 0; }
				if(x == 2){ outputs[0] = 0.5; }
				if(x == 3){ outputs[0] = 1; }

				inputList.add(inputs);
				outputList.add(outputs);
			}
		} finally {
			s.close();
		}

		int i = 0;
		dataReturn.inputvs = new double[inputList.size()][];
		dataReturn.outputvs = new double[outputList.size()][];
		for (double[] arr: inputList) {
			dataReturn.inputvs[i] = arr;
			++i;
		}
		int j = 0;
		for (double[] arr: outputList) {
			dataReturn.outputvs[j] = arr;
			++j;
		}
		return dataReturn;
	}

}
