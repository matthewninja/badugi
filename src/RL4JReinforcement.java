import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.ILearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.mdp.toy.SimpleToyState;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.RmsProp;

public class RL4JReinforcement {

	private static DQNFactoryStdDense.Configuration BADUGI_CONF;

	private DQN badugi;

	private DataManager manager;

	private File file = File.createTempFile("rl4j-dqn-", ".model");;

	public static QLearning.QLConfiguration BADUGI_QL =
            new QLearning.QLConfiguration(
                    123,   //Random seed
                    100000,//Max step By epoch
                    80000, //Max step
                    10000, //Max size of experience replay
                    32,    //size of batches
                    100,   //target update (hard)
                    0,     //num step noop warmup
                    0.05,  //reward scaling
                    0.99,  //gamma
                    10.0,  //td-error clipping
                    0.1f,  //min epsilon
                    2000,  //num step for eps greedy anneal
                    true   //double DQN
            );

	public RL4JReinforcement() throws IOException {
		// if not loading a DQN
		badugi = new DQNFactoryStdDense(BADUGI_CONF).buildDQN(new int[] { 6 }, 3);

		BADUGI_CONF = new DQNFactoryStdDense.Configuration(3, // number of layers
				9, // number of hidden nodes
				0.01, // l2 regularization
				new RmsProp(0.01), null // no listener
		);

		manager = new DataManager();

	}

	public int train(Sar sar) throws IOException {
		// input & labels come from Sar.
		INDArray input = Nd4j.create(new double[] {1.0,2.0});
		INDArray labels = Nd4j.create(new double[] {2.0, 1.0});

		MDP mdp = new Badugi();

		//define the training
        ILearning<BadugiState, Integer, DiscreteSpace> dql = new QLearningDiscreteDense<BadugiState>(mdp, badugi, BADUGI_QL, manager);

        dql.train();

        badugi.save(file.getAbsolutePath());

//		badugi.fit(input, labels);
		return 0;
	}
	
	public int predict (Sar sar) throws IOException {
		MultiLayerNetwork mln = ModelSerializer.restoreMultiLayerNetwork(file.getAbsolutePath());
		
		int[] prediction = mln.predict(Nd4j.create(new double[] {1.0,2.0}));// input from sar
		
//		INDArray prediction = badugi.output(Nd4j.create(new double[] {1.0,2.0})); // input from sar
		
		int choice = 0;

		for (int i = 0; i < prediction.length; i++) {
			choice = prediction[i] > prediction[choice] ? i : choice;
		}
		
		return choice;
	}

	public void loadAndSave() throws IOException {
		

		

		// DQN dqn2 = DQN.load(file.getAbsolutePath());

//        assertEquals(badugi.mln, dqn2.mln);
	}
}