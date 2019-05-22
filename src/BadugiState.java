import lombok.Value;
import org.deeplearning4j.rl4j.space.Encodable;

@Value
public class BadugiState implements Encodable {

    double[] values;
    int step;

    public double[] toArray() {
        return values;
    }
}
