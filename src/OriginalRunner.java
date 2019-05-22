
// VERSION MARCH 6, 2018

import java.util.*;
import java.io.*;
import java.security.*;

public class OriginalRunner {

	// The initial ante posted by both players in the hand.
	private static final int ANTE = 1;
	// How many bets and raises are allowed during one betting round.
	public static final int MAX_RAISES = 4;
	// Number of hands in each heads-up match.
	public static final int HANDS_PER_MATCH = 10_000;
	// How often to print out the current hand even when silent (-1 means never)
	private static final int SAMPLE_OUTPUT = 1_000;
	// Minimum raise in each betting round.
	private static final int[] MIN_RAISE = { 4, 2, 2, 1 };
	// Whether two agent objects of same type will play against each other in the
	// tournament.
	private static boolean SAME_TYPE_PLAY = true;
	// How many hands have been played so far in this entire tournament.
	private static long handCount = 0;

	// A utility method to output a message to the given PrintWriter, forcing it to
	// flush() after the message.
	private static void message(PrintWriter out, String msg) {
		if (out != null) {
			out.println(msg);
			out.flush();
		}
	}

	/**
	 * Play one hand of badugi, with both players first placing an ANTE, after which
	 * the betting takes place in fixed size increments depending on the street. For
	 * simplicity, both players are assumed to have deep enough stacks so that the
	 * concept of all-in does not emerge.
	 * 
	 * @param handSize     How many cards each player receives to his hand.
	 * @param deck         The deck of cards used to play this hand.
	 * @param players      The two-element array of the players in this hand, in the
	 *                     order (dealer, opponent).
	 * @param out          The PrintWriter used to write the verbose messages about
	 *                     the events in this hand. If null, there is no output.
	 * @param handsToGo    How many hands are left in the current heads-up match.
	 * @param currentScore The current score of player 0.
	 * @return The result of the hand, as indicated by the amount won by player 0
	 *         from player 1. A negative result therefore means that the player 0
	 *         lost the hand.
	 */
	public static int playOneHand(int handSize, EfficientDeck deck, PLBadugiPlayer[] players, PrintWriter out,
			int handsToGo, int currentScore) {
		message(out, "\n----\nHand #" + handCount + " for " + players[0].getAgentName() + " vs. "
				+ players[1].getAgentName() + ". Both players ante " + ANTE + ".");
		int pot = 2 * ANTE;
		deck.restoreCards();
		int[] totalBets = new int[2];
		totalBets[0] = totalBets[1] = ANTE;
		int[] drawCounts = new int[2];
		drawCounts[0] = drawCounts[1] = -1;
		PLBadugiHand[] hands = new PLBadugiHand[2];
		hands[0] = deck.drawBadugiHand(handSize);
		hands[1] = deck.drawBadugiHand(handSize);

		try {
			players[0].startNewHand(0, handsToGo, currentScore);
		} catch (Exception e) {
			return -1000;
		}
		try {
			players[1].startNewHand(1, handsToGo, -currentScore);
		} catch (Exception e) {
			return +1000;
		}

		// A single badugi hand consists of four betting streets and three draws.
		for (int drawsRemaining = 3; drawsRemaining >= 0; drawsRemaining--) {
			message(out, "Pot is " + pot + " chips, " + drawsRemaining + " draws remain.");
			message(out, players[0].getAgentName() + " has " + hands[0] + ", " + players[1].getAgentName() + " has "
					+ hands[1] + ".");

			int currPlayer = 0; // Dealer starts the betting on each street
			int calls = -1; // Number of consecutive calls made in this betting round.
			int raises = 0; // The number of bets and raises made in this betting round.
			int action; // How many chips the current player pushes into the pot in his turn to bet.
			int highestRaise = ANTE * MIN_RAISE[drawsRemaining]; // The highest raise made so far in this betting round.

			// Betting action for the current street
			while (calls < 1) { // Betting ends when there is a call, or when both players call in the
								// beginning.
				int otherPlayer = 1 - currPlayer;
				int toCall = totalBets[otherPlayer] - totalBets[currPlayer];
				int minRaise, maxRaise;
				if (raises < MAX_RAISES) {
					minRaise = Math.max(highestRaise, 2 * toCall);
					maxRaise = Math.max(highestRaise, pot + 2 * toCall);
				} else { // no more raises allowed this betting round
					minRaise = maxRaise = toCall;
				}
				try {
					action = players[currPlayer].bettingAction(drawsRemaining, hands[currPlayer], pot, raises, toCall,
							minRaise, maxRaise, drawCounts[otherPlayer]);
					String agentName = players[currPlayer].getAgentName();
					if (action > toCall && action < minRaise) {
						action = toCall;
					}
					if (action > maxRaise) {
						action = maxRaise;
					}
					message(out,
							agentName + " "
									+ (action < toCall ? "FOLDS"
											: (maxRaise > toCall && action >= minRaise
													? (toCall == 0 ? "BETS" : (raises > 1 ? "RERAISES" : "RAISES"))
															+ " " + (action - toCall) + ((toCall > 0) ? " MORE" : "")
													: (toCall == 0 ? "CHECKS" : "CALLS " + toCall)))
									+ ".");
				} catch (Exception e) { // Any failure is considered a checkfold.
					message(out, players[currPlayer].getAgentName() + " bettingAction method failed! " + e);
					action = 0;
				}
				if (action < toCall) { // current player folds, the hand is finished
					message(out, players[otherPlayer].getAgentName() + " won " + totalBets[currPlayer]
							+ " chips. score: " + currentScore);
					try {
						players[currPlayer].handComplete(hands[currPlayer], null, -totalBets[currPlayer]);
					} catch (Exception e) {
					}
					try {
						players[otherPlayer].handComplete(hands[otherPlayer], null, totalBets[currPlayer]);
					} catch (Exception e) {
					}
					return totalBets[currPlayer] * (currPlayer == 1 ? +1 : -1);
				} else if (action == toCall) { // current player merely calls
					calls++;
				} else { // current player raises

					raises++;
					calls = 0;
					// update the highest raise made on this betting round
					if (action - toCall > highestRaise) {
						highestRaise = action - toCall;
					}
				}
				pot += action;
				totalBets[currPlayer] += action;
				currPlayer = 1 - currPlayer; // and it is now opponent's turn to act
			}

			if (drawsRemaining > 0) { // Drawing action for the current street.
				for (currPlayer = 0; currPlayer <= 1; currPlayer++) {
					List<Card> cards = hands[currPlayer].getAllCards();
					List<Card> toReplace;
					try {
						toReplace = players[currPlayer].drawingAction(drawsRemaining, hands[currPlayer], pot,
								currPlayer == 0 ? -1 : drawCounts[0]);
						if (toReplace.size() > 4) {
							throw new IllegalArgumentException("Trying to replace too many cards.");
						}
						message(out, players[currPlayer].getAgentName() + " replaces cards " + toReplace + ".");
						for (Card c : toReplace) {
							if (!cards.contains(c)) {
								throw new IllegalArgumentException("Trying to replace nonexistent card " + c);
							}
							hands[currPlayer].replaceCard(c, deck);
						}
						drawCounts[currPlayer] = toReplace.size();
					} catch (Exception e) {
						message(out, players[currPlayer].getAgentName() + ": drawingAction method failed: " + e);
						return totalBets[currPlayer] * (currPlayer == 1 ? +1 : -1);
					}
				}
			}
		}

		message(out, "The hand has reached the showdown.");
		message(out, players[0].getAgentName() + " has " + hands[0] + ".");
		message(out, players[1].getAgentName() + " has " + hands[1] + ".");

		// Bug found and fix provided by Alex Ladd March 6 2018
		int showdown = hands[0].compareTo(hands[1]);
		int result = showdown < 0 ? -totalBets[0] : (showdown > 0 ? totalBets[1] : 0);
		if (showdown != 0) {
			message(out, players[showdown > 0 ? 0 : 1].getAgentName() + " won " + totalBets[1] + " chips. score: "
					+ currentScore);
			try {
				players[0].handComplete(hands[0], hands[1], showdown > 0 ? totalBets[0] : -totalBets[0]);
			} catch (Exception e) {
			}
			try {
				players[1].handComplete(hands[1], hands[0], showdown < 0 ? totalBets[1] : -totalBets[1]);
			} catch (Exception e) {
			}
		} else {
			message(out, "Both players brought equal badugi hands to showdown.");
			try {
				players[0].handComplete(hands[0], hands[1], 0);
			} catch (Exception e) {
			}
			try {
				players[1].handComplete(hands[1], hands[0], 0);
			} catch (Exception e) {
			}
		}
		return result;
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
	 */
	public static int playHeadsUp(EfficientDeck deck, PLBadugiPlayer[] players, PrintWriter out, int hands) {
		int score = 0;
		PLBadugiPlayer[] thisRoundPlayers = new PLBadugiPlayer[2];
		players[0].startNewMatch(hands);
		players[1].startNewMatch(hands);
		while (--hands >= 0) {
			if (hands % 2 == 0) {
				thisRoundPlayers[0] = players[0];
				thisRoundPlayers[1] = players[1];
			} else {
				thisRoundPlayers[0] = players[1];
				thisRoundPlayers[1] = players[0];
			}
			int sign = (hands % 2 == 0 ? +1 : -1);
			handCount++;
			if (SAMPLE_OUTPUT > 0 && handCount % SAMPLE_OUTPUT == 0 && out == null) {
				score += sign
						* playOneHand(4, deck, thisRoundPlayers, new PrintWriter(System.out), hands, sign * score);
			} else {
				score += sign * playOneHand(4, deck, thisRoundPlayers, out, hands, sign * score);
			}
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
	 */
	public static void badugiTournament(String[] agentClassNames, PrintWriter out, PrintWriter results) {

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
	 */
	public static void playThreeHandTournament() throws IOException {
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
	 */
	public static void main(String[] args) throws IOException {
		/*
		 * Modify this array to include the player classes that participate in the
		 * tournament.
		 */

		String[] playerClasses = { "IlkkaPlayer3", "PLBadugi500805168" };

		PrintWriter out = new PrintWriter(System.out);
		PrintWriter result = new PrintWriter(new FileWriter("results.txt"));
		badugiTournament(playerClasses, out, result);
		result.close();
	}
}