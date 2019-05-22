import java.io.Serializable;

/**
 * A class whose objects represent individual playing cards in the ordinary 52-card deck.
 * @author Ilkka Kokkarinen
 */

public class Card implements Comparable<Card>, Serializable {
    
    public static final String suits = "cdhs"; // or "\u2663\u2666\u2665\u2660";
    public static final String ranks = "a23456789tjqk";
    
    private final int suit, rank; // The suit and rank of this card.
    private final String repr; // The cached String representation of this card.

    /**
     * Constructor for the class.
     * @param suit The suit of this card. Should be an int from 0 to 3.
     * @param rank The rank of this card. Should be an int from 0 (ace) to 12 (king).
     */
    public Card(int suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.repr = ranks.charAt(rank) + "" + suits.charAt(suit);
    }       

    public static Card from(String s) {
        if(s.length() != 2) {
            throw new IllegalArgumentException("String " + s + " is not a proper card");
        }
        s = s.toLowerCase();
        int rank = ranks.indexOf(s.charAt(0));
        int suit = suits.indexOf(s.charAt(1));
        if(rank < 0 || suit < 0) {
            throw new IllegalArgumentException("String " + s + " is not a proper card");
        }
        return new Card(suit, rank);
    }
    
    /**
     * Accessor method for the suit.
     * @return The suit of the card, as an integer from 0 to 3.
     */
    public int getSuit() { return suit; }
    
    /**
     * Accessor method for the rank.
     * @return The rank of the card, as an integer from 1 (ace) to 13 (king).
     */
    public int getRank() { return rank + 1; }
    
    /**
     * Equality comparison of {@code Card} objects. No {@code Card} is ever equal to something that is not
     * a {@code Card},
     * and two {@code Card} objects are equal if they are the same object, or have the same suit and rank.
     * @return The result of the equality comparison.
     */
    public boolean equals(Object other) {
        if(!(other instanceof Card)) { return false; }
        Card o = (Card) other;
        return this == other || (this.suit == o.suit && this.rank == o.rank);
    }
        
    /**
     * Returns the String representation for this card.
     * @return The String representation for this card.
     */
    public String toString() { return repr; }
    
    /**
     * The order comparison between {@code Card} objects, as defined in interface {@code Comparable<Card>}.
     * @param other The other {@code Card} to compare to this {@code Card}.
     * @return The result of the order comparison as an int -1, 0 or +1 whose sign gives the result.
     */
    public int compareTo(Card other) {
        if(this.rank > other.rank) return -1;
        if(other.rank > this.rank) return +1;
        if(this.suit > other.suit) return -1;
        if(other.suit > this.suit) return +1;
        return 0;
    }
    
    /**
     * Checks whether {@code this} card and the {@other} card may not be part of the same badugi hand.
     * @param other The other {@code Card} that participates in the test.
     * @return The result of the test.
     */
    public boolean badugiConflict(Card other) {
        return this.suit == other.suit || this.rank == other.rank;
    }
}