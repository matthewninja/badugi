import java.util.*;

/**
 * This interface defines the methods that every badugi player subclass must implement.
 * @author Ilkka Kokkarinen
 */
public interface PLBadugiPlayer
{
    
    /**
     * The method to inform the agent that a new heads-up match is starting.
     * @param handsToGo How many hands this tournament consists of.
     */
    default public void startNewMatch(int handsToGo) { }
    
    /**
     * The method to inform the agent that the current heads-up match has ended.
     * @param finalScore The total number of chips accumulated by this player during the match.  
     */
    default public void finishedMatch(int finalScore) { }
    
    /**
     * The method to inform the agent that a new hand is starting.
     * @param position 0 if the agent is the dealer in this hand, 1 if the opponent.
     * @param handsToGo The number of hands left to play in this heads-up tournament.
     * @param currentScore The current score of the tournament.
     */
    public void startNewHand(int position, int handsToGo, int currentScore);
    
    /**
     * The method to ask the agent what betting action it wants to perform.
     * @param drawsRemaining How many draws are remaining after this betting round.
     * @param hand The current hand held by this player.
     * @param pot The current size of the pot.
     * @param raises The number of raises made in this round.
     * @param toCall The cost to call to stay in the pot.
     * @param minRaise The minimum allowed raise to make, if the agent wants to raise.
     * @param maxRaise The maximum allowed raise to make, if the agent wants to raise.
     * @param opponentDrew How many cards the opponent drew in the previous drawing round. In the
     * first betting round, this argument will be -1.
     * @return The amount of chips that the player pushes into the pot. Putting in less than
     * toCall means folding. Any amount less than minRaise becomes a call, and any amount between
     * minRaise and maxRaise, inclusive, is a raise. Any amount greater than maxRaise is clipped at
     * maxRaise.
     */
    public int bettingAction(int drawsRemaining, PLBadugiHand hand, int pot, int raises, int toCall,
                             int minRaise, int maxRaise, int opponentDrew);
    
    /**
     * The method to ask the agent which cards it wants to replace in this drawing round.
     * @param drawsRemaining How many draws are remaining, including this drawing round.
     * @param hand The current hand held by this player.
     * @param pot The current size of the pot.
     * @param dealerDrew How many cards the dealer drew in this drawing round. When this method is called
     * for the dealer, this argument will be -1.
     * @return The list of cards in the hand that the agent wants to replace.
     */
    public List<Card> drawingAction(int drawsRemaining, PLBadugiHand hand, int pot, int dealerDrew);
    
    /**
     * The method that gets called at the end of the current hand, whether fold or showdown.
     * @param yourHand The hand held by this agent.
     * @param opponentHand The hand held by the opponent, or null if either player folded.
     * @param result The win or the loss in chips for the player.
     */
    public void handComplete(PLBadugiHand yourHand, PLBadugiHand opponentHand, int result);
 
    /**
     * Returns the nickname of this agent.
     * @return The nickname of this agent.
     */
    public String getAgentName();
    
    /**
     * Returns the author of this agent. The name should be given in the format "Last, First".
     * @return The author of this agent.
     */
    public String getAuthor();
}