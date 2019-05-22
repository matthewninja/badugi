import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class PokerHands {
	public static final int NUMBER_OF_HANDS = 270_725; // thanks prolog
	public static ArrayList<PLBadugiHand> sorted_hands = new ArrayList<PLBadugiHand>();

	public static ArrayList<PLBadugiHand> allHands() throws IOException { // only do this once. save to data file
		if (sorted_hands.size() == NUMBER_OF_HANDS)
			return sorted_hands;
//		FileWriter sarWriter = new FileWriter("data/sorted_hands.txt", true);
		File file = new File("data/hands.txt");

		String thisLine;
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine()) {
			thisLine = sc.nextLine();

			ArrayList<Card> hand = new ArrayList<Card>();
			String[] arr = thisLine.split(","); // length 8
			for (int i = 0; i < 8; i = i + 2) {
				hand.add(Card.from(arr[i] + arr[i + 1]));
			}
			Collections.sort(hand);
			PLBadugiHand h = new PLBadugiHand(hand);
			// testing
			
			sorted_hands.add(h);
		}
		sc.close();
		Collections.sort(sorted_hands);
//		for (PLBadugiHand q : sorted_hands) {
////			sarWriter.write(q.toString()+'\n');
//		}
//		sarWriter.close();
		
		// sets have no duplicates
		if (sorted_hands.size() != NUMBER_OF_HANDS)
			throw new RuntimeException("Something went wrong making hands...");
		Collections.sort(sorted_hands);
		for (int i = 1; i < NUMBER_OF_HANDS - 1; i++) {
			if (sorted_hands.get(i - 1).compareTo(sorted_hands.get(i)) > 0)
				throw new RuntimeException("Something went wrong making hands...");
		}
		// save
//        FileOutputStream fileOut = new FileOutputStream("data/hand_strength_map.ser");
//        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
//        objectOut.writeObject(sorted_hands);
//        objectOut.close();
//        System.out.println("sorted hand size :" + sorted_hands.size());
		return sorted_hands;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		allHands();
	}
}