import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.encog.ml.MLRegression;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

public class Deep_PLBadugi500805168 implements PLBadugiPlayer {
	public static final String FILENAME = "data/network.eg";
	BasicNetwork network;
	/*****************************************************
	 * Initialization related variables ********************
	 *****************************************************/
	String name;
	private static int decisions_made = 0;
	public static int call_count = 0;
	public static int fold_count = 0;
	public static int raise_count = 0;
	public static int check_count = 0;
	private static int id = 0; // Counter of how many objects have been created.
	Scanner sc = new Scanner(System.in);

	NormalizationHelper helper;
	MLRegression bestMethod;
	boolean loadNetwork = false;
	public static final int NUMBER_OF_HANDS = 270_725;
	public static ArrayList<PLBadugiHand> allHands;
	static Comparator<PLBadugiHand> c = new Comparator<PLBadugiHand>() {
		public int compare(PLBadugiHand h1, PLBadugiHand h2) {
			return h1.compareTo(h2);
		}
	};

	/*****************************************************
	 * Hand related variables ******************************
	 *****************************************************/
	private int position;
	boolean autoFold = false;

	public Deep_PLBadugi500805168(String name) throws IOException {
		this.name = name;
		initModel();
		// create MultiLayerPerceptron neural network
//		neuralNet = new MultiLayerPerceptron(6, 16, 3);
//		testDataSet = DataSet.createFromFile(inputFileName, 6, 3, ",");
//		norm.normalize(testDataSet);
//		System.out.println("training...");
//		neuralNet.learn(testDataSet);
//		System.out.println("Done training...");
	}

	public Deep_PLBadugi500805168() throws IOException {
		this.name = "Matthew's Agent " + (++id);
		initModel();
		// create MultiLayerPerceptron neural network
//		neuralNet = new MultiLayerPerceptron(6, 16, 3);
//		testDataSet = DataSet.createFromFile(inputFileName, 6, 3, ",");
//		norm.normalize(testDataSet);
//		System.out.println("training...");
//		neuralNet.learn(testDataSet);
//		System.out.println("Done training...");
	}

	@Override
	public void startNewHand(int position, int handsToGo, int currentScore) {
		this.position = position;
		if (currentScore > handsToGo)
			autoFold = true;
		else
			autoFold = false;
	}

	@Override
	public int bettingAction(int drawsRemaining, PLBadugiHand hand, int pot, int raises, int toCall, int minRaise,
			int maxRaise, int opponentDrew) {
//		if (autoFold)
//			return 0;
		int bet = betAmount(hand, pot, minRaise, maxRaise, toCall);
		decisions_made++;

		if (toCall == 0 && bet == toCall)
			check_count++;
		else if (bet < toCall)
			fold_count++;
		else if (toCall == bet)
			call_count++;
		else
			raise_count++;

		return bet;

		/*
		 * double[] line = { (double) drawsRemaining, handStrength(hand), (double) pot,
		 * (double) raises, (double) toCall, (double) opponentDrew, (double) position };
		 * BasicMLData input = new BasicMLData(line); MLData output =
		 * network.compute(input); int choice = getIndexOfLargest(output.getData()); if
		 * (choice == 0) { call_count++; return toCall; // flat call } else if (choice
		 * == 1) { fold_count++; return 0; // fold } else { raise_count++; return (int)
		 * (minRaise + (maxRaise - minRaise) * 0.3); // raise }
		 */
	}

	@Override
	public List<Card> drawingAction(int drawsRemaining, PLBadugiHand hand, int pot, int dealerDrew) {
		List<Card> inactiveCards = hand.getInactiveCards();
		List<Card> pitch = new ArrayList<Card>();

		// With a four card badugi, pitch nothing.
		if (inactiveCards.size() == 0)
			return pitch;

		// Pitch all inactive cards
		for (Card c : inactiveCards) {
			pitch.add(c);
		}

		return pitch;
	}

	@Override
	public void handComplete(PLBadugiHand yourHand, PLBadugiHand opponentHand, int result) {
		// update model

	}

	@Override
	public String getAgentName() {
		return name;
	}

	@Override
	public String getAuthor() {
		return "Matthew Ham";
	}

	public void initModel() throws IOException {
		System.out.println("Loading hands");
		allHands = PokerHands.allHands();
		System.out.println("Loading network");
		network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(FILENAME));
	}

	private int getIndexOfLargest(double[] array) {
		if (array == null || array.length == 0)
			return -1; // null or empty

		int largest = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > array[largest])
				largest = i;
		}
		return largest; // position of the first largest found
	}

	private int betAmount(PLBadugiHand hand, int pot, int minRaise, int maxRaise, int toCall) {
		// handStrength(hand) is rough equity. It doesn't consider draws left or
		// opponent behaviour
		double equity = handStrength(hand); // naive
		int bet = 0;
		double expected = EV(pot, bet, equity, toCall);
		// if (toCall == 0)
		// expected = (double) pot / 2.0f;
		// if (EV((double) pot, toCall, equity, toCall) > expected)
		for (int i = 0; i <= toCall; i++) {
			double e = EV(pot, i, equity, toCall);
			if (e > expected) {
				bet = i;
				expected = e;
			}
		}
		for (int i = minRaise; i <= maxRaise; i++) {
			double e = EV(pot, i, equity, toCall);
			if (e > expected) {
				bet = i;
				expected = e;
			}
		}
		System.out.println("pot: " + pot + ", toCall: " + toCall + ", maxRaise: " + maxRaise + ", Best bet is: " + bet);
		System.out.println("EV for a bet of " + bet + ": " + EV(pot, bet, equity, toCall));
		return bet;
	}

	private double EV(int pot, int bet, double equity, double foldEquity, int toCall) {
		if (bet < toCall)
			return 0; // That's a fold!
		else if (bet == toCall)
			pot += bet; // That's a call!
		else if (bet > toCall)
			pot += bet; // naive, doesn't consider fold equity
		double e = (equity * pot) - (bet * (1.0 - equity));
		return e;
	}

	private double EV(int pot, int bet, double equity, int toCall) {
		if (bet < toCall)
			return 0; // That's a fold!
		else if (bet == toCall)
			pot += bet; // That's a call!
		else if (bet > toCall)
			pot += bet; // naive, doesn't consider fold equity
		double e = (equity * pot) - (bet * (1.0 - equity));
		return e;
	}

	public static double handStrength(PLBadugiHand hand) {
		String hand_string = hand.toString().substring(1, 15).replaceAll(" ", "");
		List<Card> sortable_hand = new ArrayList<Card>();

		String[] arr = hand_string.split(","); // length 8

		for (int i = 0; i < 4; i++) {
			sortable_hand.add(Card.from(arr[i]));
		}
		Collections.sort(sortable_hand);

		PLBadugiHand hand2 = new PLBadugiHand(sortable_hand);
		// binary search
		int index = Collections.binarySearch(allHands, hand2, c);

		double stren = (double) index / NUMBER_OF_HANDS;
		if (stren < 0)
			throw new RuntimeException("not a valid hand. stren: " + stren);
		return stren;
	}

	public static void doneGame() {
		System.out.println("Decisions made: " + decisions_made);
		double call = (double) call_count / (double) decisions_made;
		double fold = (double) fold_count / (double) decisions_made;
		double raise = (double) raise_count / (double) decisions_made;
		double check = (double) check_count / (double) decisions_made;
		System.out.println("call %: " + call + ", fold %: " + fold + ", raise %: " + raise + ", check %: " + check);
	}

	class AgentModel {
		Mean mean_raise; 
		NormalDistribution n;
		StandardDeviation s;

		AgentModel() {
			mean_raise = new Mean();
		}

		// these should change based on the state
		int check_bet[] = new int[] { 0, 0 };
		double bet_amount = 0.0d; 
									
		int fold_call_raise[] = new int[] { 0, 0, 0 }; // there is another state called fold_call
		double raise_amount = 0.0d; // percentage that they raise by from 0-1

		Double fold_equity(/* state */) {
//			if () return null; // statistical significance needed
			int fold_opportunities = IntStream.of(fold_call_raise).sum();
			return ((double) fold_call_raise[0] / fold_opportunities);
		}

		double predict_raise() { // From 0-1, the percentage that they raise 
			// this is naive, because people raise more depending on the state
			// neural network can do this!
			return mean_raise.getResult();
		}

		double probability_raise() {
			// state matters very much here. For now, just P(R|fold_call_raise)

			return 0.0d;
		}

		// check_bet choice
		void update(boolean check, int bet, int maxRaise) {
			if (check)
				check_bet[0]++;
			else {
				check_bet[1]++;
				bet_amount = (bet_amount * (check_bet[1] - 1) + ((double)bet / maxRaise)) / check_bet[1];
			}
		}

		// fold_call_raise choice
		void update(boolean fold, boolean call, int raise, int maxRaise) {
			if (fold && call)
				throw new RuntimeException("Error. You just folded and called at the smame time...");
			if (fold)
				fold_call_raise[0]++;
			else if (call)
				fold_call_raise[1]++;
			else { // this agent raised
				mean_raise.increment((double)raise / maxRaise);
				fold_call_raise[2]++;
				// let raise_amount be updated to the new mean
				raise_amount = (raise_amount * (fold_call_raise[2] - 1) + ((double)raise / maxRaise)) / fold_call_raise[2];
			}
		}

		void reset() {
			check_bet = new int[] { 0, 0 };
			fold_call_raise = new int[] { 0, 0, 0 };
			mean_raise.clear();
		}
	}
}
