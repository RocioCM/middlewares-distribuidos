package org.cloudsimplus;

public class Metrics {
	private static final int ITERATIONS = 30;
	private static double[][] results = new double[30][];


	public static void main(String[] args) {
		new Metrics();
	}

	private Metrics() {
		for (int i = 0; i<ITERATIONS; i++) {
			DatacenterBrokerHeuristicExample simulation = new DatacenterBrokerHeuristicExample();
			results[i] = simulation.print();
		}

		for (int i = 0; i<ITERATIONS; i++) {
			double[] metrics = results[i];
			for (int j = 0; j<metrics.length; j++) {
				System.out.print(metrics[j]);
				System.out.print(",");
			}
			System.out.println("");
		}
	}
}
