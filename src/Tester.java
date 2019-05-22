import java.io.IOException;

public class Tester {
	public static void main(String[] args) throws Exception {
		PLBadugi500805168 test_agent = new PLBadugi500805168();
		PLBadugiHand hand = new PLBadugiHand("kckhkdks");
		System.out.println(hand.toString());
		System.out.println(PLBadugi500805168.handStrength(hand));
	}
}
