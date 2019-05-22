import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class PLBadugiDataCollector implements PLBadugiPlayer {
	/*****************************************************
	 Initialization related variables ********************
	 *****************************************************/
	String name;
    private static int id = 0; // Counter of how many objects have been created.
    Scanner sc = new Scanner(System.in);
    
    /*****************************************************
	 Hand related variables ******************************
	 *****************************************************/
    private int position;
    
	public PLBadugiDataCollector (String name) {
        this.name = name;
        
    }
	
	public PLBadugiDataCollector() {
        this.name = "Collector " + (++id);
    }

	@Override
	public void startNewHand(int position, int handsToGo, int currentScore) {
		this.position = position;
		
	}

	@Override
	public int bettingAction(int drawsRemaining, PLBadugiHand hand, int pot, int raises, int toCall, int minRaise,
			int maxRaise, int opponentDrew) {
		Random rand = new Random();
		int actionToTake = rand.nextInt(3);
		if (actionToTake == 0) return toCall; // flat call
		else if (actionToTake == 1) return 0; // fold
		else return (int)(minRaise + (maxRaise-minRaise)*0.3); // raise
	}
	/**
	 * bet with no freedom
	 * @param actionToTake		0 = toCall, 1 = 0, 2 = minRaise + (maxRaise-minRaise)*0.3
	 * @return amount of chips to bet
	 */
	public int bettingActionForced(int actionToTake, int drawsRemaining, double handStrength, int pot, 
			int raises, int toCall, int minRaise, int maxRaise, int opponentDrew) {
		if (actionToTake == 0) return toCall; // flat call
		else if (actionToTake == 1) return 0; // fold
		else return (int)(minRaise + (maxRaise-minRaise)*0.3); // raise
	}

	@Override
	public List<Card> drawingAction(int drawsRemaining, PLBadugiHand hand, int pot, int dealerDrew) {
//		List<Card> allCards = hand.getAllCards();
        List<Card> inactiveCards = hand.getInactiveCards();
        List<Card> pitch = new ArrayList<Card>();
        
        // With a four card badugi, pitch nothing.
        if (inactiveCards.size() == 0) return pitch;
        
        // Pitch all inactive cards
        for (Card c : inactiveCards) {
        	pitch.add(c);
        }
        
		return pitch;
	}

	@Override
	public void handComplete(PLBadugiHand yourHand, PLBadugiHand opponentHand, int result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAgentName() {return name;}

	@Override
	public String getAuthor() {return "Collector";}

}
