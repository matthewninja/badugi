import lombok.Getter;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.json.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Random;
import java.util.logging.Logger;

/**
 * @author rubenfiszel (ruben.fiszel@epfl.ch) on 8/9/16.
 *
 *         A toy MDP where the agent should find the maximum to get the reward.
 *         Useful to debug as it's very fast to run
 */
public class Badugi implements MDP<BadugiState, Integer, DiscreteSpace> {

	final private static int MAX_STEP = 20; // might be 16?

	final private static int SEED = 1234;

	final private static int ACTION_SIZE = 3;

	final private static int STATE_SIZE = 7;

	// private BadugiState[] states = genBadugiStates(MAX_STEP, SEED);

	@Getter
	private DiscreteSpace actionSpace; //  = new DiscreteSpace(ACTION_SIZE)

    private ObservationSpace<BadugiState> state;
    
	@Override
	public ObservationSpace<BadugiState> getObservationSpace() {
		return state;
	}

	private BadugiState badugiState;

//    public void printTest(IDQN idqn) {
//        INDArray input = Nd4j.create(MAX_STEP, ACTION_SIZE);
//        for (int i = 0; i < MAX_STEP; i++) {
//            input.putRow(i, Nd4j.create(states[i].toArray()));
//        }
//        INDArray output = Nd4j.max(idqn.output(input), 1);
//        Logger.getAnonymousLogger().info(output.toString());
//    }

//    public static int maxIndex(double[] values) {
//        double maxValue = -Double.MIN_VALUE;
//        int maxIndex = -1;
//        for (int i = 0; i < values.length; i++) {
//            if (values[i] > maxValue) {
//                maxValue = values[i];
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }

	// public static BadugiState[] genBadugiStates(int size, int seed) {

	// Random rd = new Random(seed);
	// BadugiState[] badugiStates = new BadugiState[size];
	// // shouldn't be random. load from file.
	// for (int i = 0; i < size; i++) {

	// double[] state_values = new double[ACTION_SIZE];

	// for (int j = 0; j < ACTION_SIZE; j++) {
	// state_values[j] = rd.nextDouble();
	// }
	// badugiStates[i] = new BadugiState(state_values, i);
	// }

	// return badugiStates;
	// }

	public void close() {
	}

	@Override
	public boolean isDone() { // logic is wrong. might terminate earlier.

		return badugiState.getStep() == MAX_STEP - 1;
	}

	public BadugiState reset() {
		return badugiState;
		// return badugiState = states[0];
	}

	public StepReply<BadugiState> step(Integer action) {
        // this rewards your action.
//		state = new ArrayObservationSpace();
		double reward = 0;

		// not sure what a is.
		// so... badugiState double array has the truth values or the three choices?
		// Instead, the reward should be equal to the chips won or lost by a choice
		// reward = rewardValue(sar)

//        if (action == maxIndex(badugiState.getValues()))
//            reward += 1;

		// Move on to the next state
//        badugiState = states[badugiState.getStep() + 1];

		// Return a StepReply object w/ reward
		return new StepReply(badugiState, reward, isDone(), new JSONObject("{}"));
	}

	public Badugi newInstance() {
		return new Badugi();
	}

}
