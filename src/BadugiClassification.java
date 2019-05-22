import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationElliott;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.persist.EncogDirectoryPersistence;

public class BadugiClassification {

	public static final String FILENAME = "data/network.eg";

	public void trainAndSave() throws FileNotFoundException {
		// Turn CSV into double[][] arrays for input and ideal
		// File file = new File("sars_clean/sar.txt");
		File folder = new File("sars_clean");
		File[] listOfFiles = folder.listFiles();

		String thisLine;
		List<double[]> sars = new ArrayList<double[]>();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (!file.getName().contains("DS_")) {
					Scanner sc = new Scanner(file);
					while (sc.hasNext()) {
						thisLine = sc.nextLine();
						String[] arr = thisLine.split(",");
						double sar[] = new double[10];
						for (int i = 0; i < arr.length; i++) {
							sar[i] = Double.parseDouble(arr[i]);
						}
						sars.add(sar);
					}
					sc.close();
				}
			}
			System.out.println("Loaded sars: " + file.getName());
		}
		System.out.println("Checkpoint");
		int rows = sars.size();
		double[][] TSAR_INPUT = new double[rows][7];
		double[][] TSAR_IDEAL = new double[rows][3];
		for (int i = 0; i < rows; i++) {
			TSAR_INPUT[i] = Arrays.copyOfRange(sars.get(i), 0, 7);
			TSAR_IDEAL[i] = Arrays.copyOfRange(sars.get(i), 7, 10);
			// System.out.println(Arrays.toString(TSAR_IDEAL[i]));
		}

		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, false, 7));
		network.addLayer(new BasicLayer(new ActivationElliott(), false, 9));
		network.addLayer(new BasicLayer(new ActivationElliott(), false, 3));
		network.getStructure().finalizeStructure();

		// train the neural network
		MLDataSet trainingSet = new BasicMLDataSet(TSAR_INPUT, TSAR_IDEAL);
		Backpropagation train = new Backpropagation(network, trainingSet);
		// train.setLearningRate(0.000001);
		int epoch = 1;
		// train.setMomentum(0.3f);

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + ", Error:" + train.getError() + ", Momentum: " + train.getMomentum()
					+ ", LR: " + train.getLearningRate());
			epoch++;
		} while (epoch < 3);
		// train.finishTraining();

		System.out.println("Saving network");
		EncogDirectoryPersistence.saveObject(new File(FILENAME), network);
	}

	public void loadAndEvaluate() {
		System.out.println("Loading network");
		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(FILENAME));

		// MLDataSet data = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);
		// double e = network.calculateError(trainingSet);
		// System.out.println("Loaded network's error is(should be same as above): " +
		// e);
	}

	public void loadAndTrain() throws FileNotFoundException {
		System.out.println("Loading network");
		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(FILENAME));

		File folder = new File("sars_clean");
		File[] listOfFiles = folder.listFiles();

		String thisLine;
		List<double[]> sars = new ArrayList<double[]>();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (!file.getName().contains("DS_")) {
					Scanner sc = new Scanner(file);
					while (sc.hasNext()) {
						thisLine = sc.nextLine();
						String[] arr = thisLine.split(",");
						double sar[] = new double[10];
						for (int i = 0; i < arr.length; i++) {
							sar[i] = Double.parseDouble(arr[i]);
						}
						sars.add(sar);
					}
					sc.close();
				}
			}
			System.out.println("Done a file");
		}
		System.out.println("Checkpoint");
		int rows = sars.size();
		double[][] TSAR_INPUT = new double[rows][7];
		double[][] TSAR_IDEAL = new double[rows][3];
		for (int i = 0; i < rows; i++) {
			TSAR_INPUT[i] = Arrays.copyOfRange(sars.get(i), 0, 7);
			TSAR_IDEAL[i] = Arrays.copyOfRange(sars.get(i), 7, 10);
			// System.out.println(Arrays.toString(TSAR_IDEAL[i]));
		}
		MLDataSet trainingSet = new BasicMLDataSet(TSAR_INPUT, TSAR_IDEAL);
		Backpropagation train = new Backpropagation(network, trainingSet);
		// train.setLearningRate(0.1);
		int epoch = 1;
		// train.setMomentum(0.3f);

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + ", Error:" + train.getError() + ", Momentum: " + train.getMomentum()
					+ ", LR: " + train.getLearningRate());
			epoch++;
		} while (epoch < 3);
		// train.finishTraining();

		System.out.println("Saving network");
		EncogDirectoryPersistence.saveObject(new File(FILENAME), network);
	}

	public void loadAndFinalize() {
		System.out.println("Loading network");
		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(FILENAME));

		// MLDataSet data = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);
		// double e = network.calculateError(trainingSet);
		// System.out.println("Loaded network's error is(should be same as above): " +
		// e);
	}

	public static void main(String[] args) {
		try {
			BadugiClassification program = new BadugiClassification();
//			 program.trainAndSave();
			program.loadAndTrain();
			// program.loadAndFinalize();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			Encog.getInstance().shutdown();
		}

	}
}