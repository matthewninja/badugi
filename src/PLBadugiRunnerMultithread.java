
// VERSION MARCH 6, 2018

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PLBadugiRunnerMultithread {
	public static final int NUMBER_OF_HANDS = 270_725;
	public static ArrayList<PLBadugiHand> allHands;
	static Comparator<PLBadugiHand> c = new Comparator<PLBadugiHand>() {
		public int compare(PLBadugiHand h1, PLBadugiHand h2) {
			return h1.compareTo(h2);
		}
	};
	// The initial ante posted by both players in the hand.
	private static final int ANTE = 1;
	// How many bets and raises are allowed during one betting round.
	public static final int MAX_RAISES = 4;
	// Number of hands in each heads-up match.
	public static final int HANDS_PER_MATCH = 50_000; // changed from one million
	// How often to print out the current hand even when silent (-1 means never)
//	private static final int SAMPLE_OUTPUT = 100; // changed from 200_000
	// Minimum raise in each betting round.
	private static final int[] MIN_RAISE = { 4, 2, 2, 1 };
	// Whether two agent objects of same type will play against each other in the
	// tournament.
	private static boolean SAME_TYPE_PLAY = true;
	// How many hands have been played so far in this entire tournament.
	private static final int NUMBER_OF_THREADS = 4;
	static ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	private static int explorations = 0;
	private static FileWriter sarWriters[] = new FileWriter[NUMBER_OF_THREADS];

	// A utility method to output a message to the given PrintWriter, forcing it to
	// flush() after the message.
	private static void message(PrintWriter out, String msg) {
		if (out != null) {
			out.println(msg);
			out.flush();
		}
	}

	public static double handStrength(PLBadugiHand hand) {
		// System.out.println("checking hand stren");
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

	public static void doneHand(List<int[]> states) {
		for (int i = 0; i < states.size(); i++) {
			System.out.println(Arrays.toString(states.get(i)));
		}
		states.clear();
		// System.out.println("done hand");
	}

	public static int collectorPosition;

	public static int explorePlay(int handSize, EfficientDeck deck, PLBadugiPlayer[] players, int handsToGo,
			int currentScore) throws CloneNotSupportedException, IOException {
		// initialize the two agents and state
		if (handsToGo % 2_00 == 0)
			System.out.println("Hand #: " + (HANDS_PER_MATCH - handsToGo) + ", Thread count: " + Thread.activeCount());
		int pot = 2 * ANTE;
		deck.restoreCards();
		int[] totalBets = new int[2];
		totalBets[0] = totalBets[1] = ANTE;
		int[] drawCounts = new int[2];
		drawCounts[0] = drawCounts[1] = -1;
		PLBadugiHand[] hands = new PLBadugiHand[2];
		hands[0] = deck.drawBadugiHand(handSize);
		hands[1] = deck.drawBadugiHand(handSize);
		int raises = 0;
		int calls = -1;
		int currPlayer = 0;

		try {
			players[0].startNewHand(0, handsToGo, currentScore);
			if (players[0].getClass().getSimpleName().equals("PLBadugiDataCollector"))
				collectorPosition = 1;
			else
				collectorPosition = 0;
		} catch (Exception e) {
			return -1000;
		}
		try {
			players[1].startNewHand(1, handsToGo, -currentScore);
		} catch (Exception e) {
			return +1000;
		}

		explore(players, hands, 3, currPlayer, pot, raises, totalBets, calls, drawCounts, deck, UUID.randomUUID(),
				UUID.randomUUID(), 2);
		return 0; // not particularly relevant for the purpose of data collection

	}

	public static void explore(PLBadugiPlayer[] players, PLBadugiHand[] hands, int drawsRemaining, int currPlayer,
			int pot, int raises, int[] totalBets, int calls, int[] drawCounts, EfficientDeck deck,
			UUID stateId, UUID parentStateId, int lastChoice) throws CloneNotSupportedException, IOException {
		int otherPlayer = 1 - currPlayer;
		int toCall = totalBets[otherPlayer] - totalBets[currPlayer];
		int minRaise, maxRaise;
		int highestRaise = ANTE * MIN_RAISE[drawsRemaining];
		if (raises < MAX_RAISES) {
			minRaise = Math.max(highestRaise, 2 * toCall);
			maxRaise = Math.max(highestRaise, pot + 2 * toCall);
		} else { // no more raises allowed this betting round
			minRaise = maxRaise = toCall;
		}

		final double handStren = handStrength(hands[currPlayer]);
		if (players[currPlayer].getClass().getSimpleName().equals("PLBadugiDataCollector")) {

			final UUID parentStateId_clone = stateId;
			final int currPlayer_clone = currPlayer;
			final int drawsRemaining_clone = drawsRemaining;
			final int pot_clone = pot;
			final int raises_clone = raises;
			final int toCall_clone = toCall;
			final int minRaise_clone = minRaise;
			final int maxRaise_clone = maxRaise;
			final int otherPlayer_clone = otherPlayer;
			final int calls_clone = calls;
			final int highestRaise_clone = highestRaise;
			final int[] drawCounts_clone = new int[2];
			System.arraycopy(drawCounts, 0, drawCounts_clone, 0, 2);
			final PLBadugiPlayer[] players_clone = players.clone();
			// players.clone();

			for (int i = 2; i >= 0; i--) {

				final PLBadugiHand[] hands_clone = new PLBadugiHand[hands.length];
				for (int j = 0; j < hands_clone.length; j++) {
					List<Card> h = hands[j].getAllCards();
					if (h != null) {
						hands_clone[j] = new PLBadugiHand(h);
					}
				}
				// Spawn each sub-state in a new thread
				final int choice = i;
				pool.execute(() -> {
					final UUID sId = UUID.randomUUID();
					int[] betsCopy = new int[2];
					System.arraycopy(totalBets, 0, betsCopy, 0, 2);

					// clone deck so that we draw the same card for all actions
					EfficientDeck deckClone;
					try {
						deckClone = (EfficientDeck) deck.clone();
						int action = ((PLBadugiDataCollector) players_clone[currPlayer_clone]).bettingActionForced(
								choice, drawsRemaining_clone, handStren, pot_clone, raises_clone, toCall_clone,
								minRaise_clone, maxRaise_clone, drawCounts_clone[otherPlayer_clone]);
						sarWriters[(int) (Thread.currentThread().getId()) % NUMBER_OF_THREADS].write(new Sar(sId, parentStateId_clone, choice, drawsRemaining_clone, handStren, pot_clone,
								raises_clone, toCall_clone, drawCounts_clone[otherPlayer_clone],
								carryOn(action, toCall_clone, minRaise_clone, maxRaise_clone, betsCopy,
										currPlayer_clone, calls_clone, raises_clone, highestRaise_clone, pot_clone,
										players_clone, hands_clone, drawsRemaining_clone, drawCounts_clone,
										deckClone, sId, parentStateId_clone, choice),
								0, collectorPosition).toString() + '\n');
					} catch (CloneNotSupportedException | IOException e) {
						e.printStackTrace();
					}

				});
			}
		} else {
			// other agent acts. if fold, terminal
			double DOESNT_MATTER = 0.0;
			int action = players[currPlayer].bettingAction(drawsRemaining, hands[currPlayer], pot, raises, toCall,
					minRaise, maxRaise, drawCounts[otherPlayer]);
			// change something in sar if carryOn != null
			Integer reward = carryOn(action, toCall, minRaise, maxRaise, totalBets, currPlayer, calls, raises,
					highestRaise, pot, players, hands, drawsRemaining, drawCounts, deck, stateId, parentStateId,
					lastChoice);
			if (reward != null)
				reward = -reward;
			sarWriters[(int) (Thread.currentThread().getId()) % NUMBER_OF_THREADS].write(new Sar(stateId, parentStateId, lastChoice, drawsRemaining, DOESNT_MATTER, pot, raises, toCall,
					drawCounts[otherPlayer], reward, 1, collectorPosition).toString()+ '\n');
		}
	}

	public static Integer carryOn(int action, int toCall, int minRaise, int maxRaise, int[] totalBets, int currPlayer,
			int calls, int raises, int highestRaise, int pot, PLBadugiPlayer[] players, PLBadugiHand[] hands,
			int drawsRemaining, int[] drawCounts, EfficientDeck deck, UUID stateId, UUID parentStateId,
			int choice) throws CloneNotSupportedException, IOException {
		explorations++;
		if (explorations % 10_000 == 0)
			System.out.println("Exploration #: " + explorations);
		if (action > toCall && action < minRaise) {
			action = toCall;
		}
		if (action > maxRaise) {
			action = maxRaise;
		}
		if (action < toCall) { // current player folds, the hand is finished
			// terminal
			if (players[currPlayer].getClass().getSimpleName().equals("PLBadugiDataCollector")) {
			} else {
			}
			if (currPlayer == 0) {
				players[0].handComplete(hands[0], hands[1], -totalBets[0]);
				players[1].handComplete(hands[1], hands[0], totalBets[1]);
			} else {
				players[0].handComplete(hands[0], hands[1], totalBets[0]);
				players[1].handComplete(hands[1], hands[0], -totalBets[1]);
			}

			return -totalBets[currPlayer];
		} else if (action == toCall) { // current player merely calls
			calls++;
		} else { // current player raises
			// recurse
			raises++;
			calls = 0;
			// update the highest raise made on this betting round
			if (action - toCall > highestRaise) {
				highestRaise = action - toCall;
			}
		}

		pot += action;
		totalBets[currPlayer] += action;
		currPlayer = 1 - currPlayer;

		// if more betting this round
		if (calls < 1) {
			explore(players, hands, drawsRemaining, currPlayer, pot, raises, totalBets, calls, drawCounts, deck,
					stateId, parentStateId, choice);
		} else if (drawsRemaining > 0) { // if drawing
			for (currPlayer = 0; currPlayer <= 1; currPlayer++) {
				List<Card> cards = hands[currPlayer].getAllCards();
				List<Card> toReplace;
				try {
					toReplace = players[currPlayer].drawingAction(drawsRemaining, hands[currPlayer], pot,
							currPlayer == 0 ? -1 : drawCounts[0]);
					if (toReplace.size() > 4) {
						throw new IllegalArgumentException("Trying to replace too many cards.");
					}
					for (Card c : toReplace) {
						if (!cards.contains(c)) {
							throw new IllegalArgumentException("Trying to replace nonexistent card " + c);
						}
						hands[currPlayer].replaceCard(c, deck);
					}
					drawCounts[currPlayer] = toReplace.size();
				} catch (Exception e) {
				}
			}
			drawsRemaining--;
			// currPlayer is 0 after drawing
			explore(players, hands, drawsRemaining, 0, pot, raises, totalBets, calls, drawCounts, deck, stateId,
					parentStateId, choice);
		} else { // showdown
			int showdown = hands[0].compareTo(hands[1]);
			int result = showdown < 0 ? -totalBets[0] : (showdown > 0 ? totalBets[1] : 0);
			if (showdown != 0) {
				try {
					players[0].handComplete(hands[0], hands[1], showdown > 0 ? totalBets[0] : -totalBets[0]);
				} catch (Exception e) {
				}
				try {
					players[1].handComplete(hands[1], hands[0], showdown < 0 ? totalBets[1] : -totalBets[1]);
				} catch (Exception e) {
				}
				try {
					players[0].handComplete(hands[0], hands[1], showdown > 0 ? totalBets[0] : -totalBets[0]);
				} catch (Exception e) {
				}
				try {
					players[1].handComplete(hands[1], hands[0], showdown < 0 ? totalBets[1] : -totalBets[1]);
				} catch (Exception e) {
				}

				return result;
			} else {
				try {
					players[0].handComplete(hands[0], hands[1], 0);
				} catch (Exception e) {
				}
				try {
					players[1].handComplete(hands[1], hands[0], 0);
				} catch (Exception e) {
				}
				try {
					players[0].handComplete(hands[0], hands[1], 0);
				} catch (Exception e) {
				}
				try {
					players[1].handComplete(hands[1], hands[0], 0);
				} catch (Exception e) {
				}
				return 0;
				// no gain/loss
			}

		}

		return null;

	}

	/**
	 * Play the given number of hands of heads-up badugi between the two players,
	 * alternating the dealer position between each round.
	 * 
	 * @param rng     The random number generator used to initialize the deck in
	 *                each hand.
	 * @param players The two players participating in this heads-up match.
	 * @param out     The PrintWriter used to write the verbose messages about the
	 *                events in this hand. To silence this output, use e.g. new
	 *                FileWriter("/dev/null") as this argument in an Unix system.
	 * @param hands   How many hands to play in this heads-up match.
	 * @return The result of the match, as indicated by the amount won by player 0
	 *         from player 1. A negative result therefore means that the player 0
	 *         lost the match.
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static int playHeadsUp(EfficientDeck deck, PLBadugiPlayer[] players, PrintWriter out, int hands)
			throws CloneNotSupportedException, IOException, InterruptedException {
		int score = 0;
		PLBadugiPlayer[] thisRoundPlayers = new PLBadugiPlayer[2];
		players[0].startNewMatch(hands);
		players[1].startNewMatch(hands);
		
		for (int i = 0; i < sarWriters.length; i++) {
			sarWriters[i] = new FileWriter("sars/sar"+i+".txt", true);
		}
		while (--hands >= 0) {
			if (hands % 1_000 == 0) {
				}
			}
			if (hands % 2 == 0) {
				thisRoundPlayers[0] = players[0];
				thisRoundPlayers[1] = players[1];
			} else {
				thisRoundPlayers[0] = players[1];
				thisRoundPlayers[1] = players[0];
			}
			int sign = (hands % 2 == 0 ? +1 : -1);

			explorePlay(4, deck, thisRoundPlayers, hands, sign * score);
			
			while (((ThreadPoolExecutor)(pool)).getQueue().size()  > 0) {
				System.out.println(((ThreadPoolExecutor)(pool)).getQueue().size() + " Threads in queue. Going to sleep...");
				Thread.sleep(5000);
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("Due to an interrupt, not all threads finished. Exiting.");
			System.exit(130);
		}
		for (int i = 0; i < sarWriters.length; i++) {
			sarWriters[i].close();
		}
		players[0].finishedMatch(score);
		players[1].finishedMatch(-score);
		return score;
	}

	/**
	 * Play the entire multiagent Badugi tournament, one heads-up match between
	 * every possible pair of agents.
	 * 
	 * @param agentClassNames A string array containing the names of agent
	 *                        subclasses.
	 * @param out             A PrintWriter to write the results of the individual
	 *                        heads-up matches into.
	 * @param results         A PrintWriter to write the tournament results into.
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void badugiTournament(String[] agentClassNames, PrintWriter out, PrintWriter results)
			throws CloneNotSupportedException, IOException, InterruptedException {

		// Create the list of player agents.
		List<PLBadugiPlayer> players = new ArrayList<PLBadugiPlayer>(agentClassNames.length);
		for (String agent : agentClassNames) {
			Class c = null;
			try {
				c = Class.forName(agent);
			} catch (Exception e) {
				System.out.println("Unable to load class bytecode for [" + agent + "]. Exiting.");
				return;
			}
			PLBadugiPlayer bp = null;
			try {
				bp = (PLBadugiPlayer) (c.newInstance());
			} catch (Exception e) {
				System.out.println("Unable to instantiate class [" + agent + "]. Exiting.");
				return;
			}
			players.add(bp);
		}
		int[] scores = new int[players.size()];
		Random rng;
		String seed = "This string is to be used as seed of secure random number generator "
				+ System.currentTimeMillis();
		try {
			rng = new SecureRandom(seed.getBytes());
		} catch (Exception e) {
			message(out, "Unable to create secure RNG: " + e);
			message(out, "Using system Random class instead.");
			rng = new Random();
		}
		// One and the same deck object is reused through the entire tournament.
		EfficientDeck deck = new EfficientDeck(rng);

		// Play and score the individual heads-up matches.
		for (int i = 0; i < players.size(); i++) {
			for (int j = i + 1; j < players.size(); j++) {
				if (!SAME_TYPE_PLAY && agentClassNames[i].equals(agentClassNames[j])) {
					continue;
				}
				PLBadugiPlayer[] playersArr = { players.get(i), players.get(j) };

				int result = playHeadsUp(deck, playersArr, null, HANDS_PER_MATCH);
				out.print("[" + players.get(i).getAgentName() + "] vs. [" + players.get(j).getAgentName() + "]: ");
				if (result < 0) {
					scores[j] += 2;
				} else if (result > 0) {
					scores[i] += 2;
				} else {
					scores[j]++;
					scores[i]++;
				}
				out.println(result);
				out.flush();
			}
		}

		for (int i = 0; i < players.size(); i++) {
			int max = 0;
			for (int j = 1; j < players.size(); j++) {
				if (scores[j] > scores[max]) {
					max = j;
				}
			}
			String name = players.get(max).getAgentName();
			results.println((i + 1) + " : " + name + " : " + scores[max]);
			scores[max] = -scores[max];
		}

		for (int i = 0; i < players.size(); i++) {
			scores[i] = -scores[i];
		}

		results.println("\n\n");

		for (int i = 0; i < players.size(); i++) {
			int max = 0;
			for (int j = 1; j < players.size(); j++) {
				if (scores[j] > scores[max]) {
					max = j;
				}
			}
			int pos = players.size() - i;
			results.println((i + 1) + ": " + players.get(max).getAuthor());
			scores[max] = -scores[max];
		}
	}

	/**
	 * Play three hands in the verbose mode. Suitable for watching your agents play.
	 * 
	 * @throws CloneNotSupportedException
	 * @throws InterruptedException 
	 */
	public static void playThreeHandTournament() throws IOException, CloneNotSupportedException, InterruptedException {
		PLBadugiPlayer[] players = {
				// Replace these with some suitable objects.
				new IlkkaPlayer3(), new IlkkaPlayer3() };
		Random rng;
		String seed = "This string is to be used as seed of secure random number generator";
		try {
			rng = new SecureRandom(seed.getBytes());
		} catch (Exception e) {
			System.out.println("Unable to create a secure RNG. Using java.util.Random instead.");
			rng = new Random();
		}
		EfficientDeck deck = new EfficientDeck(rng);
		int result = playHeadsUp(deck, players, new PrintWriter(System.out), 3);
		System.out.println("\n\nMatch result is " + result + ".");
	}

	/**
	 * Run the entire badugi tournament between agents from classes listed inside
	 * this method.
	 * 
	 * @throws CloneNotSupportedException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, CloneNotSupportedException, InterruptedException {
		allHands = PokerHands.allHands();
		/*
		 * Modify this array to include the player classes that participate in the
		 * tournament.
		 */

		String[] playerClasses = { "PLBadugi500854978", "PLBadugiDataCollector" };

		PrintWriter out = new PrintWriter(System.out);
		PrintWriter result = new PrintWriter(new FileWriter("results.txt"));
		badugiTournament(playerClasses, out, result);
		result.close();
	}
}