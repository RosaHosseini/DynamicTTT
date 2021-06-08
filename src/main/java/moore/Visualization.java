package moore;

import moore.data.MealySULReader;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.visualization.DefaultVisualizationHelper;

import java.io.File;

import static net.automatalib.visualization.Visualization.visualize;

public class Visualization {

    public static void main(String[] args) throws Exception {
        String path = "Mealy/Nordsec16/server_101k.dot";
        CompactMealy<String, String> mealy = new MealySULReader().
                parseModelFromDot(new File(path));
        visualize(
                mealy,
                mealy.getInputAlphabet(),
                new DefaultVisualizationHelper<>()
        );
    }
}
